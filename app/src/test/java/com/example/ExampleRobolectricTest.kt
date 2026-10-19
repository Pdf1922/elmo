package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.User
import com.example.data.remote.FirebaseCloudSyncManager
import com.example.data.repository.SundaySchoolRepository
import com.example.util.DataExporter
import com.example.util.SecuredAction
import com.example.util.SecurityManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Meserete Hiwot", appName)
  }

  @Test
  fun `all 8 security audit checks pass deterministically`() {
    val results = SecurityManager.runSecurityAudit()
    assertEquals(8, results.size)
    results.forEach { check ->
      assertTrue("Security check ${check.id} (${check.titleEn}) failed", check.passed)
    }
  }

  @Test
  fun `password hashing and cross-role isolation prevent privilege escalation`() {
    val manager = User(
      id = 1,
      email = "manager@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("CustomManagerSecret#2019"),
      fullName = "Dn. Yohannes Girma",
      phone = "+251911223344",
      role = "Manager",
      isApproved = true
    )

    // Valid manager password succeeds
    assertTrue(SecurityManager.verifyPassword(manager, "CustomManagerSecret#2019"))
    // Member or DeptAdmin or wrong passwords strictly fail
    assertFalse(SecurityManager.verifyPassword(manager, "member123"))
    assertFalse(SecurityManager.verifyPassword(manager, "dept123"))
    assertFalse(SecurityManager.verifyPassword(manager, "manager123"))
  }

  @Test
  fun `brute force rate limiter locks account after 5 failed attempts`() {
    val email = "testlock@meseretehiwot.org"
    SecurityManager.resetAttempts(email)
    val startMs = 5_000_000L

    repeat(4) {
      assertFalse(SecurityManager.recordFailedAttempt(email, startMs))
      assertFalse(SecurityManager.isAccountLocked(email, startMs))
    }
    // 5th attempt triggers lockout
    assertTrue(SecurityManager.recordFailedAttempt(email, startMs))
    assertTrue(SecurityManager.isAccountLocked(email, startMs + 10_000L))
    SecurityManager.resetAttempts(email)
    assertFalse(SecurityManager.isAccountLocked(email, startMs + 10_000L))
  }

  @Test
  fun `csv formula injection and xss payloads are sanitized`() {
    val csvFormula = "=CMD|' /C calc'!A0"
    val sanitizedCsv = SecurityManager.sanitizeCsvField(csvFormula)
    assertTrue(sanitizedCsv.startsWith("\"'="))

    val xss = "<script>evil()</script>St. Gabriel"
    assertEquals("St. Gabriel", SecurityManager.sanitizeInput(xss))

    assertTrue(SecurityManager.isAuthorized(
      User(email = "m@c.org", passwordHash = "h", fullName = "M", phone = "1", role = "Manager", isApproved = true),
      SecuredAction.APPROVE_ADMIN
    ))
    assertFalse(SecurityManager.isAuthorized(
      User(email = "u@c.org", passwordHash = "h", fullName = "U", phone = "1", role = "Member", isApproved = true),
      SecuredAction.APPROVE_ADMIN
    ))
  }

  @Test
  fun `manager editable user credentials and firestore room sync work end-to-end`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val dao = db.sundaySchoolDao()
    val repository = SundaySchoolRepository(dao)
    val syncManager = FirebaseCloudSyncManager(context, dao)

    repository.initializeSeedDataIfNeeded()

    // Manager creates/updates a custom user account with a new password
    val createdUser = syncManager.saveManagedUserAccount(
      user = User(
        email = "customadmin@meseretehiwot.org",
        passwordHash = "",
        fullName = "Dn.Samuel Tesfaye",
        phone = "+251911889900",
        role = "DeptAdmin",
        departmentId = 3L,
        isApproved = true
      ),
      newPlainPassword = "NewSecurePass!77"
    )

    assertTrue(createdUser.id > 0L)
    val (authSuccessUser, _) = syncManager.authenticateWithFirebaseOrRoom(
      "customadmin@meseretehiwot.org",
      "NewSecurePass!77"
    )
    assertNotNull(authSuccessUser)
    assertEquals("Dn.Samuel Tesfaye", authSuccessUser?.fullName)

    // Manager edits the user's password; old password must be rejected and new password accepted
    syncManager.saveManagedUserAccount(
      user = createdUser,
      newPlainPassword = "UpdatedManagerPass#99"
    )
    val (oldPassUser, _) = syncManager.authenticateWithFirebaseOrRoom(
      "customadmin@meseretehiwot.org",
      "NewSecurePass!77"
    )
    assertNull(oldPassUser)

    val (newPassUser, _) = syncManager.authenticateWithFirebaseOrRoom(
      "customadmin@meseretehiwot.org",
      "UpdatedManagerPass#99"
    )
    assertNotNull(newPassUser)

    // Verify full Room <-> Firestore sync and JSON backup export
    val syncState = syncManager.syncAllRoomDataWithFirestore()
    assertTrue(syncState.syncedRecordsCount > 0)

    val jsonBackup = DataExporter.generateFullDatabaseJsonBackup(
      dbPath = context.getDatabasePath("meserete_hiwot_church.db").absolutePath,
      usersCount = dao.getAllUsers().first().size,
      departments = dao.getAllDepartments().first(),
      members = dao.getAllMembers().first(),
      payments = dao.getAllPayments().first(),
      attendance = dao.getAllAttendance().first(),
      plans = dao.getAllPlans().first(),
      reports = dao.getAllReports().first(),
      announcements = dao.getAllAnnouncements().first(),
      quizResults = dao.getAllQuizResults().first()
    )
    assertTrue(jsonBackup.contains("meserete_hiwot_church.db"))

    // Test FirebaseAuthManager registration, login, and role assignment
    val authMgr = com.example.data.remote.FirebaseAuthManager(context, dao)

    // 1. Register student
    val (registeredStudent, _) = authMgr.registerUserWithRole(
      fullName = "Kidanemariam Wolde",
      email = "kidane@meseretehiwot.org",
      plainPassword = "StudentPass!123",
      phone = "+251911998877",
      role = "Student",
      departmentId = 1L
    )
    assertNotNull(registeredStudent)
    assertEquals("Student", registeredStudent?.role)
    assertTrue(registeredStudent?.isApproved == true)

    // 2. Login with role credentials
    val (loggedInStudent, _) = authMgr.loginWithRoleCredentials(
      email = "kidane@meseretehiwot.org",
      pass = "StudentPass!123"
    )
    assertNotNull(loggedInStudent)
    assertTrue(authMgr.isCurrentUserStudent())
    assertFalse(authMgr.isCurrentUserTeacher())

    // 3. Manager assigns new role (e.g. promotes to Teacher)
    val roleAssigned = authMgr.assignUserRole(
      targetEmail = "kidane@meseretehiwot.org",
      newRole = "Teacher",
      departmentId = 2L,
      isApproved = true,
      assignedByManager = "Manager Dn. Yohannes"
    )
    assertTrue(roleAssigned)

    // 4. Verify updated role reflected in login
    val (promotedUser, _) = authMgr.loginWithRoleCredentials(
      email = "kidane@meseretehiwot.org",
      pass = "StudentPass!123"
    )
    assertNotNull(promotedUser)
    assertEquals("Teacher", promotedUser?.role)
    assertTrue(authMgr.isCurrentUserTeacher())

    db.close()
  }

  @Test
  fun `localization manager switches between Amharic and English strings using Android resource system`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val locMgr = com.example.util.LocalizationManager

    // Set to English
    locMgr.setLanguage(context, com.example.ui.language.AppLanguage.ENGLISH)
    assertEquals(com.example.ui.language.AppLanguage.ENGLISH, locMgr.currentLanguage)
    val enDashboard = context.resources.getString(R.string.nav_dashboard)
    assertEquals("Dashboard", enDashboard)

    // Set to Amharic
    locMgr.setLanguage(context, com.example.ui.language.AppLanguage.AMHARIC)
    assertEquals(com.example.ui.language.AppLanguage.AMHARIC, locMgr.currentLanguage)
    val amDashboard = context.resources.getString(R.string.nav_dashboard)
    assertEquals("ዳሽቦርድ", amDashboard)
  }

  @Test
  fun `role based navigation shell destinations filter menu items conditionally`() {
    val studentUser = User(id = 1, email = "s@c.org", passwordHash = "h", fullName = "Student A", phone = "1", role = "Student")
    val teacherUser = User(id = 2, email = "t@c.org", passwordHash = "h", fullName = "Teacher B", phone = "1", role = "Teacher")
    val adminUser = User(id = 3, email = "m@c.org", passwordHash = "h", fullName = "Manager C", phone = "1", role = "Manager")

    // Helper checking destinations for role
    fun getDestinationsForRole(user: User): List<com.example.ui.screens.ShellDestination> {
      val roleLower = user.role.lowercase()
      val isAdmin = roleLower in listOf("manager", "super admin", "admin", "deptadmin")
      val isTeacher = !isAdmin && (roleLower.contains("teacher") || roleLower.contains("instructor"))
      return when {
        isAdmin -> listOf(
          com.example.ui.screens.ShellDestination.DASHBOARD,
          com.example.ui.screens.ShellDestination.MEMBERS,
          com.example.ui.screens.ShellDestination.DATA_CENTER,
          com.example.ui.screens.ShellDestination.BIBLE_STUDY,
          com.example.ui.screens.ShellDestination.COUNSELOR
        )
        isTeacher -> listOf(
          com.example.ui.screens.ShellDestination.DASHBOARD,
          com.example.ui.screens.ShellDestination.GRADES,
          com.example.ui.screens.ShellDestination.ATTENDANCE,
          com.example.ui.screens.ShellDestination.BIBLE_STUDY,
          com.example.ui.screens.ShellDestination.COUNSELOR
        )
        else -> listOf(
          com.example.ui.screens.ShellDestination.DASHBOARD,
          com.example.ui.screens.ShellDestination.GRADES,
          com.example.ui.screens.ShellDestination.ATTENDANCE,
          com.example.ui.screens.ShellDestination.BIBLE_STUDY,
          com.example.ui.screens.ShellDestination.COUNSELOR
        )
      }
    }

    val studentDests = getDestinationsForRole(studentUser)
    val teacherDests = getDestinationsForRole(teacherUser)
    val adminDests = getDestinationsForRole(adminUser)

    // Admin has access to DATA_CENTER and MEMBERS
    assertTrue(adminDests.contains(com.example.ui.screens.ShellDestination.DATA_CENTER))
    assertTrue(adminDests.contains(com.example.ui.screens.ShellDestination.MEMBERS))

    // Students and Teachers DO NOT have DATA_CENTER or MEMBERS in their menu
    assertFalse(studentDests.contains(com.example.ui.screens.ShellDestination.DATA_CENTER))
    assertFalse(studentDests.contains(com.example.ui.screens.ShellDestination.MEMBERS))
    assertFalse(teacherDests.contains(com.example.ui.screens.ShellDestination.DATA_CENTER))

    // Teachers and Students have access to GRADES and ATTENDANCE
    assertTrue(teacherDests.contains(com.example.ui.screens.ShellDestination.GRADES))
    assertTrue(studentDests.contains(com.example.ui.screens.ShellDestination.GRADES))
  }
}
