package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SundaySchoolDao {
  // Users
  @Query("SELECT * FROM users ORDER BY fullName ASC")
  fun getAllUsers(): Flow<List<User>>

  @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
  suspend fun getUserByEmail(email: String): User?

  @Query("SELECT * FROM users WHERE id = :id")
  suspend fun getUserById(id: Long): User?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: User): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<User>)

  @Update
  suspend fun updateUser(user: User)

  @Query("DELETE FROM users WHERE id = :id")
  suspend fun deleteUser(id: Long)

  @Query("UPDATE users SET isApproved = :isApproved WHERE id = :userId")
  suspend fun updateAdminApproval(userId: Long, isApproved: Boolean)

  @Query("UPDATE users SET isApproved = :isApproved, approvalStatus = :approvalStatus, approvedBy = :approvedBy, approvedAt = :approvedAt WHERE id = :userId")
  suspend fun updateAdminApprovalStatus(userId: Long, isApproved: Boolean, approvalStatus: String, approvedBy: String, approvedAt: Long)

  // Departments
  @Query("SELECT * FROM departments ORDER BY id ASC")
  fun getAllDepartments(): Flow<List<Department>>

  @Query("SELECT * FROM departments WHERE id = :id")
  suspend fun getDepartmentById(id: Long): Department?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDepartments(departments: List<Department>)

  @Update
  suspend fun updateDepartment(department: Department)

  // Members
  @Query("SELECT * FROM members ORDER BY fullName ASC")
  fun getAllMembers(): Flow<List<MemberItem>>

  @Query("SELECT * FROM members WHERE departmentId = :deptId ORDER BY fullName ASC")
  fun getMembersByDepartment(deptId: Long): Flow<List<MemberItem>>

  @Query("SELECT * FROM members WHERE memberId = :memberId LIMIT 1")
  suspend fun getMemberByMemberId(memberId: String): MemberItem?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMember(member: MemberItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMembers(members: List<MemberItem>)

  @Update
  suspend fun updateMember(member: MemberItem)

  @Query("UPDATE members SET uniformAccessApproved = :approved WHERE memberId = :memberId")
  suspend fun updateMemberUniformAccess(memberId: String, approved: Boolean)

  @Query("DELETE FROM members WHERE id = :id")
  suspend fun deleteMember(id: Long)

  // Payments
  @Query("SELECT * FROM payments ORDER BY id DESC")
  fun getAllPayments(): Flow<List<PaymentItem>>

  @Query("SELECT * FROM payments WHERE memberId = :memberId ORDER BY id DESC")
  fun getPaymentsByMember(memberId: String): Flow<List<PaymentItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayment(payment: PaymentItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayments(payments: List<PaymentItem>)

  @Update
  suspend fun updatePayment(payment: PaymentItem)

  @Query("DELETE FROM payments WHERE id = :id")
  suspend fun deletePayment(id: Long)

  @Query("UPDATE payments SET verified = 1, verifiedBy = :verifiedBy, verificationDate = :verificationDate, receiptNumber = :receiptNo WHERE id = :paymentId")
  suspend fun verifyPayment(paymentId: Long, verifiedBy: String, verificationDate: String, receiptNo: String)

  // Payment Settings
  @Query("SELECT * FROM payment_settings")
  fun getAllPaymentSettings(): Flow<List<PaymentSetting>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPaymentSettings(settings: List<PaymentSetting>)

  @Update
  suspend fun updatePaymentSetting(setting: PaymentSetting)

  // Attendance
  @Query("SELECT * FROM attendance ORDER BY id DESC")
  fun getAllAttendance(): Flow<List<AttendanceRecord>>

  @Query("SELECT * FROM attendance WHERE memberId = :memberId ORDER BY id DESC")
  fun getAttendanceByMember(memberId: String): Flow<List<AttendanceRecord>>

  @Query("SELECT * FROM attendance WHERE date = :date ORDER BY id DESC")
  fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>>

  @Query("SELECT * FROM attendance WHERE memberId = :memberId AND date = :date AND serviceType = :serviceType LIMIT 1")
  suspend fun getAttendanceForMemberDateService(memberId: String, date: String, serviceType: String): AttendanceRecord?

  @Query("UPDATE attendance SET status = :status, notes = :notes WHERE id = :id")
  suspend fun updateAttendanceStatus(id: Long, status: String, notes: String)

  @Query("DELETE FROM attendance WHERE id = :id")
  suspend fun deleteAttendance(id: Long)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAttendance(record: AttendanceRecord): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAttendanceList(records: List<AttendanceRecord>)

  // Plans
  @Query("SELECT * FROM plans ORDER BY id DESC")
  fun getAllPlans(): Flow<List<PlanItem>>

  @Query("SELECT * FROM plans WHERE departmentId = :deptId ORDER BY id DESC")
  fun getPlansByDepartment(deptId: Long): Flow<List<PlanItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlan(plan: PlanItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlans(plans: List<PlanItem>)

  @Update
  suspend fun updatePlan(plan: PlanItem)

  @Query("DELETE FROM plans WHERE id = :id")
  suspend fun deletePlan(id: Long)

  @Query("UPDATE plans SET status = :status, approvedBy = :approvedBy WHERE id = :planId")
  suspend fun updatePlanStatus(planId: Long, status: String, approvedBy: String)

  // Reports
  @Query("SELECT * FROM reports ORDER BY id DESC")
  fun getAllReports(): Flow<List<ReportItem>>

  @Query("SELECT * FROM reports WHERE departmentId = :deptId ORDER BY id DESC")
  fun getReportsByDepartment(deptId: Long): Flow<List<ReportItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReport(report: ReportItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReports(reports: List<ReportItem>)

  @Query("UPDATE reports SET status = :status, reviewedBy = :reviewedBy, comments = :comments WHERE id = :reportId")
  suspend fun reviewReport(reportId: Long, status: String, reviewedBy: String, comments: String)

  // Announcements
  @Query("SELECT * FROM announcements ORDER BY isPinned DESC, id DESC")
  fun getAllAnnouncements(): Flow<List<AnnouncementItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAnnouncement(announcement: AnnouncementItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAnnouncements(announcements: List<AnnouncementItem>)

  @Update
  suspend fun updateAnnouncement(announcement: AnnouncementItem)

  @Query("DELETE FROM announcements WHERE id = :id")
  suspend fun deleteAnnouncement(id: Long)

  // Messages
  @Query("SELECT * FROM messages ORDER BY id DESC")
  fun getAllMessages(): Flow<List<MessageItem>>

  @Query("SELECT * FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY id DESC")
  fun getMessagesForUser(userId: String): Flow<List<MessageItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: MessageItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessages(messages: List<MessageItem>)

  // Feedback
  @Query("SELECT * FROM feedback ORDER BY id DESC")
  fun getAllFeedback(): Flow<List<FeedbackItem>>

  @Query("SELECT * FROM feedback WHERE submittedBy = :userId ORDER BY id DESC")
  fun getFeedbackForUser(userId: String): Flow<List<FeedbackItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFeedback(feedback: FeedbackItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFeedbackList(feedbackList: List<FeedbackItem>)

  @Query("UPDATE feedback SET status = :status, handledBy = :handledBy, response = :response WHERE id = :feedbackId")
  suspend fun respondToFeedback(feedbackId: Long, status: String, handledBy: String, response: String)

  // Grades
  @Query("SELECT * FROM grades ORDER BY id DESC")
  fun getAllGrades(): Flow<List<GradeItem>>

  @Query("SELECT * FROM grades WHERE studentId = :studentId ORDER BY id DESC")
  fun getGradesByStudent(studentId: String): Flow<List<GradeItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGrade(grade: GradeItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGrades(grades: List<GradeItem>)

  // Hymns
  @Query("SELECT * FROM hymns ORDER BY title ASC")
  fun getAllHymns(): Flow<List<HymnItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHymn(hymn: HymnItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHymns(hymns: List<HymnItem>)

  // Documents
  @Query("SELECT * FROM documents ORDER BY id DESC")
  fun getAllDocuments(): Flow<List<DocumentItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocument(document: DocumentItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocuments(documents: List<DocumentItem>)

  // Bible Stories (Offline cached content)
  @Query("SELECT * FROM bible_stories ORDER BY dayNumber ASC")
  fun getAllBibleStories(): Flow<List<BibleStory>>

  @Query("SELECT * FROM bible_stories WHERE dayNumber = :day LIMIT 1")
  suspend fun getBibleStoryByDay(day: Int): BibleStory?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBibleStories(stories: List<BibleStory>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBibleStory(story: BibleStory)

  // Interactive Bible Quiz Questions & Results
  @Query("SELECT * FROM bible_quiz_questions ORDER BY id ASC")
  fun getAllQuizQuestions(): Flow<List<BibleQuizQuestion>>

  @Query("SELECT * FROM bible_quiz_questions WHERE storyId = :storyId ORDER BY id ASC")
  fun getQuestionsForStory(storyId: String): Flow<List<BibleQuizQuestion>>

  @Query("SELECT COUNT(*) FROM bible_quiz_questions")
  suspend fun getQuizQuestionsCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQuizQuestions(questions: List<BibleQuizQuestion>)

  @Query("SELECT * FROM quiz_results ORDER BY completedAt DESC, id DESC")
  fun getAllQuizResults(): Flow<List<QuizResult>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQuizResult(result: QuizResult): Long

  // Monthly Attendance queries
  @Query("SELECT * FROM attendance WHERE date LIKE :monthPattern ORDER BY date DESC, id DESC")
  fun getAttendanceByMonth(monthPattern: String): Flow<List<AttendanceRecord>>

  // Weekly Lesson Notes
  @Query("SELECT * FROM lesson_notes ORDER BY weekNumber DESC, id DESC")
  fun getAllLessonNotes(): Flow<List<LessonNote>>

  @Query("SELECT * FROM lesson_notes WHERE classGrade = :grade OR classGrade = 'All Classes' ORDER BY weekNumber DESC, id DESC")
  fun getLessonNotesForGrade(grade: String): Flow<List<LessonNote>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLessonNote(note: LessonNote): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLessonNotes(notes: List<LessonNote>)

  @Query("DELETE FROM lesson_notes WHERE id = :id")
  suspend fun deleteLessonNote(id: Long)
}
