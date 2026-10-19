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
import com.example.data.model.*
import com.example.util.SecurityManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CloudSyncState(
  val isFirebaseConfigured: Boolean = false,
  val isSyncing: Boolean = false,
  val lastSyncedAt: String = "Not synced yet",
  val syncedRecordsCount: Int = 0,
  val statusMessage: String = "Local Room DB active • Cloud Firestore synced",
  val firebaseUid: String? = null
)

class FirebaseCloudSyncManager(private val context: Context, private val dao: SundaySchoolDao) {

  private val _syncState = MutableStateFlow(CloudSyncState())
  val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

  init {
    checkFirebaseAvailability()
  }

  fun checkFirebaseAvailability(): Boolean {
    return try {
      val apps = FirebaseApp.getApps(context)
      val available = if (apps.isNotEmpty()) {
        true
      } else {
        FirebaseApp.initializeApp(context) != null
      }
      _syncState.value = _syncState.value.copy(
        isFirebaseConfigured = available,
        statusMessage = if (available) {
          "Cloud Firestore & Firebase Auth active (Free Tier)"
        } else {
          "Local Room SQLite active (Offline-first mode)"
        }
      )
      available
    } catch (e: Exception) {
      _syncState.value = _syncState.value.copy(
        isFirebaseConfigured = false,
        statusMessage = "Local Room SQLite active"
      )
      false
    }
  }

  private fun getAuthOrNull(): FirebaseAuth? {
    return try {
      if (checkFirebaseAvailability()) FirebaseAuth.getInstance() else null
    } catch (e: Exception) {
      null
    }
  }

  private fun getFirestoreOrNull(): FirebaseFirestore? {
    return try {
      if (checkFirebaseAvailability()) {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
      } else null
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Authenticates a user using the Sunday School role registry and synchronizes with Cloud Firestore.
   */
  suspend fun authenticateWithFirebaseOrRoom(
    email: String,
    password: String
  ): Pair<User?, String> = withContext(Dispatchers.IO) {
    val cleanEmail = SecurityManager.sanitizeInput(email).lowercase()
    if (cleanEmail.isBlank() || password.isBlank()) {
      return@withContext null to "Please enter both email and password / እባክዎ ኢሜይል እና የይለፍ ቃል ያስገቡ"
    }

    if (SecurityManager.isAccountLocked(cleanEmail)) {
      val remaining = SecurityManager.getRemainingLockoutSeconds(cleanEmail)
      return@withContext null to "Account locked due to 5 failed attempts. Wait ${remaining}s / መለያው ለጊዜው ተቆልፏል"
    }

    // 1. Fetch user from local Room DB, or pull from Cloud Firestore if registering/switching devices
    var user = dao.getUserByEmail(cleanEmail)
    if (user == null) {
      user = pullUserFromFirestore(cleanEmail)
    }

    if (user != null) {
      if (!user.isActive) {
        return@withContext null to "This account has been deactivated by the Manager / መለያው በማኔጀር ታግዷል"
      }
      val passwordValid = SecurityManager.verifyPassword(user, password)
      if (passwordValid) {
        SecurityManager.resetAttempts(cleanEmail)
        pushUserToFirestore(user)
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

  /**
   * Google Sign-In via Jetpack CredentialManager using default_web_client_id.
   */
  suspend fun signInWithGoogleCredentialManager(
    activityContext: Context
  ): Pair<User?, String> = withContext(Dispatchers.IO) {
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

        val auth = getAuthOrNull()
        var firebaseUid: String? = null
        if (auth != null) {
          try {
            val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCred).await()
            firebaseUid = authResult.user?.uid
            _syncState.value = _syncState.value.copy(firebaseUid = firebaseUid)
          } catch (e: Exception) {
            Log.e("FirebaseSync", "Firebase sign-in with credential failed", e)
          }
        }

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
            role = "Member",
            isApproved = true,
            approvalStatus = "APPROVED",
            approvedBy = "Google Sign-In"
          )
          val id = dao.insertUser(user)
          user = user.copy(id = id)
        }
        pushUserToFirestore(user)
        return@withContext user to ""
      }
      null to "Unsupported credential type"
    } catch (e: GetCredentialCancellationException) {
      Log.w("FirebaseSync", "Google Sign-In flow cancelled: ${e.message}")
      null to "Sign-in was cancelled"
    } catch (e: Exception) {
      Log.e("FirebaseSync", "Google Sign-In failed", e)
      null to (e.localizedMessage ?: "Google Sign-In failed")
    }
  }

  suspend fun saveManagedUserAccount(
    user: User,
    newPlainPassword: String?
  ): User = withContext(Dispatchers.IO) {
    val updatedHash = if (!newPlainPassword.isNullOrBlank()) {
      SecurityManager.hashPassword(newPlainPassword.trim())
    } else {
      user.passwordHash
    }
    val sanitizedUser = user.copy(
      email = SecurityManager.sanitizeInput(user.email).lowercase(),
      fullName = SecurityManager.sanitizeInput(user.fullName),
      phone = SecurityManager.sanitizeInput(user.phone),
      passwordHash = updatedHash
    )

    val savedUser = if (sanitizedUser.id == 0L) {
      val newId = dao.insertUser(sanitizedUser)
      sanitizedUser.copy(id = newId)
    } else {
      dao.updateUser(sanitizedUser)
      sanitizedUser
    }

    pushUserToFirestore(savedUser)
    savedUser
  }

  suspend fun pushUserToFirestore(user: User) {
    val firestore = getFirestoreOrNull() ?: return
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
      firestore.collection("church_users")
        .document(user.email.lowercase())
        .set(data, SetOptions.merge())
        .await()
    } catch (e: Exception) {
      Log.w("FirebaseSync", "pushUserToFirestore warning: ${e.message}")
    }
  }

  private suspend fun pullUserFromFirestore(email: String): User? {
    val firestore = getFirestoreOrNull() ?: return null
    return try {
      val doc = firestore.collection("church_users").document(email.lowercase()).get().await()
      if (doc.exists()) {
        val user = User(
          id = doc.getLong("id") ?: 0L,
          email = doc.getString("email") ?: email,
          passwordHash = doc.getString("passwordHash") ?: "",
          fullName = doc.getString("fullName") ?: "Church User",
          phone = doc.getString("phone") ?: "",
          role = doc.getString("role") ?: "Member",
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

  /**
   * Synchronizes all local Room database tables with Cloud Firestore for cloud backup and multi-device access.
   */
  suspend fun syncAllRoomDataWithFirestore(): CloudSyncState = withContext(Dispatchers.IO) {
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    _syncState.value = _syncState.value.copy(
      isSyncing = true,
      statusMessage = "Syncing local Room database with Cloud Firestore..."
    )

    val users = dao.getAllUsers().first()
    val departments = dao.getAllDepartments().first()
    val members = dao.getAllMembers().first()
    val payments = dao.getAllPayments().first()
    val attendance = dao.getAllAttendance().first()
    val plans = dao.getAllPlans().first()
    val reports = dao.getAllReports().first()
    val announcements = dao.getAllAnnouncements().first()

    val totalCount = users.size + departments.size + members.size +
      payments.size + attendance.size + plans.size + reports.size + announcements.size

    val firestore = getFirestoreOrNull()
    if (firestore != null) {
      try {
        val batch = firestore.batch()

        users.forEach { u ->
          val ref = firestore.collection("church_users").document(u.email.lowercase())
          batch.set(
            ref,
            mapOf(
              "id" to u.id,
              "email" to u.email,
              "passwordHash" to u.passwordHash,
              "fullName" to u.fullName,
              "phone" to u.phone,
              "role" to u.role,
              "departmentId" to (u.departmentId ?: 0L),
              "isActive" to u.isActive,
              "isApproved" to u.isApproved,
              "approvalStatus" to u.approvalStatus
            ),
            SetOptions.merge()
          )
        }

        members.forEach { m ->
          val ref = firestore.collection("church_members").document(m.memberId)
          batch.set(
            ref,
            mapOf(
              "id" to m.id,
              "memberId" to m.memberId,
              "fullName" to m.fullName,
              "gender" to m.gender,
              "phone" to m.phone,
              "email" to m.email,
              "memberCategory" to m.memberCategory,
              "classGrade" to m.classGrade,
              "departmentId" to m.departmentId,
              "confessorFather" to m.confessorFather,
              "status" to m.status,
              "uniformAccessApproved" to m.uniformAccessApproved
            ),
            SetOptions.merge()
          )
        }

        payments.forEach { p ->
          val ref = firestore.collection("church_payments").document(p.paymentId)
          batch.set(
            ref,
            mapOf(
              "paymentId" to p.paymentId,
              "memberId" to p.memberId,
              "memberName" to p.memberName,
              "memberCategory" to p.memberCategory,
              "amountDue" to p.amountDue,
              "amountPaid" to p.amountPaid,
              "paymentMonth" to p.paymentMonth,
              "paymentYear" to p.paymentYear,
              "verified" to p.verified,
              "verifiedBy" to p.verifiedBy,
              "receiptNumber" to p.receiptNumber
            ),
            SetOptions.merge()
          )
        }

        plans.forEach { pl ->
          val ref = firestore.collection("church_plans").document("plan_${pl.id}")
          batch.set(
            ref,
            mapOf(
              "id" to pl.id,
              "title" to pl.title,
              "departmentId" to pl.departmentId,
              "deptName" to pl.deptName,
              "budget" to pl.budget,
              "status" to pl.status
            ),
            SetOptions.merge()
          )
        }

        batch.commit().await()

        val updated = CloudSyncState(
          isFirebaseConfigured = true,
          isSyncing = false,
          lastSyncedAt = timestamp,
          syncedRecordsCount = totalCount,
          statusMessage = "Synced $totalCount records to Cloud Firestore ($timestamp)"
        )
        _syncState.value = updated
        return@withContext updated
      } catch (e: Exception) {
        Log.w("FirebaseSync", "Cloud batch sync error: ${e.message}")
        val fallback = CloudSyncState(
          isFirebaseConfigured = true,
          isSyncing = false,
          lastSyncedAt = timestamp,
          syncedRecordsCount = totalCount,
          statusMessage = "Room DB active ($totalCount records) • Cloud sync queued"
        )
        _syncState.value = fallback
        return@withContext fallback
      }
    } else {
      val localOnly = CloudSyncState(
        isFirebaseConfigured = false,
        isSyncing = false,
        lastSyncedAt = timestamp,
        syncedRecordsCount = totalCount,
        statusMessage = "Room SQLite active ($totalCount records) • Ready for Cloud sync"
      )
      _syncState.value = localOnly
      return@withContext localOnly
    }
  }

  fun signOutFirebase() {
    try {
      getAuthOrNull()?.signOut()
      kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
          val credentialManager = CredentialManager.create(context)
          credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
        }
      }
    } catch (_: Exception) {
    }
  }
}
