package com.example.data.repository

import com.example.data.local.PreloadedData
import com.example.data.local.SundaySchoolDao
import com.example.data.model.*
import com.example.util.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SundaySchoolRepository(private val dao: SundaySchoolDao) {

  suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
    val users = dao.getAllUsers().first()
    if (users.isEmpty()) {
      dao.insertDepartments(PreloadedData.departments)
      dao.insertUsers(PreloadedData.users)
      dao.insertPaymentSettings(PreloadedData.paymentSettings)
      dao.insertMembers(PreloadedData.members)
      dao.insertPayments(PreloadedData.payments)
      dao.insertAttendanceList(PreloadedData.attendanceRecords)
      dao.insertPlans(PreloadedData.plans)
      dao.insertReports(PreloadedData.reports)
      dao.insertAnnouncements(PreloadedData.announcements)
      dao.insertHymns(PreloadedData.hymns)
      dao.insertGrades(PreloadedData.grades)
      dao.insertDocuments(PreloadedData.documents)
      dao.insertFeedbackList(PreloadedData.feedbackList)
      dao.insertMessages(PreloadedData.messages)
      dao.insertBibleStories(BibleStoryRepository.dailyStories)
      dao.insertQuizQuestions(PreloadedData.bibleQuizQuestions)
      dao.insertLessonNotes(PreloadedData.lessonNotes)
    } else {
      // Ensure writer user exists even if database was created previously
      if (dao.getUserByEmail("writer@meseretehiwot.org") == null) {
        dao.insertUser(
          User(
            email = "writer@meseretehiwot.org",
            passwordHash = SecurityManager.hashPassword("writer123"),
            fullName = "Kidus Melaku / ኪዱስ መላኩ",
            phone = "+251911335577",
            role = "DeptAdmin",
            departmentId = 1,
            isApproved = true,
            approvalStatus = "APPROVED",
            approvedBy = "Executive Manager"
          )
        )
      }
      // Ensure quiz questions are populated if empty
      if (dao.getQuizQuestionsCount() == 0) {
        dao.insertQuizQuestions(PreloadedData.bibleQuizQuestions)
      }
    }
  }

  // Users & Auth
  val allUsers: Flow<List<User>> = dao.getAllUsers()
  suspend fun getUserByEmail(email: String): User? = dao.getUserByEmail(email)
  suspend fun getUserById(id: Long): User? = dao.getUserById(id)
  suspend fun insertUser(user: User): Long = dao.insertUser(user)
  suspend fun updateUser(user: User) = dao.updateUser(user)
  suspend fun deleteUser(id: Long) = dao.deleteUser(id)
  suspend fun updateAdminApproval(userId: Long, isApproved: Boolean) = dao.updateAdminApproval(userId, isApproved)
  suspend fun updateAdminApprovalStatus(userId: Long, isApproved: Boolean, approvalStatus: String, approvedBy: String, approvedAt: Long) =
    dao.updateAdminApprovalStatus(userId, isApproved, approvalStatus, approvedBy, approvedAt)

  // Departments
  val allDepartments: Flow<List<Department>> = dao.getAllDepartments()
  suspend fun getDepartmentById(id: Long): Department? = dao.getDepartmentById(id)
  suspend fun updateDepartment(department: Department) = dao.updateDepartment(department)

  // Members
  val allMembers: Flow<List<MemberItem>> = dao.getAllMembers()
  fun getMembersByDepartment(deptId: Long): Flow<List<MemberItem>> = dao.getMembersByDepartment(deptId)
  suspend fun insertMember(member: MemberItem): Long = dao.insertMember(member)
  suspend fun updateMember(member: MemberItem) = dao.updateMember(member)
  suspend fun updateMemberUniformAccess(memberId: String, approved: Boolean) = dao.updateMemberUniformAccess(memberId, approved)
  suspend fun deleteMember(id: Long) = dao.deleteMember(id)

  // Payments
  val allPayments: Flow<List<PaymentItem>> = dao.getAllPayments()
  fun getPaymentsByMember(memberId: String): Flow<List<PaymentItem>> = dao.getPaymentsByMember(memberId)
  suspend fun insertPayment(payment: PaymentItem): Long = dao.insertPayment(payment)
  suspend fun updatePayment(payment: PaymentItem) = dao.updatePayment(payment)
  suspend fun deletePayment(id: Long) = dao.deletePayment(id)
  suspend fun verifyPayment(paymentId: Long, verifiedBy: String, date: String, receipt: String) =
    dao.verifyPayment(paymentId, verifiedBy, date, receipt)

  // Payment Settings
  val allPaymentSettings: Flow<List<PaymentSetting>> = dao.getAllPaymentSettings()
  suspend fun updatePaymentSetting(setting: PaymentSetting) = dao.updatePaymentSetting(setting)

  // Attendance
  val allAttendance: Flow<List<AttendanceRecord>> = dao.getAllAttendance()
  fun getAttendanceByMember(memberId: String): Flow<List<AttendanceRecord>> = dao.getAttendanceByMember(memberId)
  fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>> = dao.getAttendanceByDate(date)
  suspend fun getAttendanceForMemberDateService(memberId: String, date: String, service: String): AttendanceRecord? =
    dao.getAttendanceForMemberDateService(memberId, date, service)
  suspend fun updateAttendanceStatus(id: Long, status: String, notes: String = "") =
    dao.updateAttendanceStatus(id, status, notes)
  suspend fun insertAttendance(record: AttendanceRecord): Long = dao.insertAttendance(record)
  suspend fun deleteAttendance(id: Long) = dao.deleteAttendance(id)

  // Bible Stories (Offline cached Room Database content)
  val allBibleStories: Flow<List<BibleStory>> = dao.getAllBibleStories()
  suspend fun getBibleStoryByDay(day: Int): BibleStory? = dao.getBibleStoryByDay(day)
  suspend fun insertBibleStories(stories: List<BibleStory>) = dao.insertBibleStories(stories)
  suspend fun insertBibleStory(story: BibleStory) = dao.insertBibleStory(story)

  // Plans & Reports
  val allPlans: Flow<List<PlanItem>> = dao.getAllPlans()
  fun getPlansByDepartment(deptId: Long): Flow<List<PlanItem>> = dao.getPlansByDepartment(deptId)
  suspend fun insertPlan(plan: PlanItem): Long = dao.insertPlan(plan)
  suspend fun updatePlan(plan: PlanItem) = dao.updatePlan(plan)
  suspend fun deletePlan(id: Long) = dao.deletePlan(id)
  suspend fun updatePlanStatus(planId: Long, status: String, approvedBy: String) =
    dao.updatePlanStatus(planId, status, approvedBy)

  val allReports: Flow<List<ReportItem>> = dao.getAllReports()
  fun getReportsByDepartment(deptId: Long): Flow<List<ReportItem>> = dao.getReportsByDepartment(deptId)
  suspend fun insertReport(report: ReportItem): Long = dao.insertReport(report)
  suspend fun reviewReport(reportId: Long, status: String, reviewedBy: String, comments: String) =
    dao.reviewReport(reportId, status, reviewedBy, comments)

  // Announcements
  val allAnnouncements: Flow<List<AnnouncementItem>> = dao.getAllAnnouncements()
  suspend fun insertAnnouncement(announcement: AnnouncementItem): Long = dao.insertAnnouncement(announcement)
  suspend fun updateAnnouncement(announcement: AnnouncementItem) = dao.updateAnnouncement(announcement)
  suspend fun deleteAnnouncement(id: Long) = dao.deleteAnnouncement(id)

  // Messages
  val allMessages: Flow<List<MessageItem>> = dao.getAllMessages()
  fun getMessagesForUser(userId: String): Flow<List<MessageItem>> = dao.getMessagesForUser(userId)
  suspend fun insertMessage(message: MessageItem): Long = dao.insertMessage(message)

  // Feedback
  val allFeedback: Flow<List<FeedbackItem>> = dao.getAllFeedback()
  fun getFeedbackForUser(userId: String): Flow<List<FeedbackItem>> = dao.getFeedbackForUser(userId)
  suspend fun insertFeedback(feedback: FeedbackItem): Long = dao.insertFeedback(feedback)
  suspend fun respondToFeedback(id: Long, status: String, handledBy: String, response: String) =
    dao.respondToFeedback(id, status, handledBy, response)

  // Grades
  val allGrades: Flow<List<GradeItem>> = dao.getAllGrades()
  fun getGradesByStudent(studentId: String): Flow<List<GradeItem>> = dao.getGradesByStudent(studentId)
  suspend fun insertGrade(grade: GradeItem): Long = dao.insertGrade(grade)

  // Hymns
  val allHymns: Flow<List<HymnItem>> = dao.getAllHymns()
  suspend fun insertHymn(hymn: HymnItem): Long = dao.insertHymn(hymn)

  // Documents
  val allDocuments: Flow<List<DocumentItem>> = dao.getAllDocuments()
  suspend fun insertDocument(document: DocumentItem): Long = dao.insertDocument(document)

  // Interactive Bible Quiz
  val allQuizQuestions: Flow<List<BibleQuizQuestion>> = dao.getAllQuizQuestions()
  fun getQuestionsForStory(storyId: String): Flow<List<BibleQuizQuestion>> = dao.getQuestionsForStory(storyId)
  val allQuizResults: Flow<List<QuizResult>> = dao.getAllQuizResults()
  suspend fun insertQuizResult(result: QuizResult): Long = dao.insertQuizResult(result)

  // Monthly Attendance queries
  fun getAttendanceByMonth(monthPattern: String): Flow<List<AttendanceRecord>> = dao.getAttendanceByMonth(monthPattern)

  // Weekly Lesson Notes
  val allLessonNotes: Flow<List<LessonNote>> = dao.getAllLessonNotes()
  fun getLessonNotesForGrade(grade: String): Flow<List<LessonNote>> = dao.getLessonNotesForGrade(grade)
  suspend fun insertLessonNote(note: LessonNote): Long = dao.insertLessonNote(note)
  suspend fun deleteLessonNote(id: Long) = dao.deleteLessonNote(id)
}
