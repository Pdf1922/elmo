package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val email: String,
  val passwordHash: String,
  val fullName: String,
  val phone: String,
  val role: String, // "Manager", "DeptAdmin", "Member"
  val departmentId: Long? = null,
  val profileImage: String = "",
  val isActive: Boolean = true,
  val isApproved: Boolean = true, // Department admins require manager approval
  val approvalStatus: String = "APPROVED", // "APPROVED", "PENDING", "REJECTED"
  val approvedBy: String = "Manager",
  val approvedAt: Long = 0L,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "departments")
data class Department(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val nameEn: String,
  val nameAm: String,
  val code: String,
  val descriptionEn: String,
  val descriptionAm: String,
  val headUserId: Long? = null,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "members")
data class MemberItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val memberId: String,
  val fullName: String,
  val gender: String, // "Male", "Female"
  val dob: String = "",
  val age: Int = 25,
  val phone: String,
  val email: String,
  val address: String,
  val photo: String = "",
  val memberType: String = "Regular", // "Regular", "Special", "Honorary"
  val memberCategory: String = "Worker", // "Worker", "StudentWorker", "NonWorker"
  val classGrade: String = "Class 3 - Zikre Kidusan",
  val departmentId: Long = 5, // HR department default
  val familyId: Long? = null,
  val joinDate: String = "2023-01-15",
  val isBaptized: Boolean = true,
  val confessorFather: String = "Kesis Melaku / ቀሲስ መላኩ",
  val status: String = "Active", // "Active", "Inactive", "Distanced"
  val uniformAccessApproved: Boolean = false, // Song department holiday uniform authorization
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val paymentId: String,
  val memberId: String,
  val memberName: String,
  val memberCategory: String, // "Worker", "StudentWorker", "NonWorker"
  val amountDue: Double,
  val amountPaid: Double,
  val paymentMonth: String, // "Meskerem / መስከረም", "Tikimt / ጥቅምት", etc.
  val paymentYear: Int = 2019,
  val paymentDate: String,
  val paymentMethod: String = "CBE Birr / Telebirr",
  val verifiedBy: String = "",
  val verified: Boolean = false,
  val verificationDate: String = "",
  val receiptNumber: String = "",
  val notes: String = ""
)

@Entity(tableName = "payment_settings")
data class PaymentSetting(
  @PrimaryKey val category: String, // "Worker", "StudentWorker", "NonWorker"
  val nameEn: String,
  val nameAm: String,
  val amount: Double,
  val effectiveFrom: String = "2019 E.C.",
  val updatedBy: String = "Manager",
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class AttendanceRecord(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val memberId: String,
  val memberName: String,
  val date: String,
  val serviceType: String, // "Sunday Liturgy", "Sunday School Class", "Rehearsal", "Holiday Service"
  val status: String, // "Present", "Absent", "Excused"
  val recordedBy: String,
  val notes: String = ""
)

@Entity(tableName = "plans")
data class PlanItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val departmentId: Long,
  val deptName: String,
  val year: Int = 2019,
  val startDate: String,
  val endDate: String,
  val target: String,
  val measurement: String,
  val weight: Int = 20,
  val budget: Double = 50000.0,
  val status: String = "Pending", // "Pending", "Approved", "InProgress", "Completed"
  val approvedBy: String = ""
)

@Entity(tableName = "reports")
data class ReportItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val departmentId: Long,
  val deptName: String,
  val reportType: String, // "Monthly", "Quarterly", "Annual"
  val period: String,
  val submittedBy: String,
  val submittedDate: String,
  val summary: String,
  val status: String = "Pending", // "Pending", "Reviewed", "Approved"
  val reviewedBy: String = "",
  val comments: String = ""
)

@Entity(tableName = "announcements")
data class AnnouncementItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val titleEn: String,
  val titleAm: String,
  val bodyEn: String,
  val bodyAm: String,
  val postedBy: String,
  val targetAudience: String = "All Members",
  val datePosted: String,
  val isPinned: Boolean = false
)

@Entity(tableName = "messages")
data class MessageItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val senderId: String,
  val senderName: String,
  val receiverId: String,
  val subject: String,
  val body: String,
  val dateSent: String,
  val isRead: Boolean = false
)

@Entity(tableName = "feedback")
data class FeedbackItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val submittedBy: String,
  val submitterName: String,
  val type: String, // "Suggestion", "Question", "Complaint"
  val subject: String,
  val message: String,
  val date: String,
  val status: String = "Pending", // "Pending", "Addressed", "Resolved"
  val handledBy: String = "",
  val response: String = ""
)

@Entity(tableName = "grades")
data class GradeItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val studentId: String,
  val studentName: String,
  val subject: String, // "Orthodox Dogma / ሃይማኖት", "Church History / የታሪክ ትምህርት", "Geez / ቋንቋ ግዕዝ", "Ethiopic Hymns / ዜማ"
  val term: String, // "Term 1", "Term 2"
  val score: Double,
  val grade: String, // "A", "B+", "B", etc.
  val teacherName: String,
  val date: String,
  val comments: String = ""
)

@Entity(tableName = "hymns")
data class HymnItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val category: String, // "St. Gabriel", "St. Arsema", "Meskel", "Liturgy"
  val choirGroup: String, // "Senior Choir", "Youth Choir", "Children Choir"
  val leadSinger: String,
  val notes: String = ""
)

@Entity(tableName = "documents")
data class DocumentItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val category: String, // "Bylaws", "Curriculum", "Financial Policy", "Plan"
  val uploadedBy: String,
  val date: String,
  val accessLevel: String = "All",
  val fileDescription: String = ""
)

@Entity(tableName = "quiz_results")
data class QuizResult(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val storyId: String,
  val storyTitle: String,
  val participantName: String,
  val score: Int,
  val totalQuestions: Int,
  val percentage: Double,
  val completedAt: Long = System.currentTimeMillis(),
  val formattedDate: String = ""
)

@Entity(tableName = "bible_quiz_questions")
data class BibleQuizQuestion(
  @PrimaryKey val id: String,
  val storyId: String,
  val questionAmharic: String,
  val questionEnglish: String,
  val optionA: String,
  val optionB: String,
  val optionC: String,
  val optionD: String,
  val correctOptionIndex: Int, // 0 for A, 1 for B, 2 for C, 3 for D
  val explanationAmharic: String,
  val explanationEnglish: String
)

@Entity(tableName = "lesson_notes")
data class LessonNote(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val lessonId: String = "LESSON-${System.currentTimeMillis()}",
  val title: String,
  val titleAmharic: String = "",
  val subject: String, // "Bible Study", "Dogma", "Church History", "Hymns", "Ethics"
  val classGrade: String = "All Classes",
  val weekNumber: Int = 1,
  val datePosted: String,
  val scriptureReference: String = "",
  val summary: String,
  val memoryVerse: String = "",
  val teacherName: String,
  val teacherEmail: String = "",
  val createdAt: Long = System.currentTimeMillis()
)
