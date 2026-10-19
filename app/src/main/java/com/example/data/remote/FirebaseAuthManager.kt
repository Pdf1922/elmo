package com.example.data.remote

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.local.SundaySchoolDao
import com.example.data.model.MemberItem
import com.example.data.model.User
import com.example.util.SecurityManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FirebaseAuthManager handles:
 * 1. User login (Google Sign-In with CredentialManager + Role-based credential verification).
 * 2. User registration with role assignment (Student, Parent, Teacher, DeptAdmin, Manager).
 * 3. Role-based access control management in Cloud Firestore and local Room database.
 * 4. Approval lifecycle and multi-device account synchronization.
 */
class FirebaseAuthManager(
  private val context: Context,
  private val dao: SundaySchoolDao
) {
  private val _authenticatedUser = MutableStateFlow<User?>(null)
  val authenticatedUser: StateFlow<User?> = _authenticatedUser.asStateFlow()

  val isConfigured: Boolean
    get() = try {
      val apps = FirebaseApp.getApps(context)
      apps.isNotEmpty() || FirebaseApp.initializeApp(context) != null
    } catch (_: Exception) {
      false
    }

  val auth: FirebaseAuth?
    get() = try {
      if (isConfigured) FirebaseAuth.getInstance() else null
    } catch (_: Exception) {
      null
    }

  val currentFirebaseUser: FirebaseUser?
    get() = auth?.currentUser

  private val firestore: FirebaseFirestore?
    get() = try {
      if (isConfigured) {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
      } else null
    } catch (_: Exception) {
      null
    }

  /**
   * Google Sign-In using Android Jetpack CredentialManager and Firebase Auth.
   */
  suspend fun signInWithGoogle(activityContext: Context): Pair<User?, String> = withContext(Dispatchers.IO) {
    val clientId = try {
      activityContext.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
      return@withContext null to "Google Sign-In configuration missing: default_web_client_id not found"
    }

    try {
      val credentialManager = CredentialManager.create(activityContext)
      val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
      val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

      val result = credentialManager.getCredential(
        request = request,
        context = activityContext as Activity
      )

      val credential = result.credential
      if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken
        val email = googleIdTokenCredential.id.lowercase()
        val displayName = googleIdTokenCredential.displayName ?: "Church Member"

        // Link with Firebase Auth
        auth?.let { fbAuth ->
          try {
            val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
            fbAuth.signInWithCredential(firebaseCred).await()
          } catch (e: Exception) {
            Log.e("FirebaseAuthMgr", "Firebase credential exchange failed", e)
          }
        }

        // Pull existing profile from Firestore or Room, or create new student/member account
        var user = dao.getUserByEmail(email)
        if (user == null) {
          user = pullUserFromFirestore(email)
        }
        if (user == null) {
          user = User(
            email = email,
            passwordHash = SecurityManager.hashPassword(idToken.take(16)),
            fullName = displayName,
            phone = "+251900000000",
            role = "Student",
            isApproved = true,
            approvalStatus = "APPROVED",
            approvedBy = "Google Auth"
          )
          val id = dao.insertUser(user)
          user = user.copy(id = id)

          // Also register corresponding student member record
          val member = MemberItem(
            memberId = "MH-${(1000..9999).random()}",
            fullName = displayName,
            gender = "Male",
            phone = "+251900000000",
            email = email,
            address = "Addis Ababa",
            memberCategory = "Student",
            classGrade = "Grade 9-12 / 9-12ኛ ክፍል",
            departmentId = 1L,
            confessorFather = "",
            joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            status = "Active"
          )
          dao.insertMember(member)
        }

        pushUserToFirestore(user)
        _authenticatedUser.value = user
        return@withContext user to ""
      }
      null to "Unsupported credential returned"
    } catch (e: GetCredentialCancellationException) {
      Log.w("FirebaseAuthMgr", "Google Sign-In flow cancelled: ${e.message}")
      null to "Sign-in was cancelled"
    } catch (e: Exception) {
      Log.e("FirebaseAuthMgr", "Google Sign-In failed", e)
      null to (e.localizedMessage ?: "Google Sign-In failed")
    }
  }

  /**
   * Registers a new user with a specific role, storing in Cloud Firestore & local Room DB.
   * Auto-approves Students and Parents; DeptAdmins and Teachers require Admin/Manager approval.
   */
  suspend fun registerUserWithRole(
    fullName: String,
    email: String,
    plainPassword: String,
    phone: String,
    role: String,
    departmentId: Long?,
    category: String = "Student",
    classGrade: String = "Grade 9-12 / 9-12ኛ ክፍል",
    gender: String = "Male",
    confessorFather: String = "",
    address: String = "Addis Ababa"
  ): Pair<User?, String> = withContext(Dispatchers.IO) {
    val cleanName = SecurityManager.sanitizeInput(fullName)
    val cleanEmail = SecurityManager.sanitizeInput(email).lowercase()
    val cleanPhone = SecurityManager.sanitizeInput(phone)

    if (cleanName.isBlank() || cleanEmail.isBlank() || plainPassword.isBlank()) {
      return@withContext null to "Name, email, and password are required / ሙሉ ስም፣ ኢሜይል እና የይለፍ ቃል ያስፈልጋል"
    }

    // Role-dependent approval: Students, Parents, Members are auto-approved.
    // Managers registering themselves or seeded are approved. DeptAdmin & Teacher require approval.
    val isApproved = role == "Student" || role == "Member" || role == "Parent" || role == "Manager"

    val user = User(
      email = cleanEmail,
      passwordHash = SecurityManager.hashPassword(plainPassword),
      fullName = cleanName,
      phone = cleanPhone.ifBlank { "+251911000000" },
      role = role,
      departmentId = if (role == "Manager") null else departmentId,
      isApproved = isApproved,
      approvalStatus = if (isApproved) "APPROVED" else "PENDING",
      approvedBy = if (isApproved) "Self-Registration" else "Pending Manager Approval",
      isActive = true
    )

    val existing = dao.getUserByEmail(cleanEmail)
    val savedUser = if (existing != null) {
      val updated = existing.copy(
        fullName = cleanName,
        phone = cleanPhone.ifBlank { existing.phone },
        role = role,
        departmentId = departmentId,
        passwordHash = SecurityManager.hashPassword(plainPassword)
      )
      dao.updateUser(updated)
      updated
    } else {
      val id = dao.insertUser(user)
      user.copy(id = id)
    }

    // Also create Member record if registering as student or member
    if (role == "Student" || role == "Member" || role == "Parent") {
      val member = MemberItem(
        memberId = "MH-${(1000..9999).random()}",
        fullName = cleanName,
        gender = gender,
        phone = cleanPhone.ifBlank { "+251911000000" },
        email = cleanEmail,
        address = address,
        memberCategory = category,
        classGrade = classGrade,
        departmentId = departmentId ?: 1L,
        confessorFather = confessorFather,
        joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        status = "Active"
      )
      dao.insertMember(member)
    }

    pushUserToFirestore(savedUser)
    _authenticatedUser.value = savedUser
    return@withContext savedUser to ""
  }

  /**
   * Role assignment method: allows Admins/Managers to assign user role and department in Firestore & Room.
   */
  suspend fun assignUserRole(
    targetEmail: String,
    newRole: String,
    departmentId: Long?,
    isApproved: Boolean,
    assignedByManager: String = "Manager"
  ): Boolean = withContext(Dispatchers.IO) {
    val cleanEmail = targetEmail.trim().lowercase()
    var user = dao.getUserByEmail(cleanEmail)
    if (user != null) {
      val updated = user.copy(
        role = newRole,
        departmentId = departmentId,
        isApproved = isApproved,
        approvalStatus = if (isApproved) "APPROVED" else "PENDING",
        approvedBy = assignedByManager
      )
      dao.updateUser(updated)
      pushUserToFirestore(updated)
      if (_authenticatedUser.value?.email == cleanEmail) {
        _authenticatedUser.value = updated
      }
      return@withContext true
    } else {
      // Update in Firestore
      firestore?.let { db ->
        try {
          db.collection("church_users").document(cleanEmail).set(
            mapOf(
              "role" to newRole,
              "departmentId" to (departmentId ?: 0L),
              "isApproved" to isApproved,
              "approvalStatus" to (if (isApproved) "APPROVED" else "PENDING"),
              "approvedBy" to assignedByManager,
              "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
          ).await()
          return@withContext true
        } catch (_: Exception) {
          return@withContext false
        }
      }
      return@withContext false
    }
  }

  /**
   * Authenticates using role credentials with brute-force lockout and Firestore synchronization.
   */
  suspend fun loginWithRoleCredentials(
    email: String,
    pass: String
  ): Pair<User?, String> = withContext(Dispatchers.IO) {
    val cleanEmail = SecurityManager.sanitizeInput(email).lowercase()
    if (cleanEmail.isBlank() || pass.isBlank()) {
      return@withContext null to "Please enter both email and password / እባክዎ ኢሜይል እና የይለፍ ቃል ያስገቡ"
    }

    if (SecurityManager.isAccountLocked(cleanEmail)) {
      val remaining = SecurityManager.getRemainingLockoutSeconds(cleanEmail)
      return@withContext null to "Account locked due to 5 failed attempts. Wait ${remaining}s / መለያው ለጊዜው ተቆልፏል"
    }

    var user = dao.getUserByEmail(cleanEmail)
    if (user == null) {
      user = pullUserFromFirestore(cleanEmail)
    }

    if (user != null) {
      if (!user.isActive) {
        return@withContext null to "This account has been deactivated by the Manager / መለያው በማኔጀር ታግዷል"
      }
      val passwordValid = SecurityManager.verifyPassword(user, pass)
      if (passwordValid) {
        SecurityManager.resetAttempts(cleanEmail)
        pushUserToFirestore(user)
        _authenticatedUser.value = user
        return@withContext user to ""
      }
    }

    val locked = SecurityManager.recordFailedAttempt(cleanEmail)
    if (locked) {
      null to "Too many failed attempts! Account locked for 5 minutes / መለያው ለ5 ደቂቃ ተቆልፏል"
    } else {
      val rem = (SecurityManager.MAX_FAILED_ATTEMPTS - SecurityManager.getFailedAttemptCount(cleanEmail)).coerceAtLeast(1)
      null to "Invalid email or password ($rem attempts left) / የተሳሳተ ኢሜይል ወይም የይለፍ ቃል"
    }
  }

  fun isCurrentUserAdmin(): Boolean {
    val r = _authenticatedUser.value?.role
    return r == "Manager" || r == "Super Admin" || r == "Admin" || r == "DeptAdmin"
  }

  fun isCurrentUserTeacher(): Boolean {
    val r = _authenticatedUser.value?.role
    return isCurrentUserAdmin() || r == "Teacher" || r == "Song Instructor"
  }

  fun isCurrentUserStudent(): Boolean {
    val r = _authenticatedUser.value?.role
    return r == "Student" || r == "Member"
  }

  fun isCurrentUserParent(): Boolean {
    val r = _authenticatedUser.value?.role
    return r == "Parent"
  }

  fun hasRole(expectedRole: String): Boolean {
    return _authenticatedUser.value?.role?.equals(expectedRole, ignoreCase = true) == true
  }

  suspend fun pushUserToFirestore(user: User) {
    firestore?.let { db ->
      try {
        val data = mapOf(
          "id" to user.id,
          "email" to user.email,
          "passwordHash" to user.passwordHash,
          "fullName" to user.fullName,
          "phone" to user.phone,
          "role" to user.role,
          "departmentId" to (user.departmentId ?: 0L),
          "isActive" to user.isActive,
          "isApproved" to user.isApproved,
          "approvalStatus" to user.approvalStatus,
          "approvedBy" to (user.approvedBy ?: ""),
          "updatedAt" to System.currentTimeMillis()
        )
        db.collection("church_users")
          .document(user.email.lowercase())
          .set(data, SetOptions.merge())
          .await()
      } catch (e: Exception) {
        Log.w("FirebaseAuthMgr", "pushUserToFirestore: ${e.message}")
      }
    }
  }

  suspend fun pullUserFromFirestore(email: String): User? {
    val db = firestore ?: return null
    return try {
      val doc = db.collection("church_users").document(email.lowercase()).get().await()
      if (doc.exists()) {
        val user = User(
          id = doc.getLong("id") ?: 0L,
          email = doc.getString("email") ?: email,
          passwordHash = doc.getString("passwordHash") ?: "",
          fullName = doc.getString("fullName") ?: "Sunday School User",
          phone = doc.getString("phone") ?: "",
          role = doc.getString("role") ?: "Student",
          departmentId = doc.getLong("departmentId"),
          isActive = doc.getBoolean("isActive") ?: true,
          isApproved = doc.getBoolean("isApproved") ?: true,
          approvalStatus = doc.getString("approvalStatus") ?: "APPROVED",
          approvedBy = doc.getString("approvedBy") ?: "Manager"
        )
        val newId = dao.insertUser(user)
        user.copy(id = newId)
      } else null
    } catch (_: Exception) {
      null
    }
  }

  fun signOut() {
    try {
      auth?.signOut()
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val credentialManager = CredentialManager.create(context)
          credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
        }
      }
    } catch (_: Exception) {
    }
    _authenticatedUser.value = null
  }
}
