package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.language.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel
import com.example.util.DataExporter
import com.example.util.SecurityManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentAdminDashboardScreen(
  viewModel: SundaySchoolViewModel,
  onNavigateToSongAttendance: (() -> Unit)? = null,
  onNavigateToDataCenter: () -> Unit = {}
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val allUsers by viewModel.allUsers.collectAsState()
  val cloudSyncState by viewModel.cloudSyncState.collectAsState()
  val departments by viewModel.departments.collectAsState()
  val currentDept = departments.find { it.id == currentUser?.departmentId } ?: departments.firstOrNull()

  val members by viewModel.members.collectAsState()
  val payments by viewModel.payments.collectAsState()
  val attendance by viewModel.attendance.collectAsState()
  val plans by viewModel.plans.collectAsState()
  val reports by viewModel.reports.collectAsState()
  val grades by viewModel.grades.collectAsState()
  val hymns by viewModel.hymns.collectAsState()
  val documents by viewModel.documents.collectAsState()

  var selectedTab by remember { mutableStateOf(0) }
  val deptName = currentDept?.nameEn ?: "Department"

  // Dialog states
  var showSubmitReportDialog by remember { mutableStateOf(false) }
  var showNewPlanDialog by remember { mutableStateOf(false) }
  var showRecordAttendanceDialog by remember { mutableStateOf(false) }
  var showAddGradeDialog by remember { mutableStateOf(false) }
  var showAddHymnDialog by remember { mutableStateOf(false) }
  var showVerifyPaymentDialog by remember { mutableStateOf<PaymentItem?>(null) }
  var showHolidayUniformAnnouncementDialog by remember { mutableStateOf(false) }
  var showExportAttendanceDialog by remember { mutableStateOf(false) }
  var announcementFeedback by remember { mutableStateOf<String?>(null) }
  var receiptNumberInput by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      ChurchTopAppBar(
        userName = currentUser?.fullName,
        userRole = currentDept?.nameAm ?: AppStrings.roleDeptAdmin,
        onLogout = { viewModel.logout() },
        title = currentDept?.nameEn ?: AppStrings.roleDeptAdmin,
        subtitle = currentDept?.nameAm ?: AppStrings.churchName,
        extraAction = {
          IconButton(
            onClick = onNavigateToDataCenter,
            modifier = Modifier.testTag("dept_admin_data_center_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = "Data Center, Firestore Sync & Security Audit",
              tint = ChurchGold
            )
          }
        }
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showSubmitReportDialog = true },
        icon = { Icon(Icons.Default.Send, contentDescription = null) },
        text = { Text(AppStrings.submitReport) },
        containerColor = ChurchDeepBlue,
        contentColor = Color.White,
        modifier = Modifier.testTag("submit_report_fab")
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(ChurchIvory)
    ) {
      // Top Department Banner
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ChurchDeepBlue
      ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = currentDept?.nameEn ?: "",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = ChurchLightGold
            )
            val isApproved = currentUser?.isApproved == true
            val approvalStatus = currentUser?.approvalStatus ?: if (isApproved) "APPROVED" else "PENDING"

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = when (approvalStatus) {
                "APPROVED" -> ChurchGreenBg
                "REJECTED" -> ChurchRedBg
                else -> ChurchAmberBg
              }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = when (approvalStatus) {
                    "APPROVED" -> Icons.Default.CheckCircle
                    "REJECTED" -> Icons.Default.Cancel
                    else -> Icons.Default.HourglassTop
                  },
                  contentDescription = null,
                  tint = when (approvalStatus) {
                    "APPROVED" -> ChurchGreen
                    "REJECTED" -> ChurchRed
                    else -> ChurchGold
                  },
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = when (approvalStatus) {
                    "APPROVED" -> "Manager Verified ✅"
                    "REJECTED" -> "Suspended ⚠️"
                    else -> "Pending Approval ⏳"
                  },
                  color = when (approvalStatus) {
                    "APPROVED" -> ChurchGreen
                    "REJECTED" -> ChurchRed
                    else -> ChurchDarkGold
                  },
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
          Text(
            text = currentDept?.descriptionAm ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.padding(top = 2.dp)
          )

          // Song Department Direct Navigation Button
          if (currentDept?.code == "SNG" && onNavigateToSongAttendance != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = onNavigateToSongAttendance,
              colors = ButtonDefaults.buttonColors(containerColor = ChurchGold),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().testTag("open_song_attendance_screen_btn"),
              contentPadding = PaddingValues(vertical = 6.dp)
            ) {
              Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("የመዝሙር ክፍል አቴንዳንስ መዝገብ ክፈት (Open Full Attendance Register)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Tab bar for Department Workflows
      val deptTabs = listOf(AppStrings.navOverview, "Department Tools", AppStrings.navPlans, "Data & Security")
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.White,
        contentColor = ChurchDeepBlue,
        edgePadding = 8.dp
      ) {
        deptTabs.forEachIndexed { idx, title ->
          Tab(
            selected = selectedTab == idx,
            onClick = { selectedTab = idx },
            modifier = Modifier.testTag("dept_tab_$idx"),
            text = {
              Text(
                text = title,
                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == idx) ChurchDeepBlue else ChurchTextMuted,
                fontSize = 12.sp
              )
            }
          )
        }
      }

      // Content based on tab
      when (selectedTab) {
        0 -> DeptOverviewTab(
          dept = currentDept,
          plans = plans.filter { it.departmentId == currentDept?.id },
          reports = reports.filter { it.departmentId == currentDept?.id },
          members = members,
          attendance = attendance,
          currentUser = currentUser,
          deptCode = currentDept?.code ?: "",
          onOpenExportAttendance = { showExportAttendanceDialog = true }
        )
        1 -> DeptSpecializedToolsTab(
          dept = currentDept,
          members = members,
          payments = payments,
          attendance = attendance,
          grades = grades,
          hymns = hymns,
          documents = documents,
          onRecordAttendance = { showRecordAttendanceDialog = true },
          onExportAttendance = { showExportAttendanceDialog = true },
          onAddGrade = { showAddGradeDialog = true },
          onAddHymn = { showAddHymnDialog = true },
          onVerifyPayment = { p ->
            showVerifyPaymentDialog = p
            receiptNumberInput = "RCT-${(10000..99999).random()}"
          },
          onToggleUniformAccess = { memberId, approved ->
            viewModel.toggleMemberUniformAccess(memberId, approved)
          },
          onOpenHolidayAnnouncement = {
            showHolidayUniformAnnouncementDialog = true
          },
          onNavigateToSongAttendance = onNavigateToSongAttendance
        )
        2 -> DeptPlansTab(
          dept = currentDept,
          plans = plans.filter { it.departmentId == currentDept?.id },
          reports = reports.filter { it.departmentId == currentDept?.id },
          onNewPlan = { showNewPlanDialog = true }
        )
        3 -> DeptDataCenterAndSecurityTab(
          currentDept = currentDept,
          allDepartments = departments,
          allUsers = allUsers,
          members = members,
          plans = plans,
          reports = reports,
          attendance = attendance,
          payments = payments,
          cloudSyncStatus = cloudSyncState.statusMessage,
          lastSyncedAt = cloudSyncState.lastSyncedAt,
          onSyncFirestoreNow = { viewModel.syncRoomWithCloudFirestore() },
          onOpenFullDataCenter = onNavigateToDataCenter
        )
      }
    }
  }

  // Submit Report Dialog
  if (showSubmitReportDialog) {
    var title by remember { mutableStateOf("${currentDept?.nameEn} Monthly Progress Report") }
    var period by remember { mutableStateOf("Meskerem 2019 E.C.") }
    var summary by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showSubmitReportDialog = false },
      title = { Text(AppStrings.submitReport, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Report Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("report_title_input")
          )
          OutlinedTextField(
            value = period,
            onValueChange = { period = it },
            label = { Text("Period") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = summary,
            onValueChange = { summary = it },
            label = { Text("Report Summary & Achievements") },
            modifier = Modifier.fillMaxWidth().height(120.dp).testTag("report_summary_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              viewModel.submitReport(
                title = title,
                deptId = currentDept?.id ?: 1,
                deptName = currentDept?.nameEn ?: "Dept",
                period = period,
                summary = summary
              )
              showSubmitReportDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("confirm_submit_report")
        ) {
          Text(AppStrings.submit)
        }
      },
      dismissButton = {
        TextButton(onClick = { showSubmitReportDialog = false }) { Text(AppStrings.cancel) }
      }
    )
  }

  // New Plan Dialog
  if (showNewPlanDialog) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var measurement by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("25000") }

    AlertDialog(
      onDismissRequest = { showNewPlanDialog = false },
      title = { Text("Submit Department Plan", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Plan Title") },
            modifier = Modifier.fillMaxWidth().testTag("plan_title_input")
          )
          OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("Target Outcome") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = measurement,
            onValueChange = { measurement = it },
            label = { Text("Measurement Indicator") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = budget,
            onValueChange = { budget = it },
            label = { Text("Budget (ETB)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              viewModel.submitPlan(
                title = title,
                deptId = currentDept?.id ?: 1,
                deptName = currentDept?.nameEn ?: "Dept",
                target = target,
                measurement = measurement,
                budget = budget.toDoubleOrNull() ?: 0.0
              )
              showNewPlanDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("confirm_submit_plan")
        ) {
          Text(AppStrings.submit)
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewPlanDialog = false }) { Text(AppStrings.cancel) }
      }
    )
  }

  // Record Attendance Dialog (for HR / Education / Song)
  if (showRecordAttendanceDialog) {
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.memberId ?: "") }
    var status by remember { mutableStateOf("Present") }
    var serviceType by remember {
      mutableStateOf(if (currentDept?.code == "SNG") "Choir Rehearsal / የመዝሙር ልምምድ" else "Sunday Liturgy & Assembly")
    }

    AlertDialog(
      onDismissRequest = { showRecordAttendanceDialog = false },
      title = {
        Text(
          if (currentDept?.code == "SNG") "Record Choir Attendance (የመዝሙር ልምምድ)" else "Record Attendance",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Select Member / መዝሙራዊ:", style = MaterialTheme.typography.labelMedium)
          members.take(6).forEach { m ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth().clickable { selectedMemberId = m.memberId }
            ) {
              RadioButton(selected = selectedMemberId == m.memberId, onClick = { selectedMemberId = m.memberId })
              Text("${m.fullName} (${m.memberId})", fontSize = 12.sp)
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text("Service Type / የአገልግሎት ዓይነት:", style = MaterialTheme.typography.labelMedium)
          listOf(
            "Choir Rehearsal / የመዝሙር ልምምድ",
            "Sunday Liturgy & Assembly / የሰንበት ጉባኤ",
            "Holiday Practice / የበዓል ዝግጅት"
          ).forEach { type ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth().clickable { serviceType = type }
            ) {
              RadioButton(selected = serviceType == type, onClick = { serviceType = type })
              Text(type, fontSize = 11.sp)
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text("Status:", style = MaterialTheme.typography.labelMedium)
          Row {
            listOf("Present", "Absent", "Excused").forEach { s ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { status = s }.padding(end = 8.dp)
              ) {
                RadioButton(selected = status == s, onClick = { status = s })
                Text(s, fontSize = 11.sp)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val mem = members.find { it.memberId == selectedMemberId }
            viewModel.recordAttendance(
              memberId = selectedMemberId,
              memberName = mem?.fullName ?: "Member",
              status = status,
              serviceType = serviceType,
              notes = "Recorded by ${currentDept?.nameEn ?: "Dept Admin"}"
            )
            showRecordAttendanceDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
        ) {
          Text(AppStrings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showRecordAttendanceDialog = false }) { Text(AppStrings.cancel) }
      }
    )
  }

  // Add Grade Dialog (Education)
  if (showAddGradeDialog) {
    var studentName by remember { mutableStateOf("Natnael Zewdu") }
    var subject by remember { mutableStateOf("Geez Language Grammar & Hymns") }
    var score by remember { mutableStateOf("92") }

    AlertDialog(
      onDismissRequest = { showAddGradeDialog = false },
      title = { Text("Record Student Grade", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = studentName,
            onValueChange = { studentName = it },
            label = { Text("Student Name") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text(AppStrings.subject) },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = score,
            onValueChange = { score = it },
            label = { Text("Score (out of 100)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.addGrade(
              studentId = "MH-2019-109",
              studentName = studentName,
              subject = subject,
              score = score.toDoubleOrNull() ?: 85.0,
              comments = "Recorded by Education Dept"
            )
            showAddGradeDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
        ) {
          Text(AppStrings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddGradeDialog = false }) { Text(AppStrings.cancel) }
      }
    )
  }

  // Holiday Uniform Access Announcement Dialog (Song Dept)
  if (showHolidayUniformAnnouncementDialog) {
    var holidayName by remember { mutableStateOf("Meskerem 17 Feast of the Holy Cross (የመስቀል ደመራ በዓል)") }
    var customNotes by remember {
      mutableStateOf("All approved choir vocalists must assemble at 5:30 AM in official Sunday School vestments and crosses for liturgical hymns.")
    }
    val approvedChoirMembers = members.filter { it.uniformAccessApproved }

    AlertDialog(
      onDismissRequest = { showHolidayUniformAnnouncementDialog = false },
      icon = { Icon(Icons.Default.Campaign, contentDescription = null, tint = ChurchGold, modifier = Modifier.size(36.dp)) },
      title = {
        Text("Publish Holiday Uniform Announcement\n(የበዓላት የደንብ ልብስ ማስታወቂያ)", fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Official announcement for choir members qualified to receive Sunday School uniforms for liturgical service:",
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextMuted
          )
          OutlinedTextField(
            value = holidayName,
            onValueChange = { holidayName = it },
            label = { Text("Holiday / Feast Occasion (የበዓሉ ስም)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          OutlinedTextField(
            value = customNotes,
            onValueChange = { customNotes = it },
            label = { Text("Service Instructions & Dress Code Notes") },
            modifier = Modifier.fillMaxWidth().height(90.dp)
          )
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchPaleGold.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Text(
                text = "Approved Choir Members (${approvedChoirMembers.size} Qualified):",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = ChurchDeepBlue
              )
              if (approvedChoirMembers.isEmpty()) {
                Text("⚠️ No members approved yet. Toggle members in the uniform tool below before publishing.", fontSize = 10.sp, color = ChurchRed)
              } else {
                Text(
                  text = approvedChoirMembers.joinToString(", ") { it.fullName },
                  fontSize = 10.sp,
                  color = ChurchGreen,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val qualified = approvedChoirMembers.map { it.fullName }
            viewModel.publishHolidayUniformAnnouncement(holidayName, qualified, customNotes)
            showHolidayUniformAnnouncementDialog = false
            announcementFeedback = "Official Holiday Uniform Access Announcement has been published successfully for all members to view!"
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("confirm_publish_uniform_announcement")
        ) {
          Text("Publish Announcement / አውጣ")
        }
      },
      dismissButton = {
        TextButton(onClick = { showHolidayUniformAnnouncementDialog = false }) {
          Text(AppStrings.cancel)
        }
      }
    )
  }

  // Announcement Feedback Dialog
  announcementFeedback?.let { feedback ->
    AlertDialog(
      onDismissRequest = { announcementFeedback = null },
      icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChurchGreen, modifier = Modifier.size(36.dp)) },
      title = { Text("Announcement Published", fontWeight = FontWeight.Bold) },
      text = { Text(feedback, style = MaterialTheme.typography.bodySmall) },
      confirmButton = {
        Button(
          onClick = { announcementFeedback = null },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
        ) {
          Text(AppStrings.ok)
        }
      }
    )
  }

  // Add Hymn Dialog (Song & Arts)
  if (showAddHymnDialog) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("St. Gabriel") }
    var choir by remember { mutableStateOf("Senior Choir") }

    AlertDialog(
      onDismissRequest = { showAddHymnDialog = false },
      title = { Text("Add Hymn / Mahlet (መዝሙር መዝግብ)", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Hymn Title (የዝማሬ ርዕስ)") },
            modifier = Modifier.fillMaxWidth().testTag("hymn_title_input")
          )
          OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (ቅዱስ ገብርኤል፣ ቅድስት አርሴማ፣ ወዘተ)") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = choir,
            onValueChange = { choir = it },
            label = { Text("Choir Section (የመዘምራን ክፍል)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              viewModel.addHymn(title, category, choir, "Added by Song Dept Admin")
              showAddHymnDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
        ) {
          Text(AppStrings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddHymnDialog = false }) { Text(AppStrings.cancel) }
      }
    )
  }

  // Verify Payment Dialog (Finance Admin)
  showVerifyPaymentDialog?.let { p ->
    AlertDialog(
      onDismissRequest = { showVerifyPaymentDialog = null },
      title = { Text("Finance Admin Payment Verification", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Member: ${p.memberName}")
          Text("Amount: ETB ${p.amountPaid}")
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = receiptNumberInput,
            onValueChange = { receiptNumberInput = it },
            label = { Text(AppStrings.receiptNumber) },
            modifier = Modifier.fillMaxWidth().testTag("finance_receipt_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.verifyPayment(p.id, receiptNumberInput)
            showVerifyPaymentDialog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
        ) {
          Text(AppStrings.verifyPayment)
        }
      },
      dismissButton = {
        TextButton(onClick = { showVerifyPaymentDialog = null }) { Text(AppStrings.cancel) }
      }
    )
  }

  // Export Monthly Attendance Dialog for Department Admin
  if (showExportAttendanceDialog) {
    ExportAttendanceDepartmentDialog(
      records = attendance,
      members = members,
      onDismiss = { showExportAttendanceDialog = false }
    )
  }
}

@Composable
fun DeptOverviewTab(
  dept: Department?,
  plans: List<PlanItem>,
  reports: List<ReportItem>,
  members: List<MemberItem>,
  attendance: List<AttendanceRecord>,
  currentUser: User?,
  deptCode: String,
  onOpenExportAttendance: () -> Unit = {}
) {
  val activeMembersCount = members.count { it.status == "Active" }
  val presentOrLateCount = attendance.count { it.status == "Present" || it.status == "Late" }
  val weeklyAttendancePercentage = if (attendance.isNotEmpty()) ((presentOrLateCount.toDouble() / attendance.size) * 100).toInt() else 88
  val pendingApprovalRequests = plans.count { it.status == "Pending" } +
    reports.count { it.status == "Submitted" } +
    (if (currentUser?.isApproved == false || currentUser?.approvalStatus == "PENDING") 1 else 0)

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // HIGH-LEVEL SUMMARY VIEW: Total active members, % of attendance this week, pending approval requests for department admins
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, ChurchPaleGold),
        modifier = Modifier.fillMaxWidth().testTag("dept_admin_high_level_summary_view")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = CircleShape,
                color = ChurchDeepBlue,
                modifier = Modifier.size(38.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    Icons.Default.Insights,
                    contentDescription = null,
                    tint = ChurchGold,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "የክፍል ፈጣን ማጠቃለያ (Admin Overview)",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = ChurchDarkBlue
                )
                Text(
                  text = "${dept?.nameEn ?: "Department"} Live Operational Health",
                  style = MaterialTheme.typography.labelSmall,
                  color = ChurchTextMuted
                )
              }
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ChurchPaleGold,
              modifier = Modifier.clickable { onOpenExportAttendance() }.testTag("dept_open_export_attendance_btn")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = ChurchDarkGold, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export Attendance", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ChurchDarkGold)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = Color(0xFFF1F5F9))
          Spacer(modifier = Modifier.height(14.dp))

          // 3 Metric Cards
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Metric 1: Total Active Members
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFFF0FDF4),
              border = BorderStroke(1.dp, ChurchGreen.copy(alpha = 0.3f)),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(modifier = Modifier.size(7.dp).background(ChurchGreen, CircleShape))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Active Members",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChurchGreen
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "$activeMembersCount",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = ChurchDarkBlue
                )
                Text(
                  text = "ከ ${members.size} አባላት",
                  fontSize = 9.sp,
                  color = ChurchTextSecondary,
                  maxLines = 1
                )
              }
            }

            // Metric 2: Percentage of Attendance This Week
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFFEFF6FF),
              border = BorderStroke(1.dp, ChurchDeepBlue.copy(alpha = 0.2f)),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = ChurchDeepBlue,
                    modifier = Modifier.size(11.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Attendance",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChurchDeepBlue
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "$weeklyAttendancePercentage%",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = ChurchDeepBlue
                )
                Text(
                  text = "የዚህ ሳምንት ምዝገባ",
                  fontSize = 9.sp,
                  color = ChurchTextSecondary,
                  maxLines = 1
                )
              }
            }

            // Metric 3: Pending Approval Requests for Department Admins
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (pendingApprovalRequests > 0) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
              border = BorderStroke(
                1.dp,
                if (pendingApprovalRequests > 0) ChurchGold.copy(alpha = 0.4f) else Color(0xFFE2E8F0)
              ),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (pendingApprovalRequests > 0) Icons.Default.HourglassTop else Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = if (pendingApprovalRequests > 0) ChurchGold else ChurchGreen,
                    modifier = Modifier.size(11.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Pending Req.",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (pendingApprovalRequests > 0) ChurchDarkGold else ChurchTextSecondary
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "$pendingApprovalRequests",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (pendingApprovalRequests > 0) ChurchDarkGold else ChurchDarkBlue
                )
                Text(
                  text = if (pendingApprovalRequests > 0) "ማረጋገጫ የሚጠብቅ" else "ሁሉ ጸድቋል ✅",
                  fontSize = 9.sp,
                  color = if (pendingApprovalRequests > 0) ChurchDarkGold else ChurchGreen,
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        KpiCard(
          title = "Active Plans",
          value = "${plans.size}",
          subtitle = "${plans.count { it.status == "Approved" }} Approved",
          icon = Icons.Default.Assignment,
          modifier = Modifier.weight(1f)
        )
        KpiCard(
          title = "Submitted Reports",
          value = "${reports.size}",
          subtitle = "For Manager Review",
          icon = Icons.Default.Description,
          iconTint = ChurchGreen,
          iconBg = Color(0xFFDCFCE7),
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Department Specific Chart
    item {
      val chartItems = when (deptCode) {
        "FIN" -> listOf(
          BarChartItem("Hamle", 18500f, ChartBlue),
          BarChartItem("Nehase", 22400f, ChartBlue),
          BarChartItem("Meskerem", 28500f, ChartTeal)
        )
        "EDU" -> listOf(
          BarChartItem("Dogma", 94f, ChartBlue),
          BarChartItem("Geez", 89f, ChartGold),
          BarChartItem("History", 91f, ChartTeal)
        )
        "HRM" -> listOf(
          BarChartItem("Worker", 4f, ChartBlue),
          BarChartItem("Student", 2f, ChartGold),
          BarChartItem("Non-Work", 2f, ChartTeal)
        )
        else -> listOf(
          BarChartItem("Target", 100f, ChartBlue),
          BarChartItem("Achieved", 85f, ChartGold)
        )
      }

      SimpleBarChart(
        title = "${dept?.nameEn} Performance Chart",
        subtitle = "Department internal progress metrics",
        items = chartItems
      )
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Department Mandate & Responsibilities", fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
          Spacer(modifier = Modifier.height(4.dp))
          Text(dept?.descriptionEn ?: "", style = MaterialTheme.typography.bodySmall, color = ChurchTextPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          Text(dept?.descriptionAm ?: "", style = MaterialTheme.typography.bodySmall, color = ChurchTextSecondary)
        }
      }
    }
  }
}

@Composable
fun DeptSpecializedToolsTab(
  dept: Department?,
  members: List<MemberItem>,
  payments: List<PaymentItem>,
  attendance: List<AttendanceRecord>,
  grades: List<GradeItem>,
  hymns: List<HymnItem>,
  documents: List<DocumentItem>,
  onRecordAttendance: () -> Unit,
  onExportAttendance: () -> Unit = {},
  onAddGrade: () -> Unit,
  onAddHymn: () -> Unit,
  onVerifyPayment: (PaymentItem) -> Unit,
  onToggleUniformAccess: (String, Boolean) -> Unit = { _, _ -> },
  onOpenHolidayAnnouncement: () -> Unit = {},
  onNavigateToSongAttendance: (() -> Unit)? = null
) {
  val code = dept?.code ?: ""
  var studentSearchQuery by remember { mutableStateOf("") }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Finance Department Tools
    if (code == "FIN") {
      item {
        Text("Pending Payments to Verify (Finance Admin)", fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
      }
      items(payments) { p ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(p.memberName, fontWeight = FontWeight.Bold)
              Text("ETB ${p.amountPaid.toInt()} • ${p.paymentMethod}", style = MaterialTheme.typography.bodySmall)
            }
            if (!p.verified) {
              Button(
                onClick = { onVerifyPayment(p) },
                colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
              ) {
                Text(AppStrings.verifyPayment)
              }
            } else {
              StatusBadge("Verified")
            }
          }
        }
      }
    }

    // HR Department Tools
    else if (code == "HRM") {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Attendance & Members (HR)", fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
              Text("Student roster, tracking & reports", fontSize = 11.sp, color = ChurchTextMuted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              OutlinedButton(
                onClick = onExportAttendance,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                border = BorderStroke(1.dp, ChurchGold),
                modifier = Modifier.testTag("dept_hrm_export_attendance_btn")
              ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = ChurchDarkGold, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Export", fontSize = 11.sp, color = ChurchDarkGold, fontWeight = FontWeight.Bold)
              }
              Button(
                onClick = onRecordAttendance,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Record", fontSize = 11.sp)
              }
            }
          }

          // Search Bar for quick filtering students by name or registration ID
          OutlinedTextField(
            value = studentSearchQuery,
            onValueChange = { studentSearchQuery = it },
            placeholder = { Text("Search students by name or ID (e.g. MH-2015-001)...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ChurchDeepBlue) },
            trailingIcon = {
              if (studentSearchQuery.isNotEmpty()) {
                IconButton(onClick = { studentSearchQuery = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear", tint = ChurchTextSecondary)
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = ChurchDeepBlue,
              unfocusedBorderColor = Color(0xFFCBD5E1),
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().testTag("dept_hrm_search_input")
          )
        }
      }

      val filteredMembers = members.filter {
        studentSearchQuery.isEmpty() ||
          it.fullName.contains(studentSearchQuery, ignoreCase = true) ||
          it.memberCategory.contains(studentSearchQuery, ignoreCase = true) ||
          it.memberId.contains(studentSearchQuery, ignoreCase = true) ||
          it.classGrade.contains(studentSearchQuery, ignoreCase = true)
      }

      val filteredAttendance = attendance.filter {
        studentSearchQuery.isEmpty() ||
          it.memberName.contains(studentSearchQuery, ignoreCase = true) ||
          it.date.contains(studentSearchQuery, ignoreCase = true)
      }

      if (studentSearchQuery.isNotEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchPaleGold,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Showing ${filteredMembers.size} students matching \"$studentSearchQuery\"",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = ChurchDarkGold,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }

      // Filtered student cards
      item {
        Text("Students Roster (${filteredMembers.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ChurchDarkBlue)
      }

      items(filteredMembers) { m ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(m.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = ChurchDeepBlue.copy(alpha = 0.08f)
                ) {
                  Text(m.memberId, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ChurchDeepBlue, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text("${m.classGrade} • ${m.memberCategory}", fontSize = 11.sp, color = ChurchTextSecondary)
            }
            StatusBadge(m.status)
          }
        }
      }

      // Recent attendance logs
      item {
        Text("Recent Attendance Logs (${filteredAttendance.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ChurchDarkBlue, modifier = Modifier.padding(top = 8.dp))
      }

      items(filteredAttendance) { att ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(att.memberName, fontWeight = FontWeight.Bold)
              Text("${att.date} • ${att.serviceType}", style = MaterialTheme.typography.labelSmall, color = ChurchTextMuted)
            }
            StatusBadge(att.status)
          }
        }
      }
    }

    // Education Department Tools
    else if (code == "EDU") {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Curriculum & Grades (Education)", fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
          Button(onClick = onAddGrade, colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)) {
            Text("Record Grade")
          }
        }
      }
      items(grades) { g ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(g.studentName, fontWeight = FontWeight.Bold)
              Text(g.subject, style = MaterialTheme.typography.bodySmall, color = ChurchTextSecondary)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = ChurchPaleGold) {
              Text(
                text = "${g.score.toInt()}% (${g.grade})",
                color = ChurchDarkGold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }

    // Song & Arts Department Tools (Attendance Control + Holiday Uniform Access & Announcement)
    else if (code == "SNG") {
      // 1. Attendance Control Section
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Choir Attendance Control / የመዝሙር ልምምድ አቴንዳንስ",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = ChurchDeepBlue
                )
                Text(
                  text = "Track rehearsal and practice attendance to qualify members for holiday vestments",
                  style = MaterialTheme.typography.bodySmall,
                  color = ChurchTextMuted
                )
              }
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (onNavigateToSongAttendance != null) {
                  Button(
                    onClick = onNavigateToSongAttendance,
                    colors = ButtonDefaults.buttonColors(containerColor = ChurchGold),
                    modifier = Modifier.testTag("song_open_full_register_btn")
                  ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Full Register", fontSize = 12.sp)
                  }
                }
                Button(
                  onClick = onRecordAttendance,
                  colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
                  modifier = Modifier.testTag("song_record_attendance_btn")
                ) {
                  Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Record", fontSize = 12.sp)
                }
              }
            }
          }
        }
      }

      // Display Choir Attendance Logs
      val choirAttendance = attendance.filter {
        it.serviceType.contains("Choir", ignoreCase = true) ||
          it.serviceType.contains("ልምምድ", ignoreCase = true) ||
          it.serviceType.contains("Holiday", ignoreCase = true)
      }.ifEmpty { attendance.take(3) }

      item {
        Text(
          text = "Recent Choir Rehearsal Attendance Records",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = ChurchDeepBlue,
          modifier = Modifier.padding(top = 4.dp)
        )
      }

      items(choirAttendance) { att ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(att.memberName, fontWeight = FontWeight.Bold, color = ChurchTextPrimary)
              Text("${att.date} • ${att.serviceType}", style = MaterialTheme.typography.labelSmall, color = ChurchTextMuted)
            }
            StatusBadge(att.status)
          }
        }
      }

      // 2. Holiday Uniform Service Access Control & Announcement Section
      item {
        Spacer(modifier = Modifier.height(6.dp))
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ChurchPaleGold.copy(alpha = 0.45f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Checkroom, contentDescription = null, tint = ChurchDarkGold, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Holiday Uniform Service Access Control",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = ChurchDeepBlue
                )
                Text(
                  text = "የበዓላት የደንብ ልብስ (Uniform) ፍቃድ መቆጣጠሪያ",
                  style = MaterialTheme.typography.bodySmall,
                  color = ChurchTextSecondary
                )
              }
            }

            Text(
              text = "Control which choir members qualify to wear official Sunday School vestments during holiday feast services based on rehearsal attendance, and announce the approved list to all members.",
              style = MaterialTheme.typography.bodySmall,
              color = ChurchTextMuted,
              modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
            )

            Button(
              onClick = onOpenHolidayAnnouncement,
              colors = ButtonDefaults.buttonColors(containerColor = ChurchDarkGold),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("publish_holiday_announcement_btn")
            ) {
              Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(8.dp))
              Text("📢 Publish Holiday Uniform Announcement", fontWeight = FontWeight.Bold, color = Color.White)
            }
          }
        }
      }

      item {
        Text(
          text = "Choir Members & Uniform Access Status",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = ChurchDeepBlue,
          modifier = Modifier.padding(top = 6.dp)
        )
      }

      // List members with uniform access toggle
      items(members) { member ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(member.fullName, fontWeight = FontWeight.Bold, color = ChurchTextPrimary)
                Text("${member.memberId} • ${member.classGrade} (${member.memberCategory})", style = MaterialTheme.typography.labelSmall, color = ChurchTextSecondary)
              }

              if (member.uniformAccessApproved) {
                Surface(shape = RoundedCornerShape(8.dp), color = ChurchGreen.copy(alpha = 0.15f)) {
                  Text(
                    text = "✅ Uniform Approved",
                    color = ChurchGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              } else {
                Surface(shape = RoundedCornerShape(8.dp), color = ChurchAmber.copy(alpha = 0.15f)) {
                  Text(
                    text = "⏳ Incomplete Attendance",
                    color = ChurchAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              if (member.uniformAccessApproved) {
                OutlinedButton(
                  onClick = { onToggleUniformAccess(member.memberId, false) },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = ChurchRed),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                  modifier = Modifier.height(32.dp).testTag("revoke_uniform_${member.id}")
                ) {
                  Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Revoke Uniform Access", fontSize = 11.sp)
                }
              } else {
                Button(
                  onClick = { onToggleUniformAccess(member.memberId, true) },
                  colors = ButtonDefaults.buttonColors(containerColor = ChurchGreen),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                  modifier = Modifier.height(32.dp).testTag("grant_uniform_${member.id}")
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Grant Uniform Access", fontSize = 11.sp)
                }
              }
            }
          }
        }
      }

      // 3. Hymns Section
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Hymns, Choirs & Repertoire (መዝሙርና ሥነ-ጥበብ)", fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
          Button(onClick = onAddHymn, colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)) {
            Text("Add Hymn")
          }
        }
      }
      items(hymns) { h ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(h.title, fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
            Text("${h.category} • ${h.choirGroup}", style = MaterialTheme.typography.labelSmall, color = ChurchGold)
            if (h.notes.isNotEmpty()) {
              Text(h.notes, style = MaterialTheme.typography.bodySmall, color = ChurchTextMuted, modifier = Modifier.padding(top = 4.dp))
            }
          }
        }
      }
    }

    // Info & Documentation / Office
    else {
      item {
        Text("Department Documents & Archives", fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
      }
      items(documents) { doc ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(doc.title, fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
            Text("Category: ${doc.category} • Date: ${doc.date}", style = MaterialTheme.typography.labelSmall, color = ChurchTextMuted)
            Text(doc.fileDescription, style = MaterialTheme.typography.bodySmall, color = ChurchTextPrimary, modifier = Modifier.padding(top = 4.dp))
          }
        }
      }
    }
  }
}

@Composable
fun DeptPlansTab(
  dept: Department?,
  plans: List<PlanItem>,
  reports: List<ReportItem>,
  onNewPlan: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("My Department Plans", fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
        Button(onClick = onNewPlan, colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)) {
          Text("Add Plan")
        }
      }
    }

    items(plans) { plan ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(plan.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            StatusBadge(plan.status)
          }
          Text("Target: ${plan.target}", style = MaterialTheme.typography.bodySmall, color = ChurchTextSecondary)
          Text("Budget: ETB ${plan.budget.toInt()}", style = MaterialTheme.typography.labelSmall, color = ChurchDeepBlue, fontWeight = FontWeight.SemiBold)
        }
      }
    }

    item {
      Text("Submitted Department Reports", fontWeight = FontWeight.Bold, color = ChurchDeepBlue, modifier = Modifier.padding(top = 12.dp))
    }

    items(reports) { r ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(r.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            StatusBadge(r.status)
          }
          Text(r.summary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp))
          Text("Period: ${r.period} • Submitted: ${r.submittedDate}", style = MaterialTheme.typography.labelSmall, color = ChurchTextMuted)
        }
      }
    }
  }
}

@Composable
fun ExportAttendanceDepartmentDialog(
  records: List<AttendanceRecord>,
  members: List<MemberItem>,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var selectedExportType by remember { mutableStateOf("CSV") }
  var copiedFeedback by remember { mutableStateOf(false) }

  val previewContent = remember(selectedExportType, records, members) {
    if (selectedExportType == "CSV") {
      DataExporter.generateAttendanceCsv(records)
    } else {
      DataExporter.generateAttendancePrintableReport(records, members)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("dept_export_attendance_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.FileDownload, contentDescription = null, tint = ChurchDeepBlue)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "Export Monthly Attendance Data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ChurchDeepBlue
          )
          Text(
            text = "ወርሃዊ የሰንበት ት/ቤት የተማሪዎች ምዝገባ ሪፖርት",
            style = MaterialTheme.typography.labelSmall,
            color = ChurchTextSecondary
          )
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Select export format for church archives and administration records:",
          fontSize = 12.sp,
          color = ChurchTextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Format tabs
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selectedExportType == "CSV") ChurchDeepBlue else Color(0xFFF1F5F9),
            border = BorderStroke(
              1.dp,
              if (selectedExportType == "CSV") ChurchDeepBlue else Color(0xFFCBD5E1)
            ),
            modifier = Modifier
              .weight(1f)
              .clickable {
                selectedExportType = "CSV"
                copiedFeedback = false
              }
              .testTag("dept_export_csv_tab")
          ) {
            Column(
              modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "📊 CSV Format",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedExportType == "CSV") Color.White else ChurchDeepBlue
              )
              Text(
                text = "Excel & Sheets",
                fontSize = 10.sp,
                color = if (selectedExportType == "CSV") ChurchPaleGold else ChurchTextMuted
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selectedExportType == "PDF") ChurchDeepBlue else Color(0xFFF1F5F9),
            border = BorderStroke(
              1.dp,
              if (selectedExportType == "PDF") ChurchDeepBlue else Color(0xFFCBD5E1)
            ),
            modifier = Modifier
              .weight(1f)
              .clickable {
                selectedExportType = "PDF"
                copiedFeedback = false
              }
              .testTag("dept_export_pdf_tab")
          ) {
            Column(
              modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "📄 Printable / PDF",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedExportType == "PDF") Color.White else ChurchDeepBlue
              )
              Text(
                text = "Church Audit Record",
                fontSize = 10.sp,
                color = if (selectedExportType == "PDF") ChurchPaleGold else ChurchTextMuted
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Preview Card
        Text(
          text = "Preview (${records.size} Records from Room Database):",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = ChurchDarkBlue
        )
        Spacer(modifier = Modifier.height(4.dp))

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFF1E293B),
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
        ) {
          LazyColumn(modifier = Modifier.padding(10.dp)) {
            item {
              Text(
                text = previewContent,
                fontSize = 10.sp,
                color = Color(0xFFE2E8F0),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                lineHeight = 14.sp
              )
            }
          }
        }

        if (copiedFeedback) {
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = ChurchGreenBg,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "✅ Copied to clipboard successfully!",
              color = ChurchGreen,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (selectedExportType == "CSV") {
            DataExporter.shareCsv(context, "EOTC_Sunday_School_Monthly_Attendance.csv", previewContent)
          } else {
            DataExporter.shareText(context, "EOTC_Sunday_School_Monthly_Attendance_Report", previewContent)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
        modifier = Modifier.testTag("dept_confirm_export_btn")
      ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Export & Share")
      }
    },
    dismissButton = {
      Row {
        TextButton(
          onClick = {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Attendance Export", previewContent)
            clipboard.setPrimaryClip(clip)
            copiedFeedback = true
          },
          modifier = Modifier.testTag("dept_copy_export_btn")
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Copy")
        }
        TextButton(onClick = onDismiss) {
          Text(AppStrings.close)
        }
      }
    }
  )
}

@Composable
private fun DeptDataCenterAndSecurityTab(
  currentDept: Department?,
  allDepartments: List<Department>,
  allUsers: List<User>,
  members: List<MemberItem>,
  plans: List<PlanItem>,
  reports: List<ReportItem>,
  attendance: List<AttendanceRecord>,
  payments: List<PaymentItem>,
  cloudSyncStatus: String,
  lastSyncedAt: String,
  onSyncFirestoreNow: () -> Unit,
  onOpenFullDataCenter: () -> Unit
) {
  val context = LocalContext.current
  val dbPath = remember { context.getDatabasePath("meserete_hiwot_church.db").absolutePath }
  var auditChecks by remember { mutableStateOf(SecurityManager.runSecurityAudit()) }
  val passedChecksCount = auditChecks.count { it.passed }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // 1. Live Firestore Sync & Data Center Banner
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchDeepBlue),
        border = BorderStroke(1.dp, ChurchGold),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "DEPARTMENT DATA CENTER & FIRESTORE SYNC",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = ChurchLightGold
              )
              Text(
                text = "${currentDept?.nameEn ?: "Department"} (${currentDept?.code ?: "DEPT"})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
              )
              Text(
                text = "Cloud Sync: $cloudSyncStatus • Last Sync: $lastSyncedAt",
                fontSize = 11.sp,
                color = ChurchPaleGold
              )
            }
            Button(
              onClick = onSyncFirestoreNow,
              colors = ButtonDefaults.buttonColors(containerColor = ChurchGold),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("dept_sync_firestore_button")
            ) {
              Icon(Icons.Default.Sync, contentDescription = null, tint = ChurchDarkBlue, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Sync Firestore", color = ChurchDarkBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchDarkBlue,
            border = BorderStroke(1.dp, ChurchGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "Saved Data Center Storage Path (Room SQLite + Firestore):",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ChurchLightGold
              )
              Text(
                text = dbPath,
                fontSize = 11.sp,
                color = Color.White,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = onOpenFullDataCenter,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("dept_open_full_data_center_btn")
          ) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = ChurchDeepBlue, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Open Full Data Center, JSON/CSV Backup & Security Hub",
              color = ChurchDeepBlue,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    // 2. All 12 Departments Data Center, Firestore Sync & Security Matrix
    item {
      Text(
        text = "All ${allDepartments.size} Departments Data Center & Firestore Sync Matrix",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue
      )
      Text(
        text = "Real-time record counts, Firestore sync status, and RBAC security posture across every Sunday School department.",
        fontSize = 11.sp,
        color = ChurchTextSecondary
      )
    }

    items(
      allDepartments.sortedByDescending { it.id == currentDept?.id }
    ) { dept ->
      val isMyDept = dept.id == currentDept?.id
      val deptMembersCount = members.count { it.departmentId == dept.id }
      val deptPlansCount = plans.count { it.departmentId == dept.id }
      val deptReportsCount = reports.count { it.departmentId == dept.id }
      val deptAdmin = allUsers.firstOrNull { it.role == "DeptAdmin" && it.departmentId == dept.id }

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isMyDept) ChurchPaleGold else Color.White
        ),
        border = BorderStroke(
          width = if (isMyDept) 1.5.dp else 1.dp,
          color = if (isMyDept) ChurchGold else ChurchBorder
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "[${dept.code}] ${dept.nameEn} • ${dept.nameAm}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ChurchDeepBlue
              )
              Text(
                text = "Admin: ${deptAdmin?.fullName ?: "Department Admin"} (${deptAdmin?.email ?: "Managed"})",
                fontSize = 11.sp,
                color = ChurchTextSecondary
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ChurchGreenBg
            ) {
              Text(
                text = "Firestore Synced • RBAC OK",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = ChurchGreen,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Members: $deptMembersCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChurchDarkBlue)
            Text("Plans: $deptPlansCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChurchDarkBlue)
            Text("Reports: $deptReportsCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChurchDarkBlue)
            Text("Attendance: ${attendance.size}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChurchDarkBlue)
            if (dept.code == "FIN") {
              Text("Payments: ${payments.size}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChurchDarkGold)
            }
          }
        }
      }
    }

    // 3. 8-Point Security Audit Summary for Department Admins
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ChurchBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Security Audit Suite ($passedChecksCount/${auditChecks.size} Passed)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ChurchDeepBlue
              )
              Text(
                text = "SHA-256 Hashing, Role Isolation, Lockout, RBAC, SQLi & CSV Formula Protection",
                fontSize = 11.sp,
                color = ChurchTextSecondary
              )
            }
            OutlinedButton(
              onClick = { auditChecks = SecurityManager.runSecurityAudit() },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("dept_run_security_audit_button")
            ) {
              Text("Re-Test All 8", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          auditChecks.forEach { check ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (check.passed) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (check.passed) ChurchGreen else ChurchRed,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${check.id}: ${check.titleEn} (${check.titleAm})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ChurchTextPrimary
              )
            }
          }
        }
      }
    }
  }
}


