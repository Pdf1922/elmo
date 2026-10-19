package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun ManagerDashboardScreen(
  viewModel: SundaySchoolViewModel,
  onNavigateToAiCounselor: () -> Unit = {},
  onNavigateToBibleStory: () -> Unit = {},
  onNavigateToSongAttendance: () -> Unit = {},
  onNavigateToDataCenter: () -> Unit = {}
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val members by viewModel.members.collectAsState()
  val payments by viewModel.payments.collectAsState()
  val paymentSettings by viewModel.paymentSettings.collectAsState()
  val departments by viewModel.departments.collectAsState()
  val allUsers by viewModel.allUsers.collectAsState()
  val plans by viewModel.plans.collectAsState()
  val reports by viewModel.reports.collectAsState()
  val announcements by viewModel.announcements.collectAsState()
  val feedbackList by viewModel.feedbackList.collectAsState()
  val attendance by viewModel.attendance.collectAsState()
  val cloudSyncState by viewModel.cloudSyncState.collectAsState()

  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf(
    AppStrings.navOverview,
    AppStrings.navFinance,
    AppStrings.navMembers,
    "Depts",
    AppStrings.navPlans,
    AppStrings.navAnnouncements,
    AppStrings.navFeedback,
    "Data & Security"
  )

  // Dialog states
  var showVerifyDialog by remember { mutableStateOf<PaymentItem?>(null) }
  var receiptNumberInput by remember { mutableStateOf("") }
  var showRateDialog by remember { mutableStateOf<PaymentSetting?>(null) }
  var newAmountInput by remember { mutableStateOf("") }
  var showAddMemberDialog by remember { mutableStateOf(false) }
  var showAnnouncementDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  var showEditMemberDialog by remember { mutableStateOf<MemberItem?>(null) }
  var showEditDeptDialog by remember { mutableStateOf<Department?>(null) }
  var showEditPaymentDialog by remember { mutableStateOf<PaymentItem?>(null) }
  var showEditPlanDialog by remember { mutableStateOf<PlanItem?>(null) }
  var showEditAnnouncementDialog by remember { mutableStateOf<AnnouncementItem?>(null) }
  var showManageUserDialog by remember { mutableStateOf<User?>(null) }
  var showCreateNewUserDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      ChurchTopAppBar(
        userName = currentUser?.fullName,
        userRole = AppStrings.roleManager,
        onLogout = { viewModel.logout() },
        title = "Manager Portal / ሥራ አስኪያጅ",
        subtitle = "Debre Hayil St. Gabriel & St. Arsema Church",
        extraAction = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onNavigateToDataCenter,
              modifier = Modifier.testTag("manager_data_center_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Data Center & Security Hub",
                tint = ChurchGold
              )
            }
            IconButton(
              onClick = onNavigateToAiCounselor,
              modifier = Modifier.testTag("manager_ai_counselor_btn")
            ) {
              Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = "Spiritual AI Counselor",
                tint = ChurchGold
              )
            }
            IconButton(
              onClick = onNavigateToBibleStory,
              modifier = Modifier.testTag("manager_bible_story_btn")
            ) {
              Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "Daily Bible Stories",
                tint = ChurchLightGold
              )
            }
            IconButton(
              onClick = { showExportDialog = true },
              modifier = Modifier.testTag("export_statistics_button")
            ) {
              Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = "Export Statistics & Data",
                tint = ChurchLightGold
              )
            }
          }
        }
      )
    }
  ) { padding ->
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(ChurchIvory)
    ) {
      val isExpanded = maxWidth >= 640.dp

      if (isExpanded) {
        // Responsive Sidebar Navigation Layout for Admin roles
        Row(modifier = Modifier.fillMaxSize()) {
          NavigationRail(
            containerColor = ChurchDeepBlue,
            contentColor = Color.White,
            modifier = Modifier.width(90.dp)
          ) {
            tabs.forEachIndexed { index, title ->
              val icon = when (index) {
                0 -> Icons.Default.Dashboard
                1 -> Icons.Default.AccountBalanceWallet
                2 -> Icons.Default.People
                3 -> Icons.Default.Apartment
                4 -> Icons.Default.Assignment
                5 -> Icons.Default.Campaign
                6 -> Icons.Default.Forum
                else -> Icons.Default.Security
              }
              NavigationRailItem(
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title, fontSize = 10.sp, maxLines = 1) },
                colors = NavigationRailItemDefaults.colors(
                  selectedIconColor = ChurchDarkBlue,
                  selectedTextColor = ChurchLightGold,
                  indicatorColor = ChurchGold,
                  unselectedIconColor = Color.White.copy(alpha = 0.7f),
                  unselectedTextColor = Color.White.copy(alpha = 0.7f)
                )
              )
            }
          }

          Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            when (selectedTab) {
              0 -> ManagerOverviewTab(
                members = members,
                payments = payments,
                plans = plans,
                feedbackList = feedbackList,
                attendance = attendance,
                users = allUsers,
                onOpenExport = { showExportDialog = true },
                onNavigateToDeptsTab = { selectedTab = 3 }
              )
              1 -> ManagerFinanceTab(
                payments = payments,
                paymentSettings = paymentSettings,
                onVerifyPayment = { p ->
                  showVerifyDialog = p
                  receiptNumberInput = "RCT-${(10000..99999).random()}"
                },
                onEditRate = { setting ->
                  showRateDialog = setting
                  newAmountInput = setting.amount.toInt().toString()
                }
              )
              2 -> ManagerMembersTab(
                members = members,
                onAddMember = { showAddMemberDialog = true },
                onDeleteMember = { viewModel.deleteMember(it) }
              )
              3 -> ManagerDepartmentsTab(
                departments = departments,
                users = allUsers,
                onEditDepartment = { showEditDeptDialog = it },
                onSetAdminApprovalStatus = { userId, approved, status ->
                  viewModel.setAdminApprovalStatus(userId, approved, status)
                },
                onNavigateToSongAttendance = onNavigateToSongAttendance,
                onAddUserAccount = { showCreateNewUserDialog = true },
                onEditUserCredentials = { showManageUserDialog = it },
                onDeleteUserAccount = { viewModel.deleteManagedUser(it) },
                cloudSyncStatus = cloudSyncState.statusMessage,
                onSyncFirestoreNow = { viewModel.syncRoomWithCloudFirestore() },
                onOpenDataCenter = onNavigateToDataCenter
              )
              4 -> ManagerPlansReportsTab(
                plans = plans,
                reports = reports,
                onApprovePlan = { viewModel.approvePlan(it) },
                onApproveReport = { r -> viewModel.approveReport(r.id, "Approved by Executive Manager") }
              )
              5 -> ManagerAnnouncementsTab(
                announcements = announcements,
                onNewAnnouncement = { showAnnouncementDialog = true }
              )
              6 -> ManagerFeedbackTab(
                feedbackList = feedbackList,
                onRespond = { fb, reply -> viewModel.respondToFeedback(fb.id, reply) }
              )
              7 -> ManagerDataAndSecurityAllDeptsTab(
                departments = departments,
                users = allUsers,
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
      } else {
        // Mobile / Compact Layout with Top Tab Bar
        Column(modifier = Modifier.fillMaxSize()) {
          ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = ChurchDeepBlue,
            contentColor = Color.White,
            edgePadding = 12.dp
          ) {
            tabs.forEachIndexed { index, title ->
              Tab(
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                text = {
                  Text(
                    text = title,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == index) ChurchLightGold else Color.White.copy(alpha = 0.8f)
                  )
                }
              )
            }
          }

          when (selectedTab) {
            0 -> ManagerOverviewTab(
              members = members,
              payments = payments,
              plans = plans,
              feedbackList = feedbackList,
              attendance = attendance,
              users = allUsers,
              onOpenExport = { showExportDialog = true },
              onNavigateToDeptsTab = { selectedTab = 3 }
            )
            1 -> ManagerFinanceTab(
              payments = payments,
              paymentSettings = paymentSettings,
              onVerifyPayment = { p ->
                showVerifyDialog = p
                receiptNumberInput = "RCT-${(10000..99999).random()}"
              },
              onEditRate = { setting ->
                showRateDialog = setting
                newAmountInput = setting.amount.toInt().toString()
              },
              onEditPayment = { showEditPaymentDialog = it },
              onDeletePayment = { viewModel.deletePayment(it) }
            )
            2 -> ManagerMembersTab(
              members = members,
              onAddMember = { showAddMemberDialog = true },
              onEditMember = { showEditMemberDialog = it },
              onDeleteMember = { viewModel.deleteMember(it) }
            )
            3 -> ManagerDepartmentsTab(
              departments = departments,
              users = allUsers,
              onEditDepartment = { showEditDeptDialog = it },
              onSetAdminApprovalStatus = { userId, approved, status ->
                viewModel.setAdminApprovalStatus(userId, approved, status)
              },
              onNavigateToSongAttendance = onNavigateToSongAttendance,
              onAddUserAccount = { showCreateNewUserDialog = true },
              onEditUserCredentials = { showManageUserDialog = it },
              onDeleteUserAccount = { viewModel.deleteManagedUser(it) },
              cloudSyncStatus = cloudSyncState.statusMessage,
              onSyncFirestoreNow = { viewModel.syncRoomWithCloudFirestore() },
              onOpenDataCenter = onNavigateToDataCenter
            )
            4 -> ManagerPlansReportsTab(
              plans = plans,
              reports = reports,
              onApprovePlan = { viewModel.approvePlan(it) },
              onApproveReport = { r -> viewModel.approveReport(r.id, "Approved by Executive Manager") },
              onEditPlan = { showEditPlanDialog = it },
              onDeletePlan = { viewModel.deletePlan(it) }
            )
            5 -> ManagerAnnouncementsTab(
              announcements = announcements,
              onNewAnnouncement = { showAnnouncementDialog = true },
              onEditAnnouncement = { showEditAnnouncementDialog = it },
              onDeleteAnnouncement = { viewModel.deleteAnnouncement(it) }
            )
            6 -> ManagerFeedbackTab(
              feedbackList = feedbackList,
              onRespond = { fb, reply -> viewModel.respondToFeedback(fb.id, reply) }
            )
            7 -> ManagerDataAndSecurityAllDeptsTab(
              departments = departments,
              users = allUsers,
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
    }
  }

  // Payment Verification Dialog
  showVerifyDialog?.let { payment ->
    AlertDialog(
      onDismissRequest = { showVerifyDialog = null },
      title = { Text(AppStrings.verifyPayment, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Member: ${payment.memberName} (${payment.memberCategory})")
          Text("Amount: ETB ${payment.amountPaid}")
          Text("Month: ${payment.paymentMonth}")
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = receiptNumberInput,
            onValueChange = { receiptNumberInput = it },
            label = { Text(AppStrings.receiptNumber) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("receipt_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.verifyPayment(payment.id, receiptNumberInput)
            showVerifyDialog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("confirm_verify_button")
        ) {
          Text(AppStrings.verifyPayment)
        }
      },
      dismissButton = {
        TextButton(onClick = { showVerifyDialog = null }) {
          Text(AppStrings.cancel)
        }
      }
    )
  }

  // Rate Editing Dialog
  showRateDialog?.let { setting ->
    AlertDialog(
      onDismissRequest = { showRateDialog = null },
      title = { Text("Edit Monthly Dues (${setting.nameEn})", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Current: ETB ${setting.amount}")
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = newAmountInput,
            onValueChange = { newAmountInput = it },
            label = { Text("New Monthly Amount (ETB)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("amount_setting_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = newAmountInput.toDoubleOrNull() ?: setting.amount
            viewModel.updatePaymentSetting(setting.category, amount)
            showRateDialog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
        ) {
          Text(AppStrings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showRateDialog = null }) {
          Text(AppStrings.cancel)
        }
      }
    )
  }

  // Add Member Dialog
  if (showAddMemberDialog) {
    AddMemberDialog(
      onDismiss = { showAddMemberDialog = false },
      onConfirm = { name, phone, email, gender, cat, grade, confessor, addr ->
        viewModel.addMember(name, phone, email, gender, cat, grade, 5, confessor, addr)
        showAddMemberDialog = false
      }
    )
  }

  // Add Announcement Dialog
  if (showAnnouncementDialog) {
    AddAnnouncementDialog(
      onDismiss = { showAnnouncementDialog = false },
      onConfirm = { tEn, tAm, bEn, bAm, target, pin ->
        viewModel.postAnnouncement(tEn, tAm, bEn, bAm, target, pin)
        showAnnouncementDialog = false
      }
    )
  }

  // Export Statistics Dialog
  if (showExportDialog) {
    ExportDataDialog(
      members = members,
      payments = payments,
      plans = plans,
      departments = departments,
      attendance = attendance,
      onDismiss = { showExportDialog = false }
    )
  }

  // Edit Member Dialog (Manager Full Edit Access)
  showEditMemberDialog?.let { member ->
    var fullName by remember(member) { mutableStateOf(member.fullName) }
    var phone by remember(member) { mutableStateOf(member.phone) }
    var email by remember(member) { mutableStateOf(member.email) }
    var category by remember(member) { mutableStateOf(member.memberCategory) }
    var classGrade by remember(member) { mutableStateOf(member.classGrade) }
    var confessorFather by remember(member) { mutableStateOf(member.confessorFather) }
    var status by remember(member) { mutableStateOf(member.status) }
    var uniformAccess by remember(member) { mutableStateOf(member.uniformAccessApproved) }

    AlertDialog(
      onDismissRequest = { showEditMemberDialog = null },
      title = { Text("Edit Member Profile (${member.memberId})", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth().testTag("edit_member_name")
          )
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = classGrade,
            onValueChange = { classGrade = it },
            label = { Text("Class / Grade") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = confessorFather,
            onValueChange = { confessorFather = it },
            label = { Text("Confessor Father (የንስሐ አባት)") },
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(4.dp))
          Text("Member Category:", style = MaterialTheme.typography.labelMedium)
          listOf("Worker", "StudentWorker", "NonWorker").forEach { cat ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth().clickable { category = cat }
            ) {
              RadioButton(selected = category == cat, onClick = { category = cat })
              Text(cat, fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text("Membership Status:", style = MaterialTheme.typography.labelMedium)
          Row {
            listOf("Active", "Inactive").forEach { s ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { status = s }.padding(end = 12.dp)
              ) {
                RadioButton(selected = status == s, onClick = { status = s })
                Text(s, fontSize = 12.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchPaleGold.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Holiday Uniform Access", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ChurchDeepBlue)
                Text("Allow member to wear vestments for feast services", fontSize = 10.sp, color = ChurchTextMuted)
              }
              Switch(
                checked = uniformAccess,
                onCheckedChange = { uniformAccess = it },
                modifier = Modifier.testTag("edit_member_uniform_switch")
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateMember(
              member.copy(
                fullName = fullName,
                phone = phone,
                email = email,
                memberCategory = category,
                classGrade = classGrade,
                confessorFather = confessorFather,
                status = status,
                uniformAccessApproved = uniformAccess
              )
            )
            showEditMemberDialog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("save_edit_member_btn")
        ) {
          Text(AppStrings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditMemberDialog = null }) { Text(AppStrings.cancel) }
      }
    )
  }

  // Edit Department Dialog (Manager Full Edit Access)
  showEditDeptDialog?.let { dept ->
    var nameEn by remember(dept) { mutableStateOf(dept.nameEn) }
    var nameAm by remember(dept) { mutableStateOf(dept.nameAm) }
    var descriptionEn by remember(dept) { mutableStateOf(dept.descriptionEn) }
    var descriptionAm by remember(dept) { mutableStateOf(dept.descriptionAm) }

    AlertDialog(
      onDismissRequest = { showEditDeptDialog = null },
      title = { Text("Edit Department (${dept.code})", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = nameEn,
            onValueChange = { nameEn = it },
            label = { Text("Department Name (English)") },
            modifier = Modifier.fillMaxWidth().testTag("edit_dept_name_en")
          )
          OutlinedTextField(
            value = nameAm,
            onValueChange = { nameAm = it },
            label = { Text("Department Name (Amharic / አማርኛ)") },
            modifier = Modifier.fillMaxWidth().testTag("edit_dept_name_am")
          )
          OutlinedTextField(
            value = descriptionEn,
            onValueChange = { descriptionEn = it },
            label = { Text("Responsibilities / Mandate (English)") },
            modifier = Modifier.fillMaxWidth().height(90.dp)
          )
          OutlinedTextField(
            value = descriptionAm,
            onValueChange = { descriptionAm = it },
            label = { Text("Responsibilities / Mandate (Amharic)") },
            modifier = Modifier.fillMaxWidth().height(90.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateDepartment(
              dept.copy(
                nameEn = nameEn,
                nameAm = nameAm,
                descriptionEn = descriptionEn,
                descriptionAm = descriptionAm
              )
            )
            showEditDeptDialog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("save_edit_dept_btn")
        ) {
          Text(AppStrings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditDeptDialog = null }) { Text(AppStrings.cancel) }
      }
    )
  }

  // Manager User & Login Credential Editor Dialog (Create or Edit Accounts & Passwords)
  if (showManageUserDialog != null || showCreateNewUserDialog) {
    val existing = showManageUserDialog
    var fullName by remember(existing) { mutableStateOf(existing?.fullName ?: "") }
    var email by remember(existing) { mutableStateOf(existing?.email ?: "") }
    var newPassword by remember(existing) { mutableStateOf("") }
    var phone by remember(existing) { mutableStateOf(existing?.phone ?: "+2519") }
    var role by remember(existing) { mutableStateOf(existing?.role ?: "DeptAdmin") }
    var deptId by remember(existing) { mutableStateOf(existing?.departmentId ?: 1L) }
    var isApproved by remember(existing) { mutableStateOf(existing?.isApproved ?: true) }
    var isActive by remember(existing) { mutableStateOf(existing?.isActive ?: true) }
    var validationError by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = {
        showManageUserDialog = null
        showCreateNewUserDialog = false
      },
      title = {
        Text(
          text = if (existing == null) "Create User Account / አዲስ መለያ" else "Edit User & Password (${existing.email})",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; validationError = "" },
            label = { Text("Full Name / ሙሉ ስም") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("manage_user_full_name")
          )
          OutlinedTextField(
            value = email,
            onValueChange = { email = it; validationError = "" },
            label = { Text("Login Email / የመግቢያ ኢሜይል") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("manage_user_email")
          )
          OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it; validationError = "" },
            label = {
              Text(
                if (existing == null) "Password (min 6 chars) / የይለፍ ቃል"
                else "New Password (leave blank to keep current)"
              )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("manage_user_password")
          )
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number / ስልክ") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("manage_user_phone")
          )

          Text("Role / ኃላፊነት:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Manager", "DeptAdmin", "Member").forEach { r ->
              FilterChip(
                selected = role == r,
                onClick = { role = r },
                label = { Text(r, fontSize = 11.sp) },
                modifier = Modifier.testTag("role_chip_$r")
              )
            }
          }

          if (role == "DeptAdmin" || role == "Member") {
            Text("Assigned Department / ክፍል:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            departments.forEach { d ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { deptId = d.id }
                  .padding(vertical = 2.dp)
              ) {
                RadioButton(selected = deptId == d.id, onClick = { deptId = d.id })
                Text("${d.code} - ${d.nameEn}", fontSize = 12.sp)
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Manager Approved / የጸደቀ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Switch(checked = isApproved, onCheckedChange = { isApproved = it })
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Account Active / ንቁ መለያ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Switch(checked = isActive, onCheckedChange = { isActive = it })
          }

          if (validationError.isNotEmpty()) {
            Text(validationError, color = ChurchRed, fontSize = 11.sp)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (email.isBlank() || fullName.isBlank()) {
              validationError = "Email and Full Name are required."
              return@Button
            }
            if (existing == null && newPassword.isBlank()) {
              validationError = "Please set an initial password for the new account."
              return@Button
            }
            viewModel.saveManagedUserAccount(
              existingUser = existing,
              email = email,
              plainPassword = newPassword,
              fullName = fullName,
              phone = phone,
              role = role,
              departmentId = if (role == "Manager") null else deptId,
              isApproved = isApproved,
              isActive = isActive
            ) { success, err ->
              if (success) {
                showManageUserDialog = null
                showCreateNewUserDialog = false
              } else {
                validationError = err
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("save_managed_user_btn")
        ) {
          Text("Save & Sync Firebase")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showManageUserDialog = null
            showCreateNewUserDialog = false
          }
        ) {
          Text(AppStrings.cancel)
        }
      }
    )
  }

  // Edit Payment Dialog (Manager Full Edit Access)
  showEditPaymentDialog?.let { payment ->
    var amountText by remember(payment) { mutableStateOf(payment.amountPaid.toInt().toString()) }
    var month by remember(payment) { mutableStateOf(payment.paymentMonth) }
    var method by remember(payment) { mutableStateOf(payment.paymentMethod) }
    var receipt by remember(payment) { mutableStateOf(payment.receiptNumber) }
    var isVerified by remember(payment) { mutableStateOf(payment.verified) }

    AlertDialog(
      onDismissRequest = { showEditPaymentDialog = null },
      title = { Text("Edit Payment (${payment.memberName})", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount Paid (ETB)") },
            modifier = Modifier.fillMaxWidth().testTag("edit_payment_amount")
          )
          OutlinedTextField(
            value = month,
            onValueChange = { month = it },
            label = { Text("Payment Month") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = method,
            onValueChange = { method = it },
            label = { Text("Payment Method") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = receipt,
            onValueChange = { receipt = it },
            label = { Text(AppStrings.receiptNumber) },
            modifier = Modifier.fillMaxWidth()
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Payment Verified", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Switch(checked = isVerified, onCheckedChange = { isVerified = it })
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amt = amountText.toDoubleOrNull() ?: payment.amountPaid
            viewModel.updatePayment(
              payment.copy(
                amountPaid = amt,
                paymentMonth = month,
                paymentMethod = method,
                receiptNumber = receipt,
                verified = isVerified
              )
            )
            showEditPaymentDialog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("save_edit_payment_btn")
        ) {
          Text(AppStrings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditPaymentDialog = null }) { Text(AppStrings.cancel) }
      }
    )
  }

  // Edit Plan Dialog (Manager Full Edit Access)
  showEditPlanDialog?.let { plan ->
    var title by remember(plan) { mutableStateOf(plan.title) }
    var target by remember(plan) { mutableStateOf(plan.target) }
    var budgetText by remember(plan) { mutableStateOf(plan.budget.toInt().toString()) }
    var status by remember(plan) { mutableStateOf(plan.status) }

    AlertDialog(
      onDismissRequest = { showEditPlanDialog = null },
      title = { Text("Edit Departmental Plan", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Plan Title") },
            modifier = Modifier.fillMaxWidth().testTag("edit_plan_title")
          )
          OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("Target Objective") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = budgetText,
            onValueChange = { budgetText = it },
            label = { Text("Allocated Budget (ETB)") },
            modifier = Modifier.fillMaxWidth()
          )
          Text("Status:", style = MaterialTheme.typography.labelMedium)
          listOf("Pending", "Approved", "In Progress", "Completed").forEach { s ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth().clickable { status = s }
            ) {
              RadioButton(selected = status == s, onClick = { status = s })
              Text(s, fontSize = 12.sp)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val bg = budgetText.toDoubleOrNull() ?: plan.budget
            viewModel.updatePlan(
              plan.copy(
                title = title,
                target = target,
                budget = bg,
                status = status
              )
            )
            showEditPlanDialog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("save_edit_plan_btn")
        ) {
          Text(AppStrings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditPlanDialog = null }) { Text(AppStrings.cancel) }
      }
    )
  }

  // Edit Announcement Dialog (Manager Full Edit Access)
  showEditAnnouncementDialog?.let { item ->
    var titleEn by remember(item) { mutableStateOf(item.titleEn) }
    var titleAm by remember(item) { mutableStateOf(item.titleAm) }
    var bodyEn by remember(item) { mutableStateOf(item.bodyEn) }
    var bodyAm by remember(item) { mutableStateOf(item.bodyAm) }
    var audience by remember(item) { mutableStateOf(item.targetAudience) }
    var isPinned by remember(item) { mutableStateOf(item.isPinned) }

    AlertDialog(
      onDismissRequest = { showEditAnnouncementDialog = null },
      title = { Text("Edit Announcement", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = titleAm,
            onValueChange = { titleAm = it },
            label = { Text("Title (Amharic)") },
            modifier = Modifier.fillMaxWidth().testTag("edit_announcement_title_am")
          )
          OutlinedTextField(
            value = titleEn,
            onValueChange = { titleEn = it },
            label = { Text("Title (English)") },
            modifier = Modifier.fillMaxWidth().testTag("edit_announcement_title_en")
          )
          OutlinedTextField(
            value = bodyAm,
            onValueChange = { bodyAm = it },
            label = { Text("Content (Amharic)") },
            modifier = Modifier.fillMaxWidth().height(90.dp)
          )
          OutlinedTextField(
            value = bodyEn,
            onValueChange = { bodyEn = it },
            label = { Text("Content (English)") },
            modifier = Modifier.fillMaxWidth().height(90.dp)
          )
          OutlinedTextField(
            value = audience,
            onValueChange = { audience = it },
            label = { Text("Audience (e.g. All, Members, Staff)") },
            modifier = Modifier.fillMaxWidth()
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Pin to Top", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Switch(checked = isPinned, onCheckedChange = { isPinned = it })
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateAnnouncement(
              item.copy(
                titleEn = titleEn,
                titleAm = titleAm,
                bodyEn = bodyEn,
                bodyAm = bodyAm,
                targetAudience = audience,
                isPinned = isPinned
              )
            )
            showEditAnnouncementDialog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("save_edit_announcement_btn")
        ) {
          Text(AppStrings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditAnnouncementDialog = null }) { Text(AppStrings.cancel) }
      }
    )
  }
}

@Composable
fun ManagerOverviewTab(
  members: List<MemberItem>,
  payments: List<PaymentItem>,
  plans: List<PlanItem>,
  feedbackList: List<FeedbackItem>,
  attendance: List<AttendanceRecord> = emptyList(),
  users: List<User> = emptyList(),
  onOpenExport: () -> Unit = {},
  onNavigateToDeptsTab: () -> Unit = {}
) {
  val verifiedPayments = payments.filter { it.verified }
  val totalIncome = verifiedPayments.sumOf { it.amountPaid }
  val activeMembersCount = members.count { it.status == "Active" }
  val pendingTasks = plans.count { it.status == "Pending" } +
    payments.count { !it.verified } +
    feedbackList.count { it.status == "Pending" }

  // High-level statistics calculation
  val totalAttRecords = attendance.size
  val presentOrLateCount = attendance.count { it.status == "Present" || it.status == "Late" }
  val weeklyAttendancePercentage = if (totalAttRecords > 0) ((presentOrLateCount.toDouble() / totalAttRecords) * 100).toInt() else 85
  val pendingDeptAdminApprovals = users.count { it.role == "DeptAdmin" && (!it.isApproved || it.approvalStatus == "PENDING") }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Sliding Marquee Announcements
    item {
      ChurchSlidingTicker()
    }

    // HIGH-LEVEL SUMMARY VIEW: Total active members, % attendance this week, pending approval requests for department admins
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, ChurchPaleGold),
        modifier = Modifier.fillMaxWidth().testTag("executive_high_level_summary_view")
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
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "የአስተዳደር ፈጣን ማጠቃለያ (Executive Summary)",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = ChurchDarkBlue
                )
                Text(
                  text = "Live Key Performance Indicators & Sunday School Health",
                  style = MaterialTheme.typography.labelSmall,
                  color = ChurchTextMuted
                )
              }
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ChurchPaleGold,
              modifier = Modifier.clickable { onOpenExport() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = ChurchDarkGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export Data", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ChurchDarkGold)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = Color(0xFFF1F5F9))
          Spacer(modifier = Modifier.height(14.dp))

          // 3 Critical Metric Widgets
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
                  Spacer(modifier = Modifier.width(5.dp))
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
                  text = "ከ ${members.size} አጠቃላይ አባላት",
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
                    text = "Attendance Week",
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
                  text = "የዚህ ሳምንት አቴንዳንስ",
                  fontSize = 9.sp,
                  color = ChurchTextSecondary,
                  maxLines = 1
                )
              }
            }

            // Metric 3: Pending Approval Requests for Department Admins
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (pendingDeptAdminApprovals > 0) Color(0xFFFEF2F2) else Color(0xFFF8FAFC),
              border = BorderStroke(
                1.dp,
                if (pendingDeptAdminApprovals > 0) ChurchRed.copy(alpha = 0.4f) else Color(0xFFE2E8F0)
              ),
              modifier = Modifier
                .weight(1f)
                .clickable { onNavigateToDeptsTab() }
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (pendingDeptAdminApprovals > 0) Icons.Default.PendingActions else Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = if (pendingDeptAdminApprovals > 0) ChurchRed else ChurchGreen,
                    modifier = Modifier.size(11.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Admin Approvals",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (pendingDeptAdminApprovals > 0) ChurchRed else ChurchTextSecondary
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "$pendingDeptAdminApprovals",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (pendingDeptAdminApprovals > 0) ChurchRed else ChurchDarkBlue
                )
                Text(
                  text = if (pendingDeptAdminApprovals > 0) "ያልጸደቁ ኃላፊዎች" else "ሁሉም ጸድቀዋል ✅",
                  fontSize = 9.sp,
                  color = if (pendingDeptAdminApprovals > 0) ChurchRed else ChurchGreen,
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }

    // KPI Cards Grid
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        KpiCard(
          title = "Total Members",
          value = "${members.size}",
          subtitle = "$activeMembersCount Active",
          icon = Icons.Default.Groups,
          modifier = Modifier.weight(1f)
        )
        KpiCard(
          title = AppStrings.totalIncome,
          value = "${totalIncome.toInt()} ETB",
          subtitle = "Verified",
          icon = Icons.Default.AccountBalanceWallet,
          iconTint = ChurchGreen,
          iconBg = Color(0xFFDCFCE7),
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        KpiCard(
          title = "Pending Tasks",
          value = "$pendingTasks",
          subtitle = "Action needed",
          icon = Icons.Default.PendingActions,
          iconTint = ChurchAmber,
          iconBg = Color(0xFFFEF3C7),
          modifier = Modifier.weight(1f)
        )
        KpiCard(
          title = "Verified Dues",
          value = "${verifiedPayments.size} / ${payments.size}",
          subtitle = "Reconciled",
          icon = Icons.Default.CheckCircle,
          iconTint = ChurchDeepBlue,
          iconBg = Color(0xFFE0E7FF),
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Export Data & Reports Banner
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchPaleGold),
        modifier = Modifier.fillMaxWidth().clickable { onOpenExport() }.testTag("export_banner_card")
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = ChurchGold,
            modifier = Modifier.size(40.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.FileDownload, contentDescription = "Export", tint = ChurchDarkBlue)
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Export Data & Statistics / ዳታና ሪፖርት ማውረጃ",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = ChurchDarkBlue
            )
            Text(
              text = "Download Executive Audit, Payments CSV, and Member Rosters",
              style = MaterialTheme.typography.bodySmall,
              color = ChurchTextPrimary
            )
          }
          FilledTonalButton(
            onClick = onOpenExport,
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = ChurchDarkBlue, contentColor = Color.White)
          ) {
            Text("Export", fontSize = 12.sp)
          }
        }
      }
    }

    // Chart 1: Monthly Income Bar Chart
    item {
      val monthlyData = listOf(
        BarChartItem("Hamle", 18500f, ChartBlue),
        BarChartItem("Nehase", 22400f, ChartBlue),
        BarChartItem("Pagumen", 9800f, ChartGold),
        BarChartItem("Meskerem", 28500f, ChartTeal)
      )
      SimpleBarChart(
        title = AppStrings.monthlyIncomeChart,
        subtitle = "Verified collections by Ethiopian month (ETB)",
        items = monthlyData,
        valueFormatter = { "${(it / 1000).toInt()}k" }
      )
    }

    // Chart 2: Income by Category Pie/Donut Chart
    item {
      val workerIncome = verifiedPayments.filter { it.memberCategory == "Worker" }.sumOf { it.amountPaid }.toFloat().coerceAtLeast(100f)
      val studentWorkerIncome = verifiedPayments.filter { it.memberCategory == "StudentWorker" }.sumOf { it.amountPaid }.toFloat().coerceAtLeast(50f)
      val nonWorkerIncome = verifiedPayments.filter { it.memberCategory == "NonWorker" }.sumOf { it.amountPaid }.toFloat().coerceAtLeast(20f)

      val categorySlices = listOf(
        PieChartSlice(AppStrings.workerMember, workerIncome, ChartBlue),
        PieChartSlice(AppStrings.studentWorker, studentWorkerIncome, ChartGold),
        PieChartSlice(AppStrings.nonWorker, nonWorkerIncome, ChartEmerald)
      )
      DonutDistributionChart(
        title = AppStrings.incomeByCategory,
        slices = categorySlices
      )
    }

    // Chart 3: Paid vs Unpaid Members
    item {
      val paidCount = payments.count { it.verified }.toFloat()
      val unpaidCount = (members.size - paidCount).coerceAtLeast(1f)
      ComparisonBarChart(
        title = AppStrings.paidVsUnpaid,
        firstLabel = AppStrings.paidMembers,
        firstValue = paidCount,
        firstColor = ChurchGreen,
        secondLabel = AppStrings.unpaidMembers,
        secondValue = unpaidCount,
        secondColor = ChurchRed
      )
    }

    // Chart 4: Yearly Income Trend (Line Chart)
    item {
      SimpleLineTrendChart(
        title = "Yearly Income Trend (3-Year Growth)",
        subtitle = "Annual Sunday School revenue in thousands ETB",
        values = listOf(140f, 195f, 280f, 345f),
        labels = listOf("2016 E.C.", "2017 E.C.", "2018 E.C.", "2019 E.C. (Est)"),
        lineColor = ChurchDeepBlue
      )
    }

    // Chart 5: Property Value Over Time (Line Chart)
    item {
      SimpleLineTrendChart(
        title = "Sunday School Property & Assets Value",
        subtitle = "Audio systems, sacred books, vestments & instruments (ETB in '000)",
        values = listOf(180f, 240f, 310f, 420f),
        labels = listOf("2016", "2017", "2018", "2019"),
        lineColor = ChurchGold
      )
    }

    // Chart 6: Budget vs Actual Spending Bar Chart
    item {
      val budgetItems = listOf(
        BarChartItem("Education", 35000f, ChartBlue),
        BarChartItem("Program", 85000f, ChartGold),
        BarChartItem("Song/Arts", 42000f, ChartTeal),
        BarChartItem("HR/Youth", 15000f, ChartEmerald)
      )
      SimpleBarChart(
        title = AppStrings.budgetVsActual,
        subtitle = "Departmental planned budgets for 2019 E.C.",
        items = budgetItems,
        valueFormatter = { "${(it / 1000).toInt()}k" }
      )
    }
  }
}

@Composable
fun ManagerFinanceTab(
  payments: List<PaymentItem>,
  paymentSettings: List<PaymentSetting>,
  onVerifyPayment: (PaymentItem) -> Unit,
  onEditRate: (PaymentSetting) -> Unit,
  onEditPayment: (PaymentItem) -> Unit = {},
  onDeletePayment: (Long) -> Unit = {}
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Payment Settings Section (Manager control)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Payment Rates by Category (Manager Editable)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ChurchDeepBlue
          )
          Text(
            text = "Set standard monthly dues for Sunday school members:",
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
          )

          paymentSettings.forEach { setting ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(setting.nameEn, fontWeight = FontWeight.SemiBold, color = ChurchTextPrimary)
                Text(setting.nameAm, style = MaterialTheme.typography.labelSmall, color = ChurchTextSecondary)
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = ChurchPaleGold
                ) {
                  Text(
                    text = "ETB ${setting.amount.toInt()} / mo",
                    color = ChurchDarkGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { onEditRate(setting) }) {
                  Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ChurchDeepBlue)
                }
              }
            }
          }
        }
      }
    }

    item {
      Text(
        text = "All Member Payments & Verifications",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue,
        modifier = Modifier.padding(top = 8.dp)
      )
    }

    items(payments) { payment ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = payment.memberName,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = ChurchTextPrimary
            )
            Text(
              text = "${payment.memberCategory} • ${payment.paymentMonth}",
              style = MaterialTheme.typography.labelSmall,
              color = ChurchTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "ETB ${payment.amountPaid.toInt()} via ${payment.paymentMethod}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = ChurchDeepBlue
              )
              if (payment.receiptNumber.isNotEmpty()) {
                Text(
                  text = " (${payment.receiptNumber})",
                  style = MaterialTheme.typography.labelSmall,
                  color = ChurchTextMuted
                )
              }
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            StatusBadge(if (payment.verified) "Verified" else "Pending Verification")
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (!payment.verified) {
                Button(
                  onClick = { onVerifyPayment(payment) },
                  colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.testTag("verify_${payment.id}").height(28.dp)
                ) {
                  Text(AppStrings.verifyPayment, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
              }
              IconButton(onClick = { onEditPayment(payment) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Payment", tint = ChurchDeepBlue, modifier = Modifier.size(16.dp))
              }
              IconButton(onClick = { onDeletePayment(payment.id) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Payment", tint = ChurchRed, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ManagerMembersTab(
  members: List<MemberItem>,
  onAddMember: () -> Unit,
  onEditMember: (MemberItem) -> Unit = {},
  onDeleteMember: (Long) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val filteredMembers = members.filter {
    it.fullName.contains(searchQuery, ignoreCase = true) ||
      it.memberId.contains(searchQuery, ignoreCase = true) ||
      it.phone.contains(searchQuery)
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search students by name or registration ID... / በስም ወይም በመለያ ይፈልጉ", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ChurchDeepBlue) },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(
                  onClick = { searchQuery = "" },
                  modifier = Modifier.testTag("clear_member_search_btn")
                ) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = ChurchTextSecondary)
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).testTag("member_search_input")
          )
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = onAddMember,
            colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("add_member_button")
          ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add")
          }
        }
        if (searchQuery.isNotEmpty()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Showing ${filteredMembers.size} of ${members.size} students matching \"$searchQuery\"",
              style = MaterialTheme.typography.labelSmall,
              color = ChurchDeepBlue,
              fontWeight = FontWeight.SemiBold
            )
            TextButton(
              onClick = { searchQuery = "" },
              contentPadding = PaddingValues(0.dp)
            ) {
              Text("Reset filter", fontSize = 11.sp, color = ChurchRed)
            }
          }
        }
      }
    }

    items(filteredMembers) { member ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = member.fullName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ChurchTextPrimary
              )
              Text(
                text = "${member.memberId} • ${member.classGrade}",
                style = MaterialTheme.typography.labelSmall,
                color = ChurchTextSecondary
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (member.uniformAccessApproved) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = ChurchGreen.copy(alpha = 0.15f),
                  modifier = Modifier.padding(end = 6.dp)
                ) {
                  Text(
                    text = "Uniform OK",
                    color = ChurchGreen,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
              StatusBadge(member.status)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Category: ${member.memberCategory}", style = MaterialTheme.typography.bodySmall, color = ChurchDeepBlue, fontWeight = FontWeight.SemiBold)
            Text("Father: ${member.confessorFather}", style = MaterialTheme.typography.bodySmall, color = ChurchTextMuted)
          }

          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Phone: ${member.phone}", style = MaterialTheme.typography.labelSmall, color = ChurchTextSecondary)
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = { onEditMember(member) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Member", tint = ChurchDeepBlue, modifier = Modifier.size(16.dp))
              }
              IconButton(onClick = { onDeleteMember(member.id) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Member", tint = ChurchRed, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ManagerDepartmentsTab(
  departments: List<Department>,
  users: List<User>,
  onEditDepartment: (Department) -> Unit = {},
  onSetAdminApprovalStatus: (Long, Boolean, String) -> Unit = { _, _, _ -> },
  onNavigateToSongAttendance: () -> Unit = {},
  onAddUserAccount: () -> Unit = {},
  onEditUserCredentials: (User) -> Unit = {},
  onDeleteUserAccount: (Long) -> Unit = {},
  cloudSyncStatus: String = "Active",
  onSyncFirestoreNow: () -> Unit = {},
  onOpenDataCenter: () -> Unit = {}
) {
  val deptAdmins = users.filter { it.role == "DeptAdmin" }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // All-Departments Data Center, Firestore Sync & Security Audit Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchDarkBlue),
        border = BorderStroke(1.dp, ChurchGold)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "ALL ${departments.size} DEPARTMENTS • DATA CENTER & SECURITY AUDIT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = ChurchLightGold
              )
              Text(
                text = "Room SQLite + Cloud Firestore Sync & 8-Point Security",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
              )
              Text(
                text = "Status: $cloudSyncStatus",
                fontSize = 11.sp,
                color = ChurchPaleGold
              )
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = onSyncFirestoreNow,
              colors = ButtonDefaults.buttonColors(containerColor = ChurchGold),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("manager_sync_firestore_btn")
            ) {
              Icon(Icons.Default.Sync, contentDescription = null, tint = ChurchDarkBlue, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Sync All Depts", color = ChurchDarkBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            OutlinedButton(
              onClick = onOpenDataCenter,
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, ChurchLightGold),
              modifier = Modifier.weight(1f).testTag("manager_open_data_center_card_btn")
            ) {
              Icon(Icons.Default.Security, contentDescription = null, tint = ChurchLightGold, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Data Center & Audit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }
    }

    // 0. Firebase & Room User Credentials Management Banner (Editable by Manager)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchDeepBlue)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "FIREBASE & ROOM CREDENTIAL MANAGER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = ChurchLightGold
              )
              Text(
                text = "User & Password Management (የተጠቃሚ መለያዎች)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Create or edit login emails, passwords, roles & department assignments (${users.size} accounts)",
                fontSize = 11.sp,
                color = ChurchPaleGold
              )
            }
            Button(
              onClick = onAddUserAccount,
              colors = ButtonDefaults.buttonColors(containerColor = ChurchGold),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("manager_add_user_account_btn")
            ) {
              Icon(Icons.Default.PersonAdd, contentDescription = null, tint = ChurchDarkBlue, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("New Account", color = ChurchDarkBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }
    }

    // All Manager-Editable User Accounts (Managers, DeptAdmins, Members)
    items(users) { account ->
      val assignedDept = departments.find { it.id == account.departmentId }
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ChurchBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = account.fullName,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ChurchDeepBlue
              )
              Text(
                text = "${account.email} • Role: ${account.role}${if (assignedDept != null) " (${assignedDept.code})" else ""}",
                fontSize = 11.sp,
                color = ChurchTextSecondary
              )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
              OutlinedButton(
                onClick = { onEditUserCredentials(account) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp).testTag("edit_user_credentials_${account.id}")
              ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = ChurchDarkGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Login / Password", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
              }
              if (account.role != "Manager") {
                IconButton(
                  onClick = { onDeleteUserAccount(account.id) },
                  modifier = Modifier.size(28.dp).testTag("delete_user_account_${account.id}")
                ) {
                  Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Account", tint = ChurchRed, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
      }
    }

    // 1. Department Admin Approvals Management
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchPaleGold.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ChurchDeepBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Department Admins Approval & Access Control",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ChurchDeepBlue
              )
              Text(
                text = "የክፍል ኃላፊዎች ማረጋገጫና አስተዳዳሪ ፈቃድ",
                style = MaterialTheme.typography.bodySmall,
                color = ChurchTextSecondary
              )
            }
          }
          Text(
            text = "Department admins must be verified and approved by Executive Managers before gaining access to departmental tools.",
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextMuted,
            modifier = Modifier.padding(top = 6.dp)
          )
        }
      }
    }

    item {
      Text(
        text = "Registered Department Admin Accounts (${deptAdmins.size})",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue
      )
    }

    items(deptAdmins) { admin ->
      val adminDept = departments.find { it.id == admin.departmentId }
      val approvalStatus = admin.approvalStatus ?: if (admin.isApproved) "APPROVED" else "PENDING"

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
            Column(modifier = Modifier.weight(1f)) {
              Text(admin.fullName, fontWeight = FontWeight.Bold, color = ChurchTextPrimary)
              Text("${admin.email} • ${adminDept?.nameEn ?: "Department Admin"}", style = MaterialTheme.typography.labelSmall, color = ChurchTextSecondary)
              if (admin.approvedBy != null) {
                Text("Approved by: ${admin.approvedBy}", fontSize = 10.sp, color = ChurchTextMuted)
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = when (approvalStatus) {
                "APPROVED" -> ChurchGreenBg
                "REJECTED" -> ChurchRedBg
                else -> ChurchAmberBg
              }
            ) {
              Text(
                text = when (approvalStatus) {
                  "APPROVED" -> "✅ Verified & Approved"
                  "REJECTED" -> "⚠️ Suspended"
                  else -> "⏳ Pending Approval"
                },
                color = when (approvalStatus) {
                  "APPROVED" -> ChurchGreen
                  "REJECTED" -> ChurchRed
                  else -> ChurchDarkGold
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
          ) {
            if (approvalStatus != "APPROVED") {
              Button(
                onClick = { onSetAdminApprovalStatus(admin.id, true, "APPROVED") },
                colors = ButtonDefaults.buttonColors(containerColor = ChurchGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp).testTag("approve_admin_${admin.id}")
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Approve / አጽድቅ", fontSize = 11.sp)
              }
            }

            if (approvalStatus != "PENDING") {
              OutlinedButton(
                onClick = { onSetAdminApprovalStatus(admin.id, false, "PENDING") },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChurchGold),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp).testTag("pending_admin_${admin.id}")
              ) {
                Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Set Pending / ተጠባባቂ", fontSize = 11.sp)
              }
            }

            if (approvalStatus != "REJECTED") {
              OutlinedButton(
                onClick = { onSetAdminApprovalStatus(admin.id, false, "REJECTED") },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChurchRed),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp).testTag("revoke_admin_${admin.id}")
              ) {
                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Suspend / አግድ", fontSize = 11.sp)
              }
            }
          }
        }
      }
    }

    // 2. 7 Executive Departments
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Executive Departments (7 Admins + Office - Editable)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue
      )
    }

    items(departments) { dept ->
      val headUser = users.find { it.id == dept.headUserId }
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${dept.code} • ${dept.nameEn}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = ChurchDeepBlue
            )
            IconButton(onClick = { onEditDepartment(dept) }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Edit, contentDescription = "Edit Department", tint = ChurchDeepBlue, modifier = Modifier.size(18.dp))
            }
          }
          Text(
            text = dept.nameAm,
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextSecondary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = dept.descriptionEn,
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextMuted
          )
          Spacer(modifier = Modifier.height(8.dp))

          // If Song Department, provide quick launcher to Song Attendance Screen
          if (dept.code == "SNG") {
            Button(
              onClick = onNavigateToSongAttendance,
              colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().testTag("manager_open_song_attendance_btn"),
              contentPadding = PaddingValues(vertical = 4.dp)
            ) {
              Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Open Song Choir Attendance & Roster (የመዝሙር ክፍል አቴንዳንስ)", fontSize = 11.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Department Head: ${headUser?.fullName ?: "Assigned Admin"}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = ChurchGreen
            )
            OutlinedButton(
              onClick = { onEditDepartment(dept) },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp).testTag("edit_dept_${dept.code}")
            ) {
              Text("Edit Details", fontSize = 10.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun ManagerPlansReportsTab(
  plans: List<PlanItem>,
  reports: List<ReportItem>,
  onApprovePlan: (Long) -> Unit,
  onApproveReport: (ReportItem) -> Unit,
  onEditPlan: (PlanItem) -> Unit = {},
  onDeletePlan: (Long) -> Unit = {}
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "Annual Department Plans for Approval",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue
      )
    }

    items(plans) { plan ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = plan.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = ChurchTextPrimary,
              modifier = Modifier.weight(1f)
            )
            StatusBadge(plan.status)
          }
          Text(
            text = "Dept: ${plan.deptName} • Budget: ETB ${plan.budget.toInt()}",
            style = MaterialTheme.typography.bodySmall,
            color = ChurchDeepBlue,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Target: ${plan.target}",
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextMuted,
            modifier = Modifier.padding(vertical = 4.dp)
          )

          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = { onEditPlan(plan) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Plan", tint = ChurchDeepBlue, modifier = Modifier.size(16.dp))
              }
              IconButton(onClick = { onDeletePlan(plan.id) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Plan", tint = ChurchRed, modifier = Modifier.size(16.dp))
              }
            }

            if (plan.status == "Pending") {
              Button(
                onClick = { onApprovePlan(plan.id) },
                colors = ButtonDefaults.buttonColors(containerColor = ChurchGreen),
                modifier = Modifier.testTag("approve_plan_${plan.id}")
              ) {
                Text("Approve Plan")
              }
            }
          }
        }
      }
    }

    item {
      Text(
        text = "Submitted Department Reports",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue,
        modifier = Modifier.padding(top = 12.dp)
      )
    }

    items(reports) { report ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = report.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = ChurchTextPrimary,
              modifier = Modifier.weight(1f)
            )
            StatusBadge(report.status)
          }
          Text(
            text = "${report.deptName} • Period: ${report.period}",
            style = MaterialTheme.typography.labelSmall,
            color = ChurchTextSecondary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = report.summary,
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextPrimary
          )

          if (report.status == "Pending") {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = { onApproveReport(report) },
              colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
              modifier = Modifier.align(Alignment.End).testTag("approve_report_${report.id}")
            ) {
              Text("Review & Approve")
            }
          }
        }
      }
    }
  }
}

@Composable
fun ManagerAnnouncementsTab(
  announcements: List<AnnouncementItem>,
  onNewAnnouncement: () -> Unit,
  onEditAnnouncement: (AnnouncementItem) -> Unit = {},
  onDeleteAnnouncement: (Long) -> Unit = {}
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Sunday School Announcements",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = ChurchDeepBlue
        )
        Button(
          onClick = onNewAnnouncement,
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("new_announcement_button")
        ) {
          Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Post")
        }
      }
    }

    items(announcements) { item ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = item.titleAm,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = ChurchDeepBlue,
              modifier = Modifier.weight(1f)
            )
            if (item.isPinned) {
              Surface(shape = RoundedCornerShape(4.dp), color = ChurchPaleGold) {
                Text("PINNED", color = ChurchDarkGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
              }
            }
          }
          Text(item.titleEn, style = MaterialTheme.typography.labelMedium, color = ChurchTextSecondary)
          Spacer(modifier = Modifier.height(6.dp))
          Text(item.bodyAm, style = MaterialTheme.typography.bodySmall, color = ChurchTextPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          Text(item.bodyEn, style = MaterialTheme.typography.bodySmall, color = ChurchTextMuted)
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Posted: ${item.datePosted} by ${item.postedBy}",
              style = MaterialTheme.typography.labelSmall,
              color = ChurchTextSecondary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = { onEditAnnouncement(item) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Announcement", tint = ChurchDeepBlue, modifier = Modifier.size(16.dp))
              }
              IconButton(onClick = { onDeleteAnnouncement(item.id) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Announcement", tint = ChurchRed, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ManagerFeedbackTab(
  feedbackList: List<FeedbackItem>,
  onRespond: (FeedbackItem, String) -> Unit
) {
  var activeFeedbackToRespond by remember { mutableStateOf<FeedbackItem?>(null) }
  var responseText by remember { mutableStateOf("") }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text(
        text = "Member Suggestions, Questions & Complaints",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue
      )
    }

    items(feedbackList) { fb ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = when (fb.type) {
                "Suggestion" -> Color(0xFFDCFCE7)
                "Question" -> Color(0xFFE0E7FF)
                else -> Color(0xFFFEE2E2)
              }
            ) {
              Text(
                text = fb.type,
                color = when (fb.type) {
                  "Suggestion" -> ChurchGreen
                  "Question" -> ChurchDeepBlue
                  else -> ChurchRed
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            StatusBadge(fb.status)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(fb.subject, fontWeight = FontWeight.Bold, color = ChurchTextPrimary)
          Text(fb.message, style = MaterialTheme.typography.bodySmall, color = ChurchTextSecondary, modifier = Modifier.padding(vertical = 4.dp))
          Text("By: ${fb.submitterName} on ${fb.date}", style = MaterialTheme.typography.labelSmall, color = ChurchTextMuted)

          if (fb.response.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = ChurchPaleGold, modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text("Response by ${fb.handledBy}:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ChurchDarkGold)
                Text(fb.response, fontSize = 12.sp, color = ChurchTextPrimary)
              }
            }
          } else {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
              onClick = { activeFeedbackToRespond = fb; responseText = "" },
              colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
              modifier = Modifier.align(Alignment.End).testTag("respond_fb_${fb.id}")
            ) {
              Text("Reply to Member")
            }
          }
        }
      }
    }
  }

  activeFeedbackToRespond?.let { fb ->
    AlertDialog(
      onDismissRequest = { activeFeedbackToRespond = null },
      title = { Text("Reply to ${fb.submitterName}", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Subject: ${fb.subject}", style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = responseText,
            onValueChange = { responseText = it },
            label = { Text("Response Message") },
            modifier = Modifier.fillMaxWidth().height(120.dp).testTag("feedback_reply_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (responseText.isNotBlank()) {
              onRespond(fb, responseText)
              activeFeedbackToRespond = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
        ) {
          Text("Send Reply")
        }
      },
      dismissButton = {
        TextButton(onClick = { activeFeedbackToRespond = null }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun AddMemberDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, String, String, String, String, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("+2519") }
  var email by remember { mutableStateOf("") }
  var gender by remember { mutableStateOf("Male") }
  var category by remember { mutableStateOf("Worker") }
  var classGrade by remember { mutableStateOf("Grade 1 - Senbet Nigest") }
  var confessor by remember { mutableStateOf("Kesis Melaku / ቀሲስ መላኩ") }
  var address by remember { mutableStateOf("Bole Arabsa Site 2") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Register New Member", fontWeight = FontWeight.Bold) },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name (English / Amharic)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_member_name")
          )
        }
        item {
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          Text("Category:", style = MaterialTheme.typography.labelMedium)
          Row {
            listOf("Worker", "StudentWorker", "NonWorker").forEach { cat ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { category = cat }.padding(end = 8.dp)
              ) {
                RadioButton(selected = category == cat, onClick = { category = cat })
                Text(cat, fontSize = 11.sp)
              }
            }
          }
        }
        item {
          OutlinedTextField(
            value = classGrade,
            onValueChange = { classGrade = it },
            label = { Text("Class / Grade") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          OutlinedTextField(
            value = confessor,
            onValueChange = { confessor = it },
            label = { Text("Confessor Father (የንስሐ አባት)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onConfirm(name, phone, email, gender, category, classGrade, confessor, address)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
        modifier = Modifier.testTag("confirm_add_member")
      ) {
        Text("Save Member")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun AddAnnouncementDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, String, String, Boolean) -> Unit
) {
  var tEn by remember { mutableStateOf("") }
  var tAm by remember { mutableStateOf("") }
  var bEn by remember { mutableStateOf("") }
  var bAm by remember { mutableStateOf("") }
  var target by remember { mutableStateOf("All Members") }
  var isPinned by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Publish Announcement", fontWeight = FontWeight.Bold) },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
          OutlinedTextField(
            value = tAm,
            onValueChange = { tAm = it },
            label = { Text("Title (Amharic / አማርኛ ርዕስ)") },
            modifier = Modifier.fillMaxWidth().testTag("announcement_title_am")
          )
        }
        item {
          OutlinedTextField(
            value = tEn,
            onValueChange = { tEn = it },
            label = { Text("Title (English)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          OutlinedTextField(
            value = bAm,
            onValueChange = { bAm = it },
            label = { Text("Body (Amharic / ዝርዝር መረጃ)") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
          )
        }
        item {
          OutlinedTextField(
            value = bEn,
            onValueChange = { bEn = it },
            label = { Text("Body (English)") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
          )
        }
        item {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
            Text("Pin to top")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (tAm.isNotBlank() || tEn.isNotBlank()) {
            onConfirm(tEn, tAm, bEn, bAm, target, isPinned)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
        modifier = Modifier.testTag("confirm_post_announcement")
      ) {
        Text("Publish")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun ExportDataDialog(
  members: List<MemberItem>,
  payments: List<PaymentItem>,
  plans: List<PlanItem>,
  departments: List<Department>,
  attendance: List<AttendanceRecord> = emptyList(),
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var selectedExportType by remember { mutableStateOf(0) } // 0: Executive, 1: Payments CSV, 2: Members CSV, 3: Attendance CSV, 4: Attendance Printable/PDF

  val content = remember(selectedExportType, members, payments, plans, departments, attendance) {
    when (selectedExportType) {
      0 -> DataExporter.generateExecutiveSummary(members, payments, plans, departments)
      1 -> DataExporter.generatePaymentsCsv(payments)
      2 -> DataExporter.generateMembersCsv(members)
      3 -> DataExporter.generateAttendanceCsv(attendance)
      else -> DataExporter.generateAttendancePrintableReport(attendance, members, "Meskerem 2019 E.C.")
    }
  }

  val exportTitle = when (selectedExportType) {
    0 -> "Executive Statistical Report (Summary)"
    1 -> "Financial Audit & Payments (CSV)"
    2 -> "Sunday School Members Roster (CSV)"
    3 -> "Monthly Attendance Register (CSV)"
    else -> "Sunday School Attendance Audit & Register (Printable / PDF)"
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.FileDownload, contentDescription = null, tint = ChurchDeepBlue)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Export Statistics & Data", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Select dataset to export for church administration records:",
          style = MaterialTheme.typography.bodySmall,
          color = ChurchTextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Options: 5 Datasets including Monthly Attendance CSV and PDF/Printable
        val options = listOf(
          "Executive Summary",
          "Payments CSV",
          "Members CSV",
          "Attendance CSV",
          "Attendance PDF/Doc"
        )
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          itemsIndexed(options) { index, label ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (selectedExportType == index) ChurchDeepBlue else Color.White,
              border = BorderStroke(1.dp, if (selectedExportType == index) ChurchDeepBlue else Color(0xFFCBD5E1)),
              modifier = Modifier
                .clickable { selectedExportType = index }
                .testTag("export_tab_$index")
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedExportType == index) Color.White else ChurchTextPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Preview (${if (selectedExportType in listOf(1, 2, 3)) "CSV Format" else "Official Report"}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          Text(
            text = "${content.lines().size} lines",
            fontSize = 10.sp,
            color = ChurchTextMuted
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
        ) {
          LazyColumn(modifier = Modifier.padding(8.dp)) {
            item {
              Text(
                text = content,
                fontSize = 10.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = ChurchDarkBlue
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = {
            DataExporter.copyToClipboard(context, exportTitle, content)
          },
          modifier = Modifier.testTag("copy_export_button")
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Copy", fontSize = 12.sp)
        }
        Button(
          onClick = {
            if (selectedExportType in listOf(1, 2, 3)) {
              DataExporter.shareCsv(context, exportTitle, content)
            } else {
              DataExporter.shareText(context, exportTitle, content)
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("share_export_button")
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Export / Share", fontSize = 12.sp)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

@Composable
fun ManagerDataAndSecurityAllDeptsTab(
  departments: List<Department>,
  users: List<User>,
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
  val passedChecks = auditChecks.count { it.passed }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
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
                text = "EXECUTIVE MANAGER • DATA CENTER & FIRESTORE SYNC",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = ChurchLightGold
              )
              Text(
                text = "All ${departments.size} Departments Cloud & Local Data Center",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
              )
              Text(
                text = "$cloudSyncStatus • Last Sync: $lastSyncedAt",
                fontSize = 11.sp,
                color = ChurchPaleGold
              )
            }
            Button(
              onClick = onSyncFirestoreNow,
              colors = ButtonDefaults.buttonColors(containerColor = ChurchGold),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("manager_tab_sync_firestore_btn")
            ) {
              Icon(Icons.Default.Sync, contentDescription = null, tint = ChurchDarkBlue, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Sync Now", color = ChurchDarkBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
                text = "Physical On-Device SQLite Database Location:",
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
              .testTag("manager_open_full_data_center_tab_btn")
          ) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = ChurchDeepBlue, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Open Full Data Center, 17-Table Inspector & JSON/CSV Backup",
              color = ChurchDeepBlue,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    item {
      Text(
        text = "Department-by-Department Data Center, Sync & RBAC Status (${departments.size} Depts)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue
      )
    }

    items(departments) { dept ->
      val deptMembers = members.count { it.departmentId == dept.id }
      val deptPlans = plans.count { it.departmentId == dept.id }
      val deptReports = reports.count { it.departmentId == dept.id }
      val deptAdmin = users.firstOrNull { it.role == "DeptAdmin" && it.departmentId == dept.id }

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ChurchBorder),
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
              color = if (deptAdmin?.isApproved != false) ChurchGreenBg else ChurchAmberBg
            ) {
              Text(
                text = if (deptAdmin?.isApproved != false) "Synced • RBAC Verified" else "Synced • Pending Approval",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (deptAdmin?.isApproved != false) ChurchGreen else ChurchDarkGold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Members: $deptMembers", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChurchDarkBlue)
            Text("Plans: $deptPlans", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChurchDarkBlue)
            Text("Reports: $deptReports", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChurchDarkBlue)
            Text("Attendance: ${attendance.size}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChurchDarkBlue)
            if (dept.code == "FIN") {
              Text("Payments: ${payments.size}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChurchDarkGold)
            }
          }
        }
      }
    }

    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchPaleGold),
        border = BorderStroke(1.dp, ChurchGold),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "8-Point Security Audit ($passedChecks/${auditChecks.size} Passed)",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = ChurchDeepBlue
            )
            OutlinedButton(
              onClick = { auditChecks = SecurityManager.runSecurityAudit() },
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Re-Run Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          auditChecks.forEach { check ->
            Row(
              modifier = Modifier.padding(vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (check.passed) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (check.passed) ChurchGreen else ChurchRed,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${check.id}: ${check.titleEn} (${check.titleAm})",
                fontSize = 11.sp,
                color = ChurchDarkBlue,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }
    }
  }
}

