package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.language.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberPortalScreen(
  viewModel: SundaySchoolViewModel,
  onNavigateToAiCounselor: () -> Unit = {},
  onNavigateToBibleStory: () -> Unit = {}
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val members by viewModel.members.collectAsState()
  val allPayments by viewModel.payments.collectAsState()
  val paymentSettings by viewModel.paymentSettings.collectAsState()
  val allAttendance by viewModel.attendance.collectAsState()
  val allGrades by viewModel.grades.collectAsState()
  val announcements by viewModel.announcements.collectAsState()
  val feedbackList by viewModel.feedbackList.collectAsState()

  // Find linked member record
  val memberRecord = members.find {
    it.email.equals(currentUser?.email, ignoreCase = true) ||
      it.fullName.contains(currentUser?.fullName ?: "", ignoreCase = true)
  } ?: members.firstOrNull() ?: MemberItem(
    memberId = "MH-2015-001",
    fullName = currentUser?.fullName ?: "Ephrem Hailu",
    phone = "+251912345678",
    email = currentUser?.email ?: "ephrem@meseretehiwot.org",
    gender = "Male",
    classGrade = "Grade 3 - Zikre Kidusan",
    memberCategory = "Worker",
    confessorFather = "Kesis Melaku / ቀሲስ መላኩ",
    status = "Active",
    joinDate = "2021-09-12",
    address = "Bole Arabsa Site 3"
  )

  val memberPayments = allPayments.filter {
    it.memberId == memberRecord.memberId || it.memberName.contains(memberRecord.fullName.take(6), ignoreCase = true)
  }
  val memberAttendance = allAttendance.filter {
    it.memberId == memberRecord.memberId || it.memberName.contains(memberRecord.fullName.take(6), ignoreCase = true)
  }
  val memberGrades = allGrades.filter {
    it.studentId == memberRecord.memberId || it.studentName.contains(memberRecord.fullName.take(6), ignoreCase = true)
  }

  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf(
    AppStrings.myPayments,
    AppStrings.myAttendance,
    AppStrings.myGrades,
    AppStrings.navAnnouncements,
    AppStrings.feedback
  )

  var showPayDialog by remember { mutableStateOf(false) }
  var showFeedbackDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      ChurchTopAppBar(
        userName = memberRecord.fullName,
        userRole = AppStrings.roleMember,
        onLogout = { viewModel.logout() },
        title = "Member Portal / የአባል ገጽ",
        subtitle = "Debre Hayil Meserete Hiwot"
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = ChurchDeepBlue,
        contentColor = Color.White
      ) {
        tabs.forEachIndexed { index, title ->
          val icon = when (index) {
            0 -> Icons.Default.AccountBalanceWallet
            1 -> Icons.Default.EventAvailable
            2 -> Icons.Default.School
            3 -> Icons.Default.Campaign
            else -> Icons.Default.Forum
          }
          NavigationBarItem(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            icon = { Icon(icon, contentDescription = title) },
            label = { Text(title, fontSize = 10.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = ChurchDarkBlue,
              selectedTextColor = ChurchLightGold,
              indicatorColor = ChurchGold,
              unselectedIconColor = Color.White.copy(alpha = 0.7f),
              unselectedTextColor = Color.White.copy(alpha = 0.7f)
            )
          )
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(ChurchIvory)
    ) {
      // Member Profile Header Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChurchDeepBlue)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .background(ChurchGold, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Person, contentDescription = null, tint = ChurchDarkBlue, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = memberRecord.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "ID: ${memberRecord.memberId} • ${memberRecord.classGrade}",
                style = MaterialTheme.typography.labelSmall,
                color = ChurchLightGold
              )
            }
            StatusBadge(memberRecord.status)
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = ChurchMediumBlue)
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Category", style = MaterialTheme.typography.labelSmall, color = ChurchLightGold)
              Text(memberRecord.memberCategory, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Column {
              Text("Father Confessor", style = MaterialTheme.typography.labelSmall, color = ChurchLightGold)
              Text(memberRecord.confessorFather, style = MaterialTheme.typography.bodySmall, color = Color.White)
            }
            Column {
              Text("Baptized", style = MaterialTheme.typography.labelSmall, color = ChurchLightGold)
              Text(if (memberRecord.isBaptized) "Yes (ተጠምቋል)" else "No", style = MaterialTheme.typography.bodySmall, color = ChurchGreen)
            }
          }
        }
      }

      // Sliding Marquee / Live Ticker
      ChurchSlidingTicker(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
      )

      // Quick 3D Navigation Banners for Spiritual AI & Daily Bible Stories
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = ChurchDeepBlue,
          modifier = Modifier
            .weight(1f)
            .motion3DTilt(maxTiltAngle = 8f)
            .clickable { onNavigateToAiCounselor() }
            .testTag("member_spiritual_ai_btn")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(shape = CircleShape, color = ChurchGold, modifier = Modifier.size(24.dp)) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = ChurchDarkBlue, modifier = Modifier.size(16.dp))
              }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("መምህር ደብረ ኃይል", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              Text("Spiritual AI Chatbot", fontSize = 9.sp, color = ChurchLightGold)
            }
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = ChurchPaleGold,
          border = androidx.compose.foundation.BorderStroke(1.dp, ChurchGold),
          modifier = Modifier
            .weight(1f)
            .motion3DTilt(maxTiltAngle = 8f)
            .clickable { onNavigateToBibleStory() }
            .testTag("member_bible_stories_btn")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(shape = CircleShape, color = ChurchDeepBlue, modifier = Modifier.size(24.dp)) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = ChurchGold, modifier = Modifier.size(16.dp))
              }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("የመጽሐፍ ቅዱስ ታሪክ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChurchDarkBlue)
              Text("6:30 AM/PM Reminders", fontSize = 9.sp, color = ChurchTextSecondary)
            }
          }
        }
      }

      // Tab Content
      when (selectedTab) {
        0 -> MemberPaymentsTab(
          member = memberRecord,
          payments = memberPayments,
          settings = paymentSettings,
          onMakePayment = { showPayDialog = true }
        )
        1 -> MemberAttendanceTab(memberAttendance)
        2 -> MemberGradesTab(memberGrades)
        3 -> MemberAnnouncementsTab(announcements)
        4 -> MemberFeedbackTab(
          member = memberRecord,
          feedbackList = feedbackList.filter { it.submittedBy == memberRecord.email || it.submitterName.contains(memberRecord.fullName.take(5)) },
          onOpenSubmitDialog = { showFeedbackDialog = true }
        )
      }
    }
  }

  // Payment dialog
  if (showPayDialog) {
    val rate = when (memberRecord.memberCategory) {
      "Worker" -> 100.0
      "StudentWorker" -> 50.0
      else -> 20.0
    }
    var amount by remember { mutableStateOf(rate.toInt().toString()) }
    var method by remember { mutableStateOf("Telebirr") }
    var month by remember { mutableStateOf("Meskerem / መስከረም") }
    var receiptNo by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showPayDialog = false },
      title = { Text(AppStrings.makePayment, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Monthly Due for ${memberRecord.memberCategory}: ETB $rate")
          OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (ETB)") },
            modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
          )
          Text("Payment Method:", style = MaterialTheme.typography.labelMedium)
          Row {
            listOf("Telebirr", "CBE Birr", "Cash").forEach { m ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { method = m }.padding(end = 8.dp)
              ) {
                RadioButton(selected = method == m, onClick = { method = m })
                Text(m, fontSize = 11.sp)
              }
            }
          }
          OutlinedTextField(
            value = receiptNo,
            onValueChange = { receiptNo = it },
            label = { Text("Bank / Telebirr Transaction ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("payment_transaction_id")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amt = amount.toDoubleOrNull() ?: rate
            viewModel.recordPayment(
              memberId = memberRecord.memberId,
              memberName = memberRecord.fullName,
              category = memberRecord.memberCategory,
              amountPaid = amt,
              month = month,
              method = method,
              receiptNo = receiptNo,
              notes = "Submitted by member via Member Portal"
            )
            showPayDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("submit_payment_button")
        ) {
          Text(AppStrings.submit)
        }
      },
      dismissButton = {
        TextButton(onClick = { showPayDialog = false }) { Text(AppStrings.cancel) }
      }
    )
  }

  // Feedback dialog
  if (showFeedbackDialog) {
    var type by remember { mutableStateOf("Suggestion") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showFeedbackDialog = false },
      title = { Text(AppStrings.submitFeedback, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Feedback Type:", style = MaterialTheme.typography.labelMedium)
          Row {
            listOf("Suggestion", "Question", "Complaint").forEach { t ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { type = t }.padding(end = 8.dp)
              ) {
                RadioButton(selected = type == t, onClick = { type = t })
                Text(t, fontSize = 11.sp)
              }
            }
          }
          OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject (ርዕስ)") },
            modifier = Modifier.fillMaxWidth().testTag("feedback_subject_input")
          )
          OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Your Message (መልእክት)") },
            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("feedback_message_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (subject.isNotBlank() && message.isNotBlank()) {
              viewModel.submitFeedback(type, subject, message)
              showFeedbackDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("submit_feedback_button")
        ) {
          Text(AppStrings.submit)
        }
      },
      dismissButton = {
        TextButton(onClick = { showFeedbackDialog = false }) { Text(AppStrings.cancel) }
      }
    )
  }
}

@Composable
fun MemberPaymentsTab(
  member: MemberItem,
  payments: List<PaymentItem>,
  settings: List<PaymentSetting>,
  onMakePayment: () -> Unit
) {
  val expectedRate = settings.find { it.category == member.memberCategory }?.amount ?: 100.0
  val latestPayment = payments.maxByOrNull { it.id }
  val isCurrentPaid = latestPayment?.verified == true

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Current Month Status Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Meskerem 2019 Dues (መስከረም)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ChurchDeepBlue
              )
              Text(
                text = "Rate: ETB ${expectedRate.toInt()} / month (${member.memberCategory})",
                style = MaterialTheme.typography.bodySmall,
                color = ChurchTextMuted
              )
            }
            StatusBadge(if (isCurrentPaid) "Paid" else if (latestPayment != null) "Pending Verification" else "Unpaid")
          }

          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = onMakePayment,
            modifier = Modifier.fillMaxWidth().testTag("make_payment_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Payment, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(AppStrings.makePayment, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Text(
        text = "Payment History & Verification Receipts",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue
      )
    }

    items(payments) { p ->
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
            Text(p.paymentMonth, fontWeight = FontWeight.Bold, color = ChurchTextPrimary)
            StatusBadge(if (p.verified) "Verified" else "Pending Verification")
          }
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Amount: ETB ${p.amountPaid.toInt()} via ${p.paymentMethod}", style = MaterialTheme.typography.bodySmall, color = ChurchDeepBlue, fontWeight = FontWeight.SemiBold)
            Text(p.paymentDate, style = MaterialTheme.typography.labelSmall, color = ChurchTextMuted)
          }
          if (p.receiptNumber.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE0E7FF)) {
              Text(
                text = "Receipt: ${p.receiptNumber} • Verified by ${p.verifiedBy}",
                style = MaterialTheme.typography.labelSmall,
                color = ChurchDeepBlue,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MemberAttendanceTab(attendance: List<AttendanceRecord>) {
  val presentCount = attendance.count { it.status == "Present" }
  val total = attendance.size.coerceAtLeast(1)
  val pct = (presentCount.toFloat() / total * 100).toInt()

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        KpiCard(
          title = "Total Sessions",
          value = "$total",
          subtitle = "Sunday Liturgy & Classes",
          icon = Icons.Default.Event,
          modifier = Modifier.weight(1f)
        )
        KpiCard(
          title = "Attendance Rate",
          value = "$pct%",
          subtitle = "$presentCount Present",
          icon = Icons.Default.CheckCircle,
          iconTint = ChurchGreen,
          iconBg = Color(0xFFDCFCE7),
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Text("Attendance Log", fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
    }

    items(attendance) { att ->
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
            Text(att.serviceType, fontWeight = FontWeight.Bold, color = ChurchTextPrimary)
            Text("Date: ${att.date} • By: ${att.recordedBy}", style = MaterialTheme.typography.labelSmall, color = ChurchTextMuted)
          }
          StatusBadge(att.status)
        }
      }
    }
  }
}

@Composable
fun MemberGradesTab(grades: List<GradeItem>) {
  val avgScore = if (grades.isNotEmpty()) (grades.sumOf { it.score } / grades.size).toInt() else 0

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      KpiCard(
        title = "Average Academic Score",
        value = "$avgScore%",
        subtitle = "Sunday School Spiritual Curriculum",
        icon = Icons.Default.School,
        iconTint = ChurchGold,
        iconBg = ChurchPaleGold,
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      Text("Course Evaluation & Grades", fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
    }

    items(grades) { g ->
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
            Text(g.subject, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = ChurchDeepBlue)
            Surface(shape = RoundedCornerShape(8.dp), color = ChurchPaleGold) {
              Text(
                text = "${g.score.toInt()}% (${g.grade})",
                fontWeight = FontWeight.Bold,
                color = ChurchDarkGold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text("Term: ${g.term} • Teacher: ${g.teacherName}", style = MaterialTheme.typography.labelSmall, color = ChurchTextMuted)
          if (g.comments.isNotEmpty()) {
            Text("\"${g.comments}\"", style = MaterialTheme.typography.bodySmall, color = ChurchTextSecondary, modifier = Modifier.padding(top = 4.dp))
          }
        }
      }
    }
  }
}

@Composable
fun MemberAnnouncementsTab(announcements: List<AnnouncementItem>) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(announcements) { a ->
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
            Text(a.titleAm, fontWeight = FontWeight.Bold, color = ChurchDeepBlue, modifier = Modifier.weight(1f))
            if (a.isPinned) {
              Surface(shape = RoundedCornerShape(4.dp), color = ChurchPaleGold) {
                Text("PINNED", color = ChurchDarkGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
              }
            }
          }
          Text(a.titleEn, style = MaterialTheme.typography.labelSmall, color = ChurchTextSecondary)
          Spacer(modifier = Modifier.height(6.dp))
          Text(a.bodyAm, style = MaterialTheme.typography.bodySmall, color = ChurchTextPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          Text(a.bodyEn, style = MaterialTheme.typography.bodySmall, color = ChurchTextMuted)
          Spacer(modifier = Modifier.height(6.dp))
          Text("Posted by ${a.postedBy} • ${a.datePosted}", style = MaterialTheme.typography.labelSmall, color = ChurchTextSecondary)
        }
      }
    }
  }
}

@Composable
fun MemberFeedbackTab(
  member: MemberItem,
  feedbackList: List<FeedbackItem>,
  onOpenSubmitDialog: () -> Unit
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
        Text("My Submissions & Management Replies", fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
        Button(
          onClick = onOpenSubmitDialog,
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("open_feedback_dialog_button")
        ) {
          Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Submit")
        }
      }
    }

    if (feedbackList.isEmpty()) {
      item {
        Text(
          "You have not submitted any questions or feedback yet. Tap 'Submit' to message Sunday School leadership.",
          style = MaterialTheme.typography.bodySmall,
          color = ChurchTextMuted
        )
      }
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
            Text(fb.subject, fontWeight = FontWeight.Bold, color = ChurchTextPrimary, modifier = Modifier.weight(1f))
            StatusBadge(fb.status)
          }
          Text(fb.message, style = MaterialTheme.typography.bodySmall, color = ChurchTextSecondary, modifier = Modifier.padding(vertical = 4.dp))
          Text("Submitted on ${fb.date}", style = MaterialTheme.typography.labelSmall, color = ChurchTextMuted)

          if (fb.response.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = ChurchPaleGold, modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text("Leadership Reply (${fb.handledBy}):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ChurchDarkGold)
                Text(fb.response, fontSize = 12.sp, color = ChurchTextPrimary)
              }
            }
          }
        }
      }
    }
  }
}
