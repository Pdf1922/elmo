package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.remote.CloudSyncState
import com.example.data.remote.FirebaseAuthManager
import com.example.data.remote.FirebaseCloudSyncManager
import com.example.data.repository.SundaySchoolRepository
import com.example.util.SecuredAction
import com.example.util.SecurityManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SundaySchoolViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: SundaySchoolRepository
  private val cloudSyncManager: FirebaseCloudSyncManager
  val authManager: FirebaseAuthManager
  private val prefs = application.getSharedPreferences("church_app_prefs", Context.MODE_PRIVATE)

  private val _customChurchBackgroundUri = MutableStateFlow<String?>(
    prefs.getString("custom_church_bg_uri", null)
  )
  val customChurchBackgroundUri: StateFlow<String?> = _customChurchBackgroundUri.asStateFlow()

  val cloudSyncState: StateFlow<CloudSyncState>

  init {
    val db = AppDatabase.getDatabase(application)
    val dao = db.sundaySchoolDao()
    repository = SundaySchoolRepository(dao)
    cloudSyncManager = FirebaseCloudSyncManager(application, dao)
    authManager = FirebaseAuthManager(application, dao)
    cloudSyncState = cloudSyncManager.syncState
    viewModelScope.launch {
      repository.initializeSeedDataIfNeeded()
      cloudSyncManager.syncAllRoomDataWithFirestore()
    }
  }

  fun setCustomChurchBackgroundUri(uri: String?) {
    _customChurchBackgroundUri.value = uri
    prefs.edit().apply {
      if (uri == null) remove("custom_church_bg_uri") else putString("custom_church_bg_uri", uri)
      apply()
    }
  }

  // Auth & Session State
  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  val allUsers = repository.allUsers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val departments = repository.allDepartments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val members = repository.allMembers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val payments = repository.allPayments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val paymentSettings = repository.allPaymentSettings
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val attendance = repository.allAttendance
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val lessonNotes = repository.allLessonNotes
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val plans = repository.allPlans
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val reports = repository.allReports
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val announcements = repository.allAnnouncements
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val messages = repository.allMessages
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val feedbackList = repository.allFeedback
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val grades = repository.allGrades
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val hymns = repository.allHymns
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val documents = repository.allDocuments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val bibleStories = repository.allBibleStories
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val quizQuestions = repository.allQuizQuestions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val quizResults = repository.allQuizResults
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Authentication operations
  fun loginAs(user: User) {
    _currentUser.value = user
  }

  fun login(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val (authenticatedUser, errorMsg) = authManager.loginWithRoleCredentials(email, pass)
      if (authenticatedUser != null) {
        _currentUser.value = authenticatedUser
        cloudSyncManager.syncAllRoomDataWithFirestore()
        onResult(true, "")
      } else {
        onResult(false, errorMsg)
      }
    }
  }

  fun loginWithGoogleCredentialManager(
    activityContext: Context,
    onResult: (Boolean, String) -> Unit
  ) {
    viewModelScope.launch {
      val (user, errorMsg) = authManager.signInWithGoogle(activityContext)
      if (user != null) {
        _currentUser.value = user
        cloudSyncManager.syncAllRoomDataWithFirestore()
        onResult(true, "")
      } else {
        onResult(false, errorMsg)
      }
    }
  }

  fun syncRoomWithCloudFirestore() {
    viewModelScope.launch {
      cloudSyncManager.syncAllRoomDataWithFirestore()
    }
  }

  fun saveManagedUserAccount(
    existingUser: User?,
    email: String,
    plainPassword: String,
    fullName: String,
    phone: String,
    role: String,
    departmentId: Long?,
    isApproved: Boolean,
    isActive: Boolean = true,
    onComplete: (Boolean, String) -> Unit = { _, _ -> }
  ) {
    viewModelScope.launch {
      val cleanEmail = SecurityManager.sanitizeInput(email).lowercase()
      if (cleanEmail.isBlank() || fullName.isBlank()) {
        onComplete(false, "Email and Full Name are required / ኢሜይል እና ሙሉ ስም ያስፈልጋል")
        return@launch
      }
      val baseUser = existingUser?.copy(
        email = cleanEmail,
        fullName = SecurityManager.sanitizeInput(fullName),
        phone = SecurityManager.sanitizeInput(phone),
        role = role,
        departmentId = if (role == "DeptAdmin" || role == "Member") departmentId else null,
        isApproved = isApproved,
        approvalStatus = if (isApproved) "APPROVED" else "PENDING",
        isActive = isActive
      ) ?: User(
        email = cleanEmail,
        passwordHash = SecurityManager.hashPassword(plainPassword.ifBlank { "changeme123" }),
        fullName = SecurityManager.sanitizeInput(fullName),
        phone = SecurityManager.sanitizeInput(phone),
        role = role,
        departmentId = if (role == "DeptAdmin" || role == "Member") departmentId else null,
        isApproved = isApproved,
        approvalStatus = if (isApproved) "APPROVED" else "PENDING",
        approvedBy = _currentUser.value?.fullName ?: "Executive Manager",
        isActive = isActive
      )

      val saved = cloudSyncManager.saveManagedUserAccount(
        user = baseUser,
        newPlainPassword = plainPassword.takeIf { it.isNotBlank() }
      )
      if (_currentUser.value?.id == saved.id) {
        _currentUser.value = saved
      }
      cloudSyncManager.syncAllRoomDataWithFirestore()
      onComplete(true, "Account saved & synced with Firebase / መለያው ተቀምጧል")
    }
  }

  fun deleteManagedUser(userId: Long) {
    viewModelScope.launch {
      repository.deleteUser(userId)
      cloudSyncManager.syncAllRoomDataWithFirestore()
    }
  }

  fun registerNewUserAndMember(
    fullName: String,
    phone: String,
    email: String,
    plainPassword: String,
    role: String,
    departmentId: Long,
    gender: String = "Male",
    category: String = "Student",
    classGrade: String = "Grade 9-12 / 9-12ኛ ክፍል",
    confessorFather: String = "",
    address: String = "Addis Ababa",
    autoLogin: Boolean = true,
    onComplete: (Boolean, String, User?) -> Unit = { _, _, _ -> }
  ) {
    viewModelScope.launch {
      val (savedUser, errorMsg) = authManager.registerUserWithRole(
        fullName = fullName,
        email = email,
        plainPassword = plainPassword,
        phone = phone,
        role = role,
        departmentId = departmentId,
        category = category,
        classGrade = classGrade,
        gender = gender,
        confessorFather = confessorFather,
        address = address
      )
      if (savedUser != null) {
        cloudSyncManager.syncAllRoomDataWithFirestore()
        if (autoLogin) {
          _currentUser.value = savedUser
        }
        onComplete(true, "Registered & synchronized with Firebase Auth & Cloud Firestore!", savedUser)
      } else {
        onComplete(false, errorMsg.ifBlank { "Registration failed" }, null)
      }
    }
  }

  fun assignUserRole(
    targetEmail: String,
    newRole: String,
    departmentId: Long?,
    isApproved: Boolean,
    onComplete: (Boolean) -> Unit = {}
  ) {
    viewModelScope.launch {
      val success = authManager.assignUserRole(
        targetEmail = targetEmail,
        newRole = newRole,
        departmentId = departmentId,
        isApproved = isApproved,
        assignedByManager = _currentUser.value?.fullName ?: "Manager"
      )
      cloudSyncManager.syncAllRoomDataWithFirestore()
      onComplete(success)
    }
  }

  fun logout() {
    authManager.signOut()
    cloudSyncManager.signOutFirebase()
    _currentUser.value = null
  }

  // Payment Actions
  fun verifyPayment(paymentId: Long, receiptNumber: String) {
    val verifierName = _currentUser.value?.fullName ?: "Finance Admin"
    val date = "2024-09-26"
    viewModelScope.launch {
      repository.verifyPayment(paymentId, verifierName, date, receiptNumber)
    }
  }

  fun recordPayment(
    memberId: String,
    memberName: String,
    category: String,
    amountPaid: Double,
    month: String,
    method: String,
    receiptNo: String,
    notes: String
  ) {
    val verifier = _currentUser.value?.fullName ?: ""
    val isVerified = verifier.isNotEmpty()
    val payment = PaymentItem(
      paymentId = "PAY-${System.currentTimeMillis() % 10000}",
      memberId = memberId,
      memberName = memberName,
      memberCategory = category,
      amountDue = if (category == "Worker") 100.0 else if (category == "StudentWorker") 50.0 else 20.0,
      amountPaid = amountPaid,
      paymentMonth = month,
      paymentYear = 2019,
      paymentDate = "2024-09-26",
      paymentMethod = method,
      verifiedBy = if (isVerified) verifier else "",
      verified = isVerified,
      verificationDate = if (isVerified) "2024-09-26" else "",
      receiptNumber = receiptNo,
      notes = notes
    )
    viewModelScope.launch {
      repository.insertPayment(payment)
    }
  }

  fun updatePayment(payment: PaymentItem) {
    viewModelScope.launch {
      repository.updatePayment(payment)
    }
  }

  fun deletePayment(id: Long) {
    viewModelScope.launch {
      repository.deletePayment(id)
    }
  }

  fun updatePaymentSetting(category: String, newAmount: Double) {
    viewModelScope.launch {
      val existing = paymentSettings.value.find { it.category == category }
      if (existing != null) {
        repository.updatePaymentSetting(
          existing.copy(amount = newAmount, updatedAt = System.currentTimeMillis())
        )
      }
    }
  }

  // Member Actions
  fun addMember(
    fullName: String,
    phone: String,
    email: String,
    gender: String,
    category: String,
    classGrade: String,
    deptId: Long,
    confessorFather: String,
    address: String
  ) {
    val member = MemberItem(
      memberId = "MH-2019-${(100..999).random()}",
      fullName = SecurityManager.sanitizeInput(fullName),
      gender = SecurityManager.sanitizeInput(gender),
      phone = SecurityManager.sanitizeInput(phone),
      email = SecurityManager.sanitizeInput(email),
      address = SecurityManager.sanitizeInput(address),
      memberCategory = SecurityManager.sanitizeInput(category),
      classGrade = SecurityManager.sanitizeInput(classGrade),
      departmentId = deptId,
      confessorFather = SecurityManager.sanitizeInput(confessorFather),
      joinDate = "2024-09-26",
      status = "Active"
    )
    viewModelScope.launch {
      repository.insertMember(member)
    }
  }

  fun updateMember(member: MemberItem) {
    viewModelScope.launch {
      repository.updateMember(member)
    }
  }

  fun deleteMember(id: Long) {
    viewModelScope.launch {
      repository.deleteMember(id)
    }
  }

  // Department Actions
  fun updateDepartment(department: Department) {
    viewModelScope.launch {
      repository.updateDepartment(department)
    }
  }

  // Admin Approval Actions (Manager controls department admin permissions)
  fun toggleAdminApproval(userId: Long, isApproved: Boolean) {
    viewModelScope.launch {
      repository.updateAdminApproval(userId, isApproved)
      // If current user is updated, refresh local state
      if (_currentUser.value?.id == userId) {
        _currentUser.value = _currentUser.value?.copy(isApproved = isApproved)
      }
    }
  }

  fun updateUser(user: User) {
    viewModelScope.launch {
      repository.updateUser(user)
      if (_currentUser.value?.id == user.id) {
        _currentUser.value = user
      }
    }
  }

  // Song Department Attendance & Holiday Uniform Access
  fun recordSongAttendance(
    memberId: String,
    memberName: String,
    status: String,
    serviceType: String = "Choir Rehearsal / የመዝሙር ልምምድ"
  ) {
    val recorder = _currentUser.value?.fullName ?: "Song Admin"
    val record = AttendanceRecord(
      memberId = memberId,
      memberName = memberName,
      date = "2024-09-26",
      serviceType = serviceType,
      status = status,
      recordedBy = recorder,
      notes = "Tracked by Song & Arts Dept"
    )
    viewModelScope.launch {
      repository.insertAttendance(record)
    }
  }

  fun setAdminApprovalStatus(userId: Long, isApproved: Boolean, status: String) {
    val managerName = _currentUser.value?.fullName ?: "Executive Manager"
    viewModelScope.launch {
      repository.updateAdminApprovalStatus(
        userId = userId,
        isApproved = isApproved,
        approvalStatus = status,
        approvedBy = managerName,
        approvedAt = System.currentTimeMillis()
      )
    }
  }

  fun toggleMemberUniformAccess(memberId: String, hasAccess: Boolean) {
    viewModelScope.launch {
      repository.updateMemberUniformAccess(memberId, hasAccess)
    }
  }

  // Live Choir & Member Attendance Toggling with Room Database Sync
  fun toggleAttendanceStatus(
    memberId: String,
    memberName: String,
    date: String,
    serviceType: String,
    newStatus: String
  ) {
    val recorder = _currentUser.value?.fullName ?: "Song Dept Admin"
    viewModelScope.launch {
      val existing = repository.getAttendanceForMemberDateService(memberId, date, serviceType)
      if (existing != null) {
        repository.updateAttendanceStatus(existing.id, newStatus, existing.notes)
      } else {
        repository.insertAttendance(
          AttendanceRecord(
            memberId = memberId,
            memberName = memberName,
            date = date,
            serviceType = serviceType,
            status = newStatus,
            recordedBy = recorder,
            notes = "Recorded via Song Dept Attendance Register"
          )
        )
      }
    }
  }

  fun markAllPresent(members: List<MemberItem>, date: String, serviceType: String) {
    val recorder = _currentUser.value?.fullName ?: "Song Dept Admin"
    viewModelScope.launch {
      members.forEach { m ->
        val existing = repository.getAttendanceForMemberDateService(m.memberId, date, serviceType)
        if (existing != null) {
          repository.updateAttendanceStatus(existing.id, "Present", existing.notes)
        } else {
          repository.insertAttendance(
            AttendanceRecord(
              memberId = m.memberId,
              memberName = m.fullName,
              date = date,
              serviceType = serviceType,
              status = "Present",
              recordedBy = recorder,
              notes = "Batch verified present"
            )
          )
        }
      }
    }
  }

  fun publishHolidayUniformAnnouncement(
    holidayName: String,
    qualifiedNames: List<String>,
    pickupDetails: String = "ቅዳሜ ከቀኑ 8:00 ሰዓት ጀምሮ በሰንበት ት/ቤቱ አዳራሽ"
  ) {
    val author = _currentUser.value?.fullName ?: "Song & Arts Dept / የመዝሙርና ሥነ-ጥበብ ክፍል"
    val namesFormatted = if (qualifiedNames.isNotEmpty()) {
      qualifiedNames.mapIndexed { idx, name -> "${idx + 1}. $name" }.joinToString("\n")
    } else {
      "ሁሉም በልምምድ የተገኙ መዘምራን"
    }

    val announcement = AnnouncementItem(
      titleAm = "የበዓለ ንግሥ የአገልግሎት ልብስ የተፈቀደላቸው መዘምራን ዝርዝር ($holidayName)",
      titleEn = "Approved Choir Service Uniform Authorization List ($holidayName)",
      bodyAm = "በመዝሙርና ሥነ ጥበብ ክፍል የልምምድ ክትትልና የሰዓት አክባሪነት መሠረት ለመጪው $holidayName ዝማሬ አገልግሎት ልብስ ወስደው በመድረክ እንዲያገለግሉ የተፈቀደላቸው መዘምራን ዝርዝር የሚከተሉት ናቸው፦\n\n$namesFormatted\n\nልብስ የመረከቢያ ጊዜ፦ $pickupDetails።\nእንኳን ለበዓሉ በሰላም አደረሳችሁ!",
      bodyEn = "Based on choir attendance and rehearsal commitment for $holidayName, the following choir members are authorized to receive Sunday School service vestments:\n\n${qualifiedNames.joinToString(", ")}\n\nDistribution time & venue: $pickupDetails. God bless your holy service!",
      postedBy = author,
      targetAudience = "All Members",
      datePosted = "2024-09-26",
      isPinned = true
    )
    viewModelScope.launch {
      repository.insertAnnouncement(announcement)
    }
  }

  // Attendance Actions
  fun recordAttendance(
    memberId: String,
    memberName: String,
    status: String,
    serviceType: String,
    notes: String = "",
    date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  ) {
    val recorder = _currentUser.value?.fullName ?: "Teacher"
    val record = AttendanceRecord(
      memberId = memberId,
      memberName = memberName,
      date = date,
      serviceType = serviceType,
      status = status,
      recordedBy = recorder,
      notes = notes
    )
    viewModelScope.launch {
      val id = repository.insertAttendance(record)
      syncSingleAttendanceToFirestore(record.copy(id = id))
    }
  }

  fun recordBatchAttendance(
    records: List<AttendanceRecord>,
    onComplete: (Boolean) -> Unit = {}
  ) {
    viewModelScope.launch {
      for (rec in records) {
        val id = repository.insertAttendance(rec)
        syncSingleAttendanceToFirestore(rec.copy(id = id))
      }
      onComplete(true)
    }
  }

  private suspend fun syncSingleAttendanceToFirestore(record: AttendanceRecord) {
    try {
      val db = FirebaseFirestore.getInstance(getApplication<Application>().getString(com.example.R.string.firestore_database_id))
      val docId = "ATT_${record.date}_${record.memberId}_${record.id}"
      db.collection("church_attendance").document(docId).set(
        mapOf(
          "id" to record.id,
          "memberId" to record.memberId,
          "memberName" to record.memberName,
          "date" to record.date,
          "serviceType" to record.serviceType,
          "status" to record.status,
          "recordedBy" to record.recordedBy,
          "notes" to record.notes,
          "updatedAt" to System.currentTimeMillis()
        ),
        SetOptions.merge()
      )
    } catch (_: Exception) {}
  }

  // Weekly Lesson Notes Actions
  fun postWeeklyLessonNote(
    title: String,
    titleAmharic: String = "",
    subject: String,
    classGrade: String,
    weekNumber: Int,
    scriptureReference: String,
    summary: String,
    memoryVerse: String = "",
    onComplete: (Boolean) -> Unit = {}
  ) {
    val teacher = _currentUser.value?.fullName ?: "Teacher"
    val email = _currentUser.value?.email ?: "teacher@meseretehiwot.org"
    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val lessonId = "LESSON-${System.currentTimeMillis()}"
    val note = LessonNote(
      lessonId = lessonId,
      title = title,
      titleAmharic = titleAmharic,
      subject = subject,
      classGrade = classGrade,
      weekNumber = weekNumber,
      datePosted = dateStr,
      scriptureReference = scriptureReference,
      summary = summary,
      memoryVerse = memoryVerse,
      teacherName = teacher,
      teacherEmail = email
    )
    viewModelScope.launch {
      repository.insertLessonNote(note)
      syncLessonNoteToFirestore(note)
      onComplete(true)
    }
  }

  private suspend fun syncLessonNoteToFirestore(note: LessonNote) {
    try {
      val db = FirebaseFirestore.getInstance(getApplication<Application>().getString(com.example.R.string.firestore_database_id))
      db.collection("church_lessons").document(note.lessonId).set(
        mapOf(
          "lessonId" to note.lessonId,
          "title" to note.title,
          "titleAmharic" to note.titleAmharic,
          "subject" to note.subject,
          "classGrade" to note.classGrade,
          "weekNumber" to note.weekNumber,
          "datePosted" to note.datePosted,
          "scriptureReference" to note.scriptureReference,
          "summary" to note.summary,
          "memoryVerse" to note.memoryVerse,
          "teacherName" to note.teacherName,
          "teacherEmail" to note.teacherEmail,
          "updatedAt" to System.currentTimeMillis()
        ),
        SetOptions.merge()
      )
    } catch (_: Exception) {}
  }

  // Plan & Report Actions
  fun submitPlan(title: String, deptId: Long, deptName: String, target: String, measurement: String, budget: Double) {
    val plan = PlanItem(
      title = title,
      departmentId = deptId,
      deptName = deptName,
      startDate = "2024-10-01",
      endDate = "2024-12-30",
      target = target,
      measurement = measurement,
      budget = budget,
      status = "Pending"
    )
    viewModelScope.launch {
      repository.insertPlan(plan)
    }
  }

  fun updatePlan(plan: PlanItem) {
    viewModelScope.launch {
      repository.updatePlan(plan)
    }
  }

  fun deletePlan(id: Long) {
    viewModelScope.launch {
      repository.deletePlan(id)
    }
  }

  fun approvePlan(planId: Long) {
    val approver = _currentUser.value?.fullName ?: "Manager"
    viewModelScope.launch {
      repository.updatePlanStatus(planId, "Approved", approver)
    }
  }

  fun submitReport(title: String, deptId: Long, deptName: String, period: String, summary: String) {
    val submitter = _currentUser.value?.fullName ?: "Dept Admin"
    val report = ReportItem(
      title = title,
      departmentId = deptId,
      deptName = deptName,
      reportType = "Monthly",
      period = period,
      submittedBy = submitter,
      submittedDate = "2024-09-26",
      summary = summary,
      status = "Pending"
    )
    viewModelScope.launch {
      repository.insertReport(report)
    }
  }

  fun approveReport(reportId: Long, comments: String) {
    val reviewer = _currentUser.value?.fullName ?: "Manager"
    viewModelScope.launch {
      repository.reviewReport(reportId, "Approved", reviewer, comments)
    }
  }

  // Announcement Actions
  fun postAnnouncement(titleEn: String, titleAm: String, bodyEn: String, bodyAm: String, target: String, isPinned: Boolean) {
    val author = _currentUser.value?.fullName ?: "Office"
    val announcement = AnnouncementItem(
      titleEn = titleEn,
      titleAm = titleAm,
      bodyEn = bodyEn,
      bodyAm = bodyAm,
      postedBy = author,
      targetAudience = target,
      datePosted = "2024-09-26",
      isPinned = isPinned
    )
    viewModelScope.launch {
      repository.insertAnnouncement(announcement)
    }
  }

  fun updateAnnouncement(announcement: AnnouncementItem) {
    viewModelScope.launch {
      repository.updateAnnouncement(announcement)
    }
  }

  fun deleteAnnouncement(id: Long) {
    viewModelScope.launch {
      repository.deleteAnnouncement(id)
    }
  }

  // Feedback Actions
  fun submitFeedback(type: String, subject: String, message: String) {
    val user = _currentUser.value
    val feedback = FeedbackItem(
      submittedBy = user?.email ?: "anonymous",
      submitterName = user?.fullName ?: "Member",
      type = type,
      subject = subject,
      message = message,
      date = "2024-09-26",
      status = "Pending"
    )
    viewModelScope.launch {
      repository.insertFeedback(feedback)
    }
  }

  fun respondToFeedback(id: Long, response: String) {
    val handler = _currentUser.value?.fullName ?: "Admin"
    viewModelScope.launch {
      repository.respondToFeedback(id, "Resolved", handler, response)
    }
  }

  // Message Actions
  fun sendMessage(receiverId: String, subject: String, body: String) {
    val user = _currentUser.value
    val msg = MessageItem(
      senderId = user?.email ?: "me",
      senderName = user?.fullName ?: "Sender",
      receiverId = receiverId,
      subject = subject,
      body = body,
      dateSent = "2024-09-26 10:00"
    )
    viewModelScope.launch {
      repository.insertMessage(msg)
    }
  }

  // Grade Actions
  fun addGrade(
    studentId: String,
    studentName: String,
    subject: String,
    score: Double,
    comments: String = "",
    term: String = "Term 1",
    grade: String? = null,
    teacherName: String? = null
  ) {
    val teacher = teacherName ?: _currentUser.value?.fullName ?: "Teacher"
    val letter = grade ?: when {
      score >= 90 -> "A"
      score >= 80 -> "B"
      score >= 70 -> "C"
      else -> "D"
    }
    val gradeItem = GradeItem(
      studentId = studentId,
      studentName = studentName,
      subject = subject,
      term = term,
      score = score,
      grade = letter,
      teacherName = teacher,
      date = "2024-10-08",
      comments = comments
    )
    viewModelScope.launch {
      repository.insertGrade(gradeItem)
      cloudSyncManager.syncAllRoomDataWithFirestore()
    }
  }

  // Hymn Actions
  fun addHymn(title: String, category: String, choir: String, notes: String) {
    val hymn = HymnItem(
      title = title,
      category = category,
      choirGroup = choir,
      leadSinger = _currentUser.value?.fullName ?: "Lead",
      notes = notes
    )
    viewModelScope.launch {
      repository.insertHymn(hymn)
    }
  }

  // Quiz Actions
  fun submitQuizResult(
    storyId: String,
    storyTitle: String,
    participantName: String,
    score: Int,
    totalQuestions: Int,
    onCompleted: (QuizResult) -> Unit = {}
  ) {
    val percentage = if (totalQuestions > 0) (score.toDouble() / totalQuestions) * 100.0 else 0.0
    val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    val result = QuizResult(
      storyId = storyId,
      storyTitle = storyTitle,
      participantName = participantName.ifBlank { _currentUser.value?.fullName ?: "Student" },
      score = score,
      totalQuestions = totalQuestions,
      percentage = percentage,
      completedAt = System.currentTimeMillis(),
      formattedDate = formattedDate
    )
    viewModelScope.launch {
      val id = repository.insertQuizResult(result)
      onCompleted(result.copy(id = id))
    }
  }
}
