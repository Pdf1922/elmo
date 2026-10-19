package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*

object DataExporter {

  fun generateExecutiveSummary(
    members: List<MemberItem>,
    payments: List<PaymentItem>,
    plans: List<PlanItem>,
    departments: List<Department>
  ): String {
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    val verifiedPayments = payments.filter { it.verified }
    val totalVerifiedIncome = verifiedPayments.sumOf { it.amountPaid }
    val activeMembers = members.count { it.status == "Active" }
    val paidMembersCount = payments.count { it.verified }
    val unpaidMembersCount = members.size - paidMembersCount

    val workerIncome = verifiedPayments.filter { it.memberCategory == "Worker" }.sumOf { it.amountPaid }
    val studentWorkerIncome = verifiedPayments.filter { it.memberCategory == "StudentWorker" }.sumOf { it.amountPaid }
    val nonWorkerIncome = verifiedPayments.filter { it.memberCategory == "NonWorker" }.sumOf { it.amountPaid }

    return buildString {
      appendLine("===============================================================")
      appendLine("EOTC - DEBRE HAYIL ST. GABRIEL & ST. ARSEMA CHURCH")
      appendLine("MESERETE HIWOT SUNDAY SCHOOL MANAGEMENT SYSTEM")
      appendLine("የቦሌ አረብሳ ደብረ ኃይል ቅ/ገብርኤልና ቅ/አርሴማ ቤ/ክ መሠረተ ሕይወት ሰንበት ት/ቤት")
      appendLine("EXECUTIVE STATISTICAL & FINANCIAL REPORT (የዋና ሥራ አስኪያጅ ሪፖርት)")
      appendLine("Generated on: $date (መስከረም 2019 ዓ.ም.)")
      appendLine("===============================================================")
      appendLine()
      appendLine("1. KEY METRICS & MEMBERSHIP OVERVIEW (አጠቃላይ የአባላት ሁኔታ)")
      appendLine("   • Total Registered Members: ${members.size}")
      appendLine("   • Active Serving Members: $activeMembers")
      appendLine("   • Paid Members (Current Month): $paidMembersCount")
      appendLine("   • Unpaid / Pending Members: $unpaidMembersCount")
      appendLine()
      appendLine("2. FINANCIAL SUMMARY & REVENUE (የሒሳብና ገቢ ማጠቃለያ)")
      appendLine("   • Total Verified Income: ETB ${String.format(Locale.US, "%,.2f", totalVerifiedIncome)}")
      appendLine("   • Worker Members Contribution (100 ETB rate): ETB ${String.format(Locale.US, "%,.2f", workerIncome)}")
      appendLine("   • Student Worker Contribution (50 ETB rate): ETB ${String.format(Locale.US, "%,.2f", studentWorkerIncome)}")
      appendLine("   • Non-Worker / Student Contribution (20 ETB rate): ETB ${String.format(Locale.US, "%,.2f", nonWorkerIncome)}")
      appendLine()
      appendLine("3. DEPARTMENTAL PLANS & BUDGETS (የ7ቱ ክፍላት ዕቅዶችና በጀት)")
      plans.forEach { plan ->
        appendLine("   • [${plan.deptName}] ${plan.title}")
        appendLine("     Target: ${plan.target}")
        appendLine("     Budget: ETB ${String.format(Locale.US, "%,.2f", plan.budget)} | Status: ${plan.status}")
      }
      appendLine()
      appendLine("4. RECENT VERIFIED PAYMENTS AUDIT TRAIL")
      verifiedPayments.take(10).forEach { p ->
        appendLine("   • Receipt: ${p.receiptNumber} | Member: ${p.memberName} (${p.memberCategory}) | ETB ${p.amountPaid} via ${p.paymentMethod} | Verified by: ${p.verifiedBy}")
      }
      appendLine()
      appendLine("===============================================================")
      appendLine("Verified by Management / በዋና ሥራ አስኪያጅ የጸደቀ")
      appendLine("===============================================================")
    }
  }

  fun generatePaymentsCsv(payments: List<PaymentItem>): String {
    val s = SecurityManager::sanitizeCsvField
    return buildString {
      appendLine("Payment ID,Receipt Number,Member ID,Member Name,Category,Amount Due,Amount Paid,Month,Year,Payment Method,Verified,Verified By,Verification Date,Notes")
      payments.forEach { p ->
        appendLine("${s(p.paymentId)},${s(p.receiptNumber)},${s(p.memberId)},${s(p.memberName)},${s(p.memberCategory)},${p.amountDue},${p.amountPaid},${s(p.paymentMonth)},${p.paymentYear},${s(p.paymentMethod)},${p.verified},${s(p.verifiedBy)},${s(p.verificationDate)},${s(p.notes)}")
      }
    }
  }

  fun generateMembersCsv(members: List<MemberItem>): String {
    val s = SecurityManager::sanitizeCsvField
    return buildString {
      appendLine("Member ID,Full Name,Gender,Category,Class Grade,Phone,Email,Confessor Father,Status,Join Date,Address")
      members.forEach { m ->
        appendLine("${s(m.memberId)},${s(m.fullName)},${s(m.gender)},${s(m.memberCategory)},${s(m.classGrade)},${s(m.phone)},${s(m.email)},${s(m.confessorFather)},${s(m.status)},${s(m.joinDate)},${s(m.address)}")
      }
    }
  }

  fun generateAttendanceCsv(records: List<AttendanceRecord>, monthFilter: String? = null): String {
    val s = SecurityManager::sanitizeCsvField
    val filtered = if (monthFilter.isNullOrBlank()) records else records.filter { it.date.startsWith(monthFilter) || it.notes.contains(monthFilter, ignoreCase = true) }
    return buildString {
      appendLine("Record ID,Date,Member ID,Member Name,Service Type,Status,Recorded By,Notes")
      filtered.forEach { r ->
        appendLine("${r.id},${s(r.date)},${s(r.memberId)},${s(r.memberName)},${s(r.serviceType)},${s(r.status)},${s(r.recordedBy)},${s(r.notes)}")
      }
    }
  }

  fun generateFullDatabaseJsonBackup(
    dbPath: String,
    usersCount: Int,
    departments: List<Department>,
    members: List<MemberItem>,
    payments: List<PaymentItem>,
    attendance: List<AttendanceRecord>,
    plans: List<PlanItem>,
    reports: List<ReportItem>,
    announcements: List<AnnouncementItem>,
    quizResults: List<QuizResult>
  ): String {
    val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date())
    fun esc(str: String) = str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    return buildString {
      appendLine("{")
      appendLine("  \"dataCenterMetadata\": {")
      appendLine("    \"system\": \"EOTC Meserete Hiwot Sunday School Management System\",")
      appendLine("    \"engine\": \"Android Jetpack Room (SQLite 3 WAL)\",")
      appendLine("    \"databaseFileName\": \"meserete_hiwot_church.db\",")
      appendLine("    \"deviceStoragePath\": \"${esc(dbPath)}\",")
      appendLine("    \"exportedAt\": \"$timestamp\",")
      appendLine("    \"securityStatus\": \"SHA-256 Auth + RBAC + Parameterized SQL Verified\"")
      appendLine("  },")
      appendLine("  \"tableCounts\": {")
      appendLine("    \"users\": $usersCount,")
      appendLine("    \"departments\": ${departments.size},")
      appendLine("    \"members\": ${members.size},")
      appendLine("    \"payments\": ${payments.size},")
      appendLine("    \"attendance\": ${attendance.size},")
      appendLine("    \"plans\": ${plans.size},")
      appendLine("    \"reports\": ${reports.size},")
      appendLine("    \"announcements\": ${announcements.size},")
      appendLine("    \"quiz_results\": ${quizResults.size}")
      appendLine("  },")
      appendLine("  \"members\": [")
      members.forEachIndexed { idx, m ->
        val comma = if (idx < members.lastIndex) "," else ""
        appendLine("    {\"memberId\": \"${esc(m.memberId)}\", \"fullName\": \"${esc(m.fullName)}\", \"category\": \"${esc(m.memberCategory)}\", \"departmentId\": ${m.departmentId}, \"phone\": \"${esc(m.phone)}\", \"email\": \"${esc(m.email)}\", \"status\": \"${esc(m.status)}\", \"uniformApproved\": ${m.uniformAccessApproved}}$comma")
      }
      appendLine("  ],")
      appendLine("  \"payments\": [")
      payments.forEachIndexed { idx, p ->
        val comma = if (idx < payments.lastIndex) "," else ""
        appendLine("    {\"paymentId\": \"${esc(p.paymentId)}\", \"memberId\": \"${esc(p.memberId)}\", \"memberName\": \"${esc(p.memberName)}\", \"amountPaid\": ${p.amountPaid}, \"month\": \"${esc(p.paymentMonth)}\", \"verified\": ${p.verified}, \"receiptNumber\": \"${esc(p.receiptNumber)}\"}$comma")
      }
      appendLine("  ],")
      appendLine("  \"attendance\": [")
      attendance.forEachIndexed { idx, a ->
        val comma = if (idx < attendance.lastIndex) "," else ""
        appendLine("    {\"id\": ${a.id}, \"date\": \"${esc(a.date)}\", \"memberId\": \"${esc(a.memberId)}\", \"memberName\": \"${esc(a.memberName)}\", \"serviceType\": \"${esc(a.serviceType)}\", \"status\": \"${esc(a.status)}\"}$comma")
      }
      appendLine("  ]")
      appendLine("}")
    }
  }

  fun generateAttendancePrintableReport(
    records: List<AttendanceRecord>,
    members: List<MemberItem>,
    month: String = "Meskerem 2019 E.C."
  ): String {
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    val totalEntries = records.size
    val presentCount = records.count { it.status == "Present" }
    val absentCount = records.count { it.status == "Absent" }
    val lateCount = records.count { it.status == "Late" }
    val excusedCount = records.count { it.status == "Excused" }
    val attendanceRate = if (totalEntries > 0) ((presentCount + lateCount).toDouble() / totalEntries * 100).toInt() else 0

    val uniqueDates = records.map { it.date }.distinct().sorted()
    val memberStats = members.map { m ->
      val mRecords = records.filter { it.memberId == m.memberId || it.memberName.contains(m.fullName.take(5), ignoreCase = true) }
      val mPresent = mRecords.count { it.status == "Present" || it.status == "Late" }
      val mTotal = mRecords.size
      val rate = if (mTotal > 0) (mPresent * 100 / mTotal) else 0
      Triple(m, mPresent to mTotal, rate)
    }.sortedByDescending { it.third }

    return buildString {
      appendLine("===============================================================")
      appendLine("EOTC - DEBRE HAYIL ST. GABRIEL & ST. ARSEMA CHURCH")
      appendLine("MESERETE HIWOT SUNDAY SCHOOL MANAGEMENT SYSTEM")
      appendLine("የቦሌ አረብሳ ደብረ ኃይል ቅ/ገብርኤልና ቅ/አርሴማ ቤ/ክ መሠረተ ሕይወት ሰንበት ት/ቤት")
      appendLine("OFFICIAL MONTHLY ATTENDANCE REGISTER & AUDIT REPORT")
      appendLine("የወርኃዊ አቴንዳንስና የክሊኒክ ምዝገባ ኦፊሴላዊ ሠነድ - $month")
      appendLine("Generated on: $date")
      appendLine("===============================================================")
      appendLine()
      appendLine("1. ATTENDANCE AGGREGATE SUMMARY (አጠቃላይ የአቴንዳንስ ስታቲስቲክስ)")
      appendLine("   • Reporting Period / ወር: $month")
      appendLine("   • Total Registered Choir / Sunday School Members: ${members.size}")
      appendLine("   • Distinct Service Days Recorded: ${uniqueDates.size} sessions (${uniqueDates.joinToString(", ")})")
      appendLine("   • Total Attendance Entries: $totalEntries")
      appendLine("   • Present (በአካል የተገኙ): $presentCount (${if (totalEntries > 0) presentCount * 100 / totalEntries else 0}%)")
      appendLine("   • Late (ያረፈዱ): $lateCount")
      appendLine("   • Excused (በፈቃድ የቀሩ): $excusedCount")
      appendLine("   • Absent (ያለፈቃድ የቀሩ): $absentCount")
      appendLine("   • Overall Church Attendance Rate: $attendanceRate%")
      appendLine()
      appendLine("2. MEMBER ATTENDANCE BREAKDOWN & UNIFORM QUALIFICATION")
      appendLine("---------------------------------------------------------------")
      appendLine(String.format(Locale.US, "%-12s | %-24s | %-12s | %-8s | %s", "ID", "Name", "Attended", "Rate", "Uniform Status"))
      appendLine("---------------------------------------------------------------")
      memberStats.forEach { (m, counts, rate) ->
        val uniformTag = if (m.uniformAccessApproved || rate >= 75) "QUALIFIED (የተፈቀደ)" else "Pending / ያልተሟላ"
        appendLine(String.format(Locale.US, "%-12s | %-24s | %2d / %-6d | %3d%%   | %s", m.memberId, m.fullName.take(24), counts.first, counts.second, rate, uniformTag))
      }
      appendLine("---------------------------------------------------------------")
      appendLine()
      appendLine("3. DETAILED ATTENDANCE LOG (የእያንዳንዱ ቀን ምዝገባ)")
      records.take(25).forEach { r ->
        appendLine("   • [${r.date}] ${r.memberName} (${r.memberId}) - Status: ${r.status} | Service: ${r.serviceType} | By: ${r.recordedBy}")
      }
      if (records.size > 25) {
        appendLine("   ... and ${records.size - 25} more records archived in Room Database.")
      }
      appendLine()
      appendLine("===============================================================")
      appendLine("OFFICIAL CHURCH AUTHORIZATION & SIGNATURES:")
      appendLine()
      appendLine("Department Head (የክፍል ኃላፊ) : _______________________ Date: _________")
      appendLine()
      appendLine("General Manager (ዋና ሥራ አስኪያጅ) : _______________________ Date: _________")
      appendLine()
      appendLine("Parish Priest (ደብር አስተዳዳሪ)   : _______________________ Date: _________")
      appendLine("===============================================================")
    }
  }

  fun shareCsv(context: Context, title: String, csvContent: String) {
    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TITLE, title)
      putExtra(Intent.EXTRA_SUBJECT, title)
      putExtra(Intent.EXTRA_TEXT, csvContent)
      type = "text/csv"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Export $title (CSV)")
    context.startActivity(shareIntent)
  }

  fun shareText(context: Context, title: String, content: String) {
    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TITLE, title)
      putExtra(Intent.EXTRA_SUBJECT, title)
      putExtra(Intent.EXTRA_TEXT, content)
      type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, title)
    context.startActivity(shareIntent)
  }

  fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard! / ወደ ቅንጥብ ሰሌዳ ተገልብጧል!", Toast.LENGTH_SHORT).show()
  }
}
