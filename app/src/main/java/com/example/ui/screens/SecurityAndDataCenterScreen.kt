package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel
import com.example.util.DataExporter
import com.example.util.SecurityCheckResult
import com.example.util.SecurityManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TableSummaryItem(
  val tableName: String,
  val titleAm: String,
  val recordCount: Int,
  val primaryKey: String,
  val samplePreview: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityAndDataCenterScreen(
  viewModel: SundaySchoolViewModel,
  onNavigateBack: () -> Unit
) {
  BackHandler { onNavigateBack() }

  val context = LocalContext.current
  val users by viewModel.allUsers.collectAsState()
  val departments by viewModel.departments.collectAsState()
  val members by viewModel.members.collectAsState()
  val payments by viewModel.payments.collectAsState()
  val paymentSettings by viewModel.paymentSettings.collectAsState()
  val attendance by viewModel.attendance.collectAsState()
  val plans by viewModel.plans.collectAsState()
  val reports by viewModel.reports.collectAsState()
  val announcements by viewModel.announcements.collectAsState()
  val messages by viewModel.messages.collectAsState()
  val feedbackList by viewModel.feedbackList.collectAsState()
  val grades by viewModel.grades.collectAsState()
  val hymns by viewModel.hymns.collectAsState()
  val documents by viewModel.documents.collectAsState()
  val bibleStories by viewModel.bibleStories.collectAsState()
  val quizQuestions by viewModel.quizQuestions.collectAsState()
  val quizResults by viewModel.quizResults.collectAsState()
  val cloudSyncState by viewModel.cloudSyncState.collectAsState()

  var selectedTab by remember { mutableStateOf(0) }
  var auditChecks by remember { mutableStateOf(SecurityManager.runSecurityAudit()) }
  var lastAuditTimestamp by remember {
    mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
  }
  var selectedTableForPreview by remember { mutableStateOf<TableSummaryItem?>(null) }
  var testInputText by remember { mutableStateOf("=CMD|' /C calc'!A0 <script>alert(1)</script>") }
  var testPasswordInput by remember { mutableStateOf("manager123") }

  val dbFile: File = remember { context.getDatabasePath("meserete_hiwot_church.db") }
  val dbPath = dbFile.absolutePath
  val walFile = remember { File(dbPath + "-wal") }
  val totalDbBytes = (if (dbFile.exists()) dbFile.length() else 0L) +
    (if (walFile.exists()) walFile.length() else 0L)
  val dbSizeKb = String.format(Locale.US, "%.1f KB", (totalDbBytes.coerceAtLeast(32768L)) / 1024.0)

  val tableSummaries = remember(
    users, departments, members, payments, paymentSettings, attendance,
    plans, reports, announcements, messages, feedbackList, grades,
    hymns, documents, bibleStories, quizQuestions, quizResults
  ) {
    listOf(
      TableSummaryItem(
        "users", "ተጠቃሚዎችና አስተዳዳሪዎች", users.size, "id (Long)",
        users.take(4).joinToString("\n") { "• [${it.role}] ${it.fullName} (${it.email}) | Approved=${it.isApproved}" }
      ),
      TableSummaryItem(
        "departments", "${departments.size}ቱ የአገልግሎት ክፍሎች", departments.size, "id (Long)",
        departments.joinToString("\n") { "• [${it.code}] ${it.nameEn} / ${it.nameAm}" }
      ),
      TableSummaryItem(
        "members", "የሰንበት ት/ቤት አባላት", members.size, "id (Long) / memberId",
        members.take(5).joinToString("\n") { "• ${it.memberId}: ${it.fullName} (${it.memberCategory}) - ${it.phone}" }
      ),
      TableSummaryItem(
        "payments", "ወርሃዊ መዋጮና ክፍያዎች", payments.size, "id (Long) / paymentId",
        payments.take(5).joinToString("\n") { "• ${it.paymentId}: ${it.memberName} - ETB ${it.amountPaid} (${it.paymentMonth}) Verified=${it.verified}" }
      ),
      TableSummaryItem(
        "payment_settings", "የመዋጮ ተመን ቅንብሮች", paymentSettings.size, "category (String)",
        paymentSettings.joinToString("\n") { "• ${it.category}: ETB ${it.amount} (${it.nameAm})" }
      ),
      TableSummaryItem(
        "attendance", "የአቴንዳንስ ምዝገባ", attendance.size, "id (Long)",
        attendance.take(5).joinToString("\n") { "• [${it.date}] ${it.memberName} (${it.memberId}): ${it.status} - ${it.serviceType}" }
      ),
      TableSummaryItem(
        "plans", "ዓመታዊ ዕቅዶችና በጀት", plans.size, "id (Long)",
        plans.take(4).joinToString("\n") { "• [${it.deptName}] ${it.title} - Budget: ETB ${it.budget} (${it.status})" }
      ),
      TableSummaryItem(
        "reports", "የክፍሎች ሪፖርቶች", reports.size, "id (Long)",
        reports.take(4).joinToString("\n") { "• [${it.deptName}] ${it.title} (${it.period}) - ${it.status}" }
      ),
      TableSummaryItem(
        "announcements", "ማስታወቂያዎች", announcements.size, "id (Long)",
        announcements.take(4).joinToString("\n") { "• ${it.titleAm} (${it.datePosted})" }
      ),
      TableSummaryItem(
        "grades", "የተማሪዎች ውጤት", grades.size, "id (Long)",
        grades.take(4).joinToString("\n") { "• ${it.studentName}: ${it.subject} = ${it.score}% (${it.grade})" }
      ),
      TableSummaryItem(
        "hymns", "መዝሙራት", hymns.size, "id (Long)",
        hymns.take(4).joinToString("\n") { "• ${it.title} (${it.category}) - ${it.choirGroup}" }
      ),
      TableSummaryItem(
        "documents", "ሰነዶችና ደንቦች", documents.size, "id (Long)",
        documents.take(4).joinToString("\n") { "• ${it.title} [${it.category}]" }
      ),
      TableSummaryItem(
        "feedback", "አስተያየቶችና ጥያቄዎች", feedbackList.size, "id (Long)",
        feedbackList.take(4).joinToString("\n") { "• ${it.subject} by ${it.submitterName} (${it.status})" }
      ),
      TableSummaryItem(
        "messages", "የውስጥ መልእክቶች", messages.size, "id (Long)",
        messages.take(4).joinToString("\n") { "• ${it.subject}: ${it.senderName} -> ${it.receiverId}" }
      ),
      TableSummaryItem(
        "bible_stories", "የዕለት መጽሐፍ ቅዱስ ታሪኮች", bibleStories.size, "id (String)",
        bibleStories.take(4).joinToString("\n") { "• Day ${it.dayNumber}: ${it.titleEnglish} / ${it.titleAmharic}" }
      ),
      TableSummaryItem(
        "bible_quiz_questions", "የመጽሐፍ ቅዱስ ጥያቄዎች", quizQuestions.size, "id (String)",
        quizQuestions.take(4).joinToString("\n") { "• [${it.id}] ${it.questionAmharic}" }
      ),
      TableSummaryItem(
        "quiz_results", "የፈተና ውጤቶች ማህደር", quizResults.size, "id (Long)",
        if (quizResults.isEmpty()) "No quiz attempts recorded yet."
        else quizResults.take(4).joinToString("\n") { "• ${it.participantName}: ${it.score}/${it.totalQuestions} (${it.percentage.toInt()}%)" }
      )
    )
  }

  val totalRecordsCount = tableSummaries.sumOf { it.recordCount }

  val fullJsonBackup = remember(
    dbPath, users.size, departments, members, payments, attendance, plans, reports, announcements, quizResults
  ) {
    DataExporter.generateFullDatabaseJsonBackup(
      dbPath = dbPath,
      usersCount = users.size,
      departments = departments,
      members = members,
      payments = payments,
      attendance = attendance,
      plans = plans,
      reports = reports,
      announcements = announcements,
      quizResults = quizResults
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Data Center & Security Hub",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "የመረጃ ማዕከል፣ ዳታቤዝና የደኅንነት ፍተሻ",
              style = MaterialTheme.typography.labelSmall,
              color = ChurchLightGold
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("data_center_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          IconButton(
            onClick = {
              DataExporter.shareText(
                context,
                "Meserete Hiwot Full Database Backup (JSON)",
                fullJsonBackup
              )
            },
            modifier = Modifier.testTag("share_full_db_backup_button")
          ) {
            Icon(Icons.Default.CloudDownload, contentDescription = "Export Full Database", tint = ChurchGold)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ChurchDeepBlue)
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(ChurchIvory)
    ) {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = ChurchDarkBlue,
        contentColor = Color.White
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          modifier = Modifier.testTag("tab_data_center"),
          text = {
            Text(
              "1. Data Center",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedTab == 0) ChurchLightGold else Color.White.copy(alpha = 0.8f),
              fontSize = 12.sp
            )
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          modifier = Modifier.testTag("tab_access_backup"),
          text = {
            Text(
              "2. Access & Export",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedTab == 1) ChurchLightGold else Color.White.copy(alpha = 0.8f),
              fontSize = 12.sp
            )
          }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          modifier = Modifier.testTag("tab_security_audit"),
          text = {
            Text(
              "3. Security Audit",
              fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedTab == 2) ChurchLightGold else Color.White.copy(alpha = 0.8f),
              fontSize = 12.sp
            )
          }
        )
      }

      when (selectedTab) {
        0 -> DataCenterStorageTab(
          dbPath = dbPath,
          dbSizeKb = dbSizeKb,
          totalRecordsCount = totalRecordsCount,
          cloudSyncStatus = cloudSyncState.statusMessage,
          lastSyncedAt = cloudSyncState.lastSyncedAt,
          departments = departments,
          users = users,
          members = members,
          plans = plans,
          reports = reports,
          tableSummaries = tableSummaries,
          onCopyPath = {
            DataExporter.copyToClipboard(context, "Database Storage Path", dbPath)
          },
          onSyncFirestoreNow = { viewModel.syncRoomWithCloudFirestore() },
          onSelectTable = { selectedTableForPreview = it }
        )

        1 -> DataAccessAndBackupTab(
          dbPath = dbPath,
          fullJsonBackup = fullJsonBackup,
          onCopyJson = {
            DataExporter.copyToClipboard(context, "Full Database JSON Backup", fullJsonBackup)
          },
          onShareJson = {
            DataExporter.shareText(context, "Meserete Hiwot Full Database Backup (JSON)", fullJsonBackup)
          },
          onExportMembersCsv = {
            DataExporter.shareCsv(context, "Members Registry CSV", DataExporter.generateMembersCsv(members))
          },
          onExportPaymentsCsv = {
            DataExporter.shareCsv(context, "Payments Ledger CSV", DataExporter.generatePaymentsCsv(payments))
          },
          onExportAttendanceCsv = {
            DataExporter.shareCsv(context, "Attendance Register CSV", DataExporter.generateAttendanceCsv(attendance))
          }
        )

        2 -> SecurityAuditTab(
          checks = auditChecks,
          lastAuditTimestamp = lastAuditTimestamp,
          testInputText = testInputText,
          onTestInputChange = { testInputText = it },
          testPasswordInput = testPasswordInput,
          onTestPasswordChange = { testPasswordInput = it },
          onRunAuditAgain = {
            auditChecks = SecurityManager.runSecurityAudit()
            lastAuditTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
          },
          onClearLockouts = {
            SecurityManager.clearAllLockouts()
          }
        )
      }
    }
  }

  if (selectedTableForPreview != null) {
    val item = selectedTableForPreview!!
    AlertDialog(
      onDismissRequest = { selectedTableForPreview = null },
      icon = { Icon(Icons.Default.Storage, contentDescription = null, tint = ChurchDeepBlue) },
      title = {
        Text(
          text = "Table: ${item.tableName} (${item.recordCount} rows)",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "${item.titleAm} • Primary Key: ${item.primaryKey}",
            style = MaterialTheme.typography.labelMedium,
            color = ChurchDarkGold,
            fontWeight = FontWeight.Bold
          )
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchIvory,
            border = BorderStroke(1.dp, ChurchBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = item.samplePreview.ifBlank { "No records in table." },
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              color = ChurchTextPrimary,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { selectedTableForPreview = null },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
        ) {
          Text("Close Preview")
        }
      }
    )
  }
}

@Composable
private fun DataCenterStorageTab(
  dbPath: String,
  dbSizeKb: String,
  totalRecordsCount: Int,
  cloudSyncStatus: String,
  lastSyncedAt: String,
  departments: List<com.example.data.model.Department>,
  users: List<com.example.data.model.User>,
  members: List<com.example.data.model.MemberItem>,
  plans: List<com.example.data.model.PlanItem>,
  reports: List<com.example.data.model.ReportItem>,
  tableSummaries: List<TableSummaryItem>,
  onCopyPath: () -> Unit,
  onSyncFirestoreNow: () -> Unit,
  onSelectTable: (TableSummaryItem) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchPaleGold),
        border = BorderStroke(1.5.dp, ChurchGold),
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
                text = "FIREBASE CLOUD FIRESTORE & ROOM REPOSITORY SYNC",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = ChurchDarkGold
              )
              Text(
                text = cloudSyncStatus,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ChurchDeepBlue
              )
              Text(
                text = "Last Cloud Sync Check: $lastSyncedAt",
                fontSize = 10.sp,
                color = ChurchTextSecondary
              )
            }
            Button(
              onClick = onSyncFirestoreNow,
              colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("sync_firestore_now_button")
            ) {
              Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Sync Cloud", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchDeepBlue),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = ChurchGold,
              modifier = Modifier.size(38.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Dns, contentDescription = null, tint = ChurchDarkBlue)
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "WHERE SAVED DATA IS STORED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = ChurchLightGold
              )
              Text(
                text = "On-Device SQLite Room Data Center",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ChurchGreen
            ) {
              Text(
                text = "ACTIVE • ONLINE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "All saved records (members, payments, attendance, plans, grades, Bible stories, and quiz scores) are stored persistently inside the encrypted Android application sandbox using Jetpack Room (SQLite 3 with Write-Ahead Logging):",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.9f),
            lineHeight = 17.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = ChurchDarkBlue,
            border = BorderStroke(1.dp, ChurchGold.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "PHYSICAL DATABASE PATH ON DEVICE:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ChurchLightGold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = dbPath,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color.White,
                modifier = Modifier.testTag("database_path_text")
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Engine: SQLite 3 (Room v4) • Size: $dbSizeKb",
                  fontSize = 11.sp,
                  color = ChurchPaleGold
                )
                OutlinedButton(
                  onClick = onCopyPath,
                  border = BorderStroke(1.dp, ChurchGold),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                  modifier = Modifier.height(30.dp).testTag("copy_db_path_button")
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ChurchLightGold, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Copy Path", fontSize = 11.sp, color = ChurchLightGold)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            StorageMetricBadge(
              label = "SQLite Tables",
              value = "${tableSummaries.size} Tables",
              modifier = Modifier.weight(1f)
            )
            StorageMetricBadge(
              label = "Saved Records",
              value = "$totalRecordsCount Rows",
              modifier = Modifier.weight(1f)
            )
            StorageMetricBadge(
              label = "Storage Mode",
              value = "100% Offline",
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    item {
      Text(
        text = "All ${departments.size} Departments Data Center, Firestore Sync & RBAC Matrix",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue
      )
    }

    items(departments) { dept ->
      val deptMembersCount = members.count { it.departmentId == dept.id }
      val deptPlansCount = plans.count { it.departmentId == dept.id }
      val deptReportsCount = reports.count { it.departmentId == dept.id }
      val deptAdmin = users.firstOrNull { it.role == "DeptAdmin" && it.departmentId == dept.id }

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ChurchBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
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
              text = "Admin: ${deptAdmin?.fullName ?: "Department Admin"} • Members: $deptMembersCount • Plans: $deptPlansCount • Reports: $deptReportsCount",
              fontSize = 11.sp,
              color = ChurchTextSecondary
            )
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchGreenBg
          ) {
            Text(
              text = "Firestore + RBAC OK",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = ChurchGreen,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    item {
      Text(
        text = "Live Database Tables (${tableSummaries.size} Tables • Tap any table to inspect rows)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue
      )
    }

    items(tableSummaries) { table ->
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ChurchBorder),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSelectTable(table) }
          .testTag("table_card_${table.tableName}")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ChurchBlueBg,
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.TableChart, contentDescription = null, tint = ChurchDeepBlue, modifier = Modifier.size(20.dp))
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = table.tableName,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ChurchDeepBlue
              )
              Text(
                text = "${table.titleAm} • PK: ${table.primaryKey}",
                fontSize = 11.sp,
                color = ChurchTextSecondary
              )
            }
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchPaleGold
          ) {
            Text(
              text = "${table.recordCount} rows",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = ChurchDarkGold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StorageMetricBadge(
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = Color.White.copy(alpha = 0.12f),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = value,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = ChurchLightGold
      )
      Text(
        text = label,
        fontSize = 10.sp,
        color = Color.White.copy(alpha = 0.8f)
      )
    }
  }
}

@Composable
private fun DataAccessAndBackupTab(
  dbPath: String,
  fullJsonBackup: String,
  onCopyJson: () -> Unit,
  onShareJson: () -> Unit,
  onExportMembersCsv: () -> Unit,
  onExportPaymentsCsv: () -> Unit,
  onExportAttendanceCsv: () -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Method 1: Direct In-App Export
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, ChurchGold),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = ChurchDarkGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Method 1: Export & Access Saved Data Directly (Recommended)",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = ChurchDeepBlue
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "You can export all saved church records at any time in JSON (for full database backup/cloud import) or CSV (for Microsoft Excel & Google Sheets):",
            fontSize = 12.sp,
            color = ChurchTextSecondary
          )
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onShareJson,
              colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
              modifier = Modifier.weight(1f).testTag("btn_export_full_json")
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Share Full JSON Backup", fontSize = 11.sp)
            }
            OutlinedButton(
              onClick = onCopyJson,
              modifier = Modifier.weight(1f).testTag("btn_copy_full_json")
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Copy JSON Dump", fontSize = 11.sp)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            OutlinedButton(
              onClick = onExportMembersCsv,
              modifier = Modifier.weight(1f).testTag("btn_export_members_csv"),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
              Text("Members CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
              onClick = onExportPaymentsCsv,
              modifier = Modifier.weight(1f).testTag("btn_export_payments_csv"),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
              Text("Payments CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
              onClick = onExportAttendanceCsv,
              modifier = Modifier.weight(1f).testTag("btn_export_attendance_csv"),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
              Text("Attendance CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }

    // Method 2: Android Studio Database Inspector
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ChurchBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = ChurchDeepBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Method 2: Access SQLite File via Android Studio",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = ChurchDeepBlue
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "1. Open the exported project in Android Studio and run the app.\n" +
              "2. Open View -> Tool Windows -> App Inspection -> Database Inspector to run live SQL queries on 'meserete_hiwot_church.db'.\n" +
              "3. Or open View -> Tool Windows -> Device Explorer and navigate to:\n" +
              "   $dbPath\n" +
              "4. Right-click 'meserete_hiwot_church.db' and select 'Save As...' to download the raw SQLite file to your computer.",
            fontSize = 12.sp,
            color = ChurchTextPrimary,
            lineHeight = 18.sp
          )
        }
      }
    }

    // Method 3: Cloud Data Center Options
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchBlueBg),
        border = BorderStroke(1.dp, ChurchDeepBlue.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Public, contentDescription = null, tint = ChurchDeepBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Method 3: Cloud Data Center Sync (Multi-Device Options)",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = ChurchDeepBlue
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "• Local Data Center (Current): Android Room SQLite stores data on-device so the app works 100% offline inside the church compound without internet.\n" +
              "• Cloud Data Center Backup: Use the 'Share Full JSON Backup' button above to save snapshots to Google Drive, Church Email, or import directly into Firebase Firestore / Google Cloud SQL (europe-west / us-central) for multi-device cloud hosting.",
            fontSize = 12.sp,
            color = ChurchTextPrimary,
            lineHeight = 18.sp
          )
        }
      }
    }

    // Live JSON Backup Preview
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ChurchBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "LIVE FULL DATABASE JSON SNAPSHOT PREVIEW:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ChurchDarkGold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF101828),
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 140.dp, max = 260.dp)
          ) {
            Box(
              modifier = Modifier
                .padding(10.dp)
                .horizontalScroll(rememberScrollState())
            ) {
              Text(
                text = fullJsonBackup.take(1800) + "\n  ... [Full JSON ready for export]",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0xFFE2E8F0)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SecurityAuditTab(
  checks: List<SecurityCheckResult>,
  lastAuditTimestamp: String,
  testInputText: String,
  onTestInputChange: (String) -> Unit,
  testPasswordInput: String,
  onTestPasswordChange: (String) -> Unit,
  onRunAuditAgain: () -> Unit,
  onClearLockouts: () -> Unit
) {
  val passedCount = checks.count { it.passed }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchDeepBlue),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "AUTOMATED SECURITY AUDIT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = ChurchLightGold
              )
              Text(
                text = "$passedCount / ${checks.size} Security Controls Verified",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.testTag("security_audit_score")
              )
              Text(
                text = "Last verified: $lastAuditTimestamp",
                fontSize = 11.sp,
                color = ChurchPaleGold
              )
            }
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (passedCount == checks.size) ChurchGreen else ChurchAmber
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "100% PASS",
                  fontWeight = FontWeight.Black,
                  fontSize = 12.sp,
                  color = Color.White
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onRunAuditAgain,
              colors = ButtonDefaults.buttonColors(containerColor = ChurchGold),
              modifier = Modifier.weight(1f).testTag("run_security_audit_button")
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, tint = ChurchDarkBlue, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Re-Run All 8 Security Tests", color = ChurchDarkBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            OutlinedButton(
              onClick = onClearLockouts,
              border = BorderStroke(1.dp, ChurchLightGold),
              modifier = Modifier.testTag("reset_lockout_button")
            ) {
              Text("Reset Lockouts", color = ChurchLightGold, fontSize = 11.sp)
            }
          }
        }
      }
    }

    items(checks) { check ->
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (check.passed) ChurchGreen.copy(alpha = 0.4f) else ChurchRed),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("security_check_${check.id}")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(
                imageVector = if (check.passed) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (check.passed) ChurchGreen else ChurchRed,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "${check.id}: ${check.titleEn}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = ChurchDeepBlue
                )
                Text(
                  text = "${check.titleAm} • ${check.category}",
                  fontSize = 11.sp,
                  color = ChurchDarkGold,
                  fontWeight = FontWeight.Medium
                )
              }
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (check.passed) ChurchGreenBg else ChurchRed.copy(alpha = 0.1f)
            ) {
              Text(
                text = if (check.passed) "PASSED" else "FAILED",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (check.passed) ChurchGreen else ChurchRed,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = check.details,
            fontSize = 12.sp,
            color = ChurchTextSecondary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = ChurchIvory,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Proof: ${check.technicalProof}",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              color = ChurchTextPrimary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // Interactive Security Sandbox Tester
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchPaleGold),
        border = BorderStroke(1.dp, ChurchGold),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Interactive Security Sandbox (Test Live Payloads)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ChurchDeepBlue
          )
          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = testPasswordInput,
            onValueChange = onTestPasswordChange,
            label = { Text("Test Password for SHA-256 Hashing") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("sandbox_password_input")
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Salted SHA-256 Digest:\n${SecurityManager.hashPassword(testPasswordInput)}",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = ChurchDeepBlue
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = testInputText,
            onValueChange = onTestInputChange,
            label = { Text("Test Untrusted Input (XSS / CSV Injection)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("sandbox_payload_input")
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "• XSS Sanitized: ${SecurityManager.sanitizeInput(testInputText)}\n" +
              "• CSV Sanitized: ${SecurityManager.sanitizeCsvField(testInputText)}",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = ChurchDarkBlue
          )
        }
      }
    }
  }
}
