package com.example.ui.language

object AppStrings {
  fun t(en: String, am: String): String {
    return if (LanguageState.currentLanguage == AppLanguage.AMHARIC) am else en
  }

  // App & Church Info
  val appName get() = t("Meserete Hiwot", "መሠረተ ሕይወት")
  val fullAppName get() = t("Meserete Hiwot Sunday School", "መሠረተ ሕይወት ሰንበት ትምህርት ቤት")
  val churchName get() = t(
    "Bole Arabsa Debre Hayil St. Gabriel & St. Arsema Church",
    "ቦሌ አረብሳ ደብረ ኃይል ቅዱስ ገብርኤል እና ቅድስት አርሴማ ቤተክርስቲያን"
  )
  val systemTitle get() = t("Sunday School Management System", "የሰንበት ት/ቤት አስተዳደር ሥርዓት")

  // Common UI
  val login get() = t("Login", "ግባ")
  val register get() = t("Register", "ተመዝገብ")
  val logout get() = t("Log Out", "ውጣ")
  val email get() = t("Email Address", "ኢሜይል አድራሻ")
  val password get() = t("Password", "የይለፍ ቃል")
  val fullName get() = t("Full Name", "ሙሉ ስም")
  val phone get() = t("Phone Number", "ስልክ ቁጥር")
  val address get() = t("Address", "አድራሻ")
  val search get() = t("Search...", "ፈልግ...")
  val filter get() = t("Filter", "አጣራ")
  val all get() = t("All", "ሁሉም")
  val submit get() = t("Submit", "አስገባ")
  val cancel get() = t("Cancel", "ይቅር")
  val ok get() = t("OK", "እሺ")
  val save get() = t("Save Changes", "ለውጦችን መዝግብ")
  val edit get() = t("Edit", "አስተካክል")
  val delete get() = t("Delete", "አጥፋ")
  val status get() = t("Status", "ሁኔታ")
  val date get() = t("Date", "ቀን")
  val actions get() = t("Actions", "ተግባራት")
  val details get() = t("Details", "ዝርዝር")
  val close get() = t("Close", "ዝጋ")
  val notes get() = t("Notes", "ማስታወሻ")
  val approved get() = t("Approved", "የጸደቀ")
  val pending get() = t("Pending", "በመጠባበቅ ላይ")
  val verified get() = t("Verified", "የተረጋገጠ")
  val unverified get() = t("Pending Verification", "ያልተረጋገጠ")

  // Roles
  val roleManager get() = t("Manager", "ዋና ሥራ አስኪያጅ")
  val roleDeptAdmin get() = t("Department Admin", "የክፍል ኃላፊ")
  val roleMember get() = t("Sunday School Member", "የሰንበት ት/ቤት አባል")

  // Departments
  val deptOffice get() = t("Office & Coordination", "ጽሕፈት ቤትና አስተባባሪ")
  val deptPlan get() = t("Plan & Development", "ዕቅድና ልማት")
  val deptEducation get() = t("Education & Training", "ትምህርትና ሥልጠና")
  val deptProgram get() = t("Program & Assemblies", "መርሐ ግብርና ጉባኤያት")
  val deptHR get() = t("Human Resources", "የሰው ኃይል አስተዳደር")
  val deptInfo get() = t("Information & PR", "መረጃና የውስጥ ግንኙነት")
  val deptSong get() = t("Song & Arts", "መዝሙርና ሥነ ጥበብ")
  val deptFinance get() = t("Finance & Property", "ሒሳብና ንብረት አስተዳደር")

  // Navigation & Tabs
  val navOverview get() = t("Overview", "አጠቃላይ እይታ")
  val navMembers get() = t("Members", "አባላት")
  val navFinance get() = t("Finance", "ሒሳብ")
  val navAttendance get() = t("Attendance", "የአገልግሎት ክትትል")
  val navEducation get() = t("Education", "ትምህርት")
  val navPlans get() = t("Plans & Reports", "ዕቅድና ሪፖርት")
  val navSong get() = t("Hymns & Arts", "ዝማሬና ኪነጥበብ")
  val navAnnouncements get() = t("Announcements", "ማስታወቂያዎች")
  val navMessages get() = t("Messages", "መልእክቶች")
  val navFeedback get() = t("Feedback", "አስተያየትና ጥቆማ")
  val navProfile get() = t("My Profile", "የግል ገጽ")
  val navSettings get() = t("Settings", "ቅንብሮች")
  val myPayments get() = t("My Payments", "የእኔ ክፍያዎች")
  val myAttendance get() = t("My Attendance", "የአገልግሎት ክትትሌ")
  val myGrades get() = t("My Grades", "ውጤቶቼ")
  val feedback get() = t("Feedback & Q&A", "አስተያየትና ጥያቄ")
  val makePayment get() = t("Record Payment", "ክፍያ ፈጽም / አስገባ")
  val submitFeedback get() = t("Submit Feedback", "አስተያየት / ጥያቄ ላክ")

  // Finance Terms
  val workerMember get() = t("Worker Member", "የሠራተኛ አባል")
  val studentWorker get() = t("Student Worker", "ተማሪና ሠራተኛ")
  val nonWorker get() = t("Non-Worker / Student", "የማይሠራ / ተማሪ")
  val monthlyFee get() = t("Monthly Fee", "ወርሃዊ መዋጮ")
  val amountDue get() = t("Amount Due", "የሚጠበቅ ክፍያ")
  val amountPaid get() = t("Amount Paid", "የተከፈለ መጠን")
  val paymentMethod get() = t("Payment Method", "የክፍያ ዘዴ")
  val receiptNumber get() = t("Receipt No.", "ደረሰኝ ቁጥር")
  val totalIncome get() = t("Total Verified Income", "አጠቃላይ የተረጋገጠ ገቢ")
  val paidMembers get() = t("Paid Members", "የከፈሉ አባላት")
  val unpaidMembers get() = t("Unpaid Members", "ያልከፈሉ አባላት")
  val pendingVerification get() = t("Pending Verification", "ማረጋገጫ የሚጠብቁ")
  val markAsPaid get() = t("Mark as Paid", "እንደተከፈለ መዝግብ")
  val verifyPayment get() = t("Verify Payment", "ክፍያውን አረጋግጥ")
  val paymentHistory get() = t("Payment History", "የክፍያ ታሪክ")
  val paymentCategory get() = t("Payment Category", "የክፍያ ምድብ")
  val budgetVsActual get() = t("Budget vs Actual", "በጀትና ትክክለኛ ወጪ")
  val monthlyIncomeChart get() = t("Monthly Income (ETB)", "ወርሃዊ ገቢ (በብር)")
  val incomeByCategory get() = t("Income by Category", "ገቢ በምድብ")
  val paidVsUnpaid get() = t("Paid vs Unpaid Members", "የከፈሉ እና ያልከፈሉ")

  // Member Fields
  val memberId get() = t("Member ID", "የአባል መለያ")
  val gender get() = t("Gender", "ፆታ")
  val male get() = t("Male", "ወንድ")
  val female get() = t("Female", "ሴት")
  val age get() = t("Age", "ዕድሜ")
  val confessorFather get() = t("Confessor Father", "የንስሐ አባት")
  val isBaptized get() = t("Baptized in EOTC", "በኦርቶዶክስ ተጠምቋል")
  val memberType get() = t("Member Type", "የአባልነት ዓይነት")
  val regular get() = t("Regular", "መደበኛ")
  val special get() = t("Special", "ልዩ")
  val honorary get() = t("Honorary", "ክብር")
  val active get() = t("Active", "ንቁ")
  val inactive get() = t("Inactive", "ቦዘኔ")
  val distanced get() = t("Distanced", "የራቀ")
  val joinDate get() = t("Join Date", "የተቀላቀለበት ቀን")
  val classGrade get() = t("Class / Grade", "ክፍል / ደረጃ")

  // Attendance
  val present get() = t("Present", "ተገኝቷል")
  val absent get() = t("Absent", "ቀሪ")
  val excused get() = t("Excused", "ፈቃድ")
  val recordAttendance get() = t("Record Attendance", "ክትትል መዝግብ")

  // Education
  val subject get() = t("Spiritual Subject", "የመንፈሳዊ ትምህርት ዓይነት")
  val score get() = t("Score", "ውጤት")
  val grade get() = t("Grade", "ደረጃ")
  val teacher get() = t("Teacher", "አስተማሪ")
  val term get() = t("Term / Semester", "ሴሚስተር / ሩብ ዓመት")

  // Plans & Reports
  val planTitle get() = t("Plan Title", "የዕቅድ ርዕስ")
  val annualPlan get() = t("Annual Plan", "ዓመታዊ ዕቅድ")
  val budget get() = t("Budget (ETB)", "በጀት (በብር)")
  val target get() = t("Target", "ግብ")
  val progress get() = t("Progress", "አፈጻጸም")
  val submitReport get() = t("Submit Report to Manager", "ለሥራ አስኪያጅ ሪፖርት አቅርብ")
  val reportType get() = t("Report Type", "የሪፖርት ዓይነት")
  val reviewReport get() = t("Review Report", "ሪፖርት መርምር")

  // Song & Arts
  val hymnTitle get() = t("Hymn / Mahlet Title", "የዝማሬ ርዕስ")
  val hymnCategory get() = t("Category (St. Gabriel / St. Arsema / Fast)", "ምድብ (ቅ/ገብርኤል፣ ቅ/አርሴማ፣ ጾም)")
  val choirGroup get() = t("Choir Section", "የዝማሬ ቡድን")
  val instruments get() = t("Instruments (Begena, Kebero, Tsenatsil)", "የዜማ መሣሪያዎች (በገና፣ ከበሮ፣ ጸናጽል)")

  // Feedback & Messaging
  val feedbackType get() = t("Feedback Type", "የመልእክት ዓይነት")
  val suggestion get() = t("Suggestion", "ጠቃሚ ሐሳብ")
  val question get() = t("Question", "ጥያቄ")
  val complaint get() = t("Complaint", "ቅሬታ")
  val messageBody get() = t("Message / Content", "የመልእክቱ ይዘት")
  val response get() = t("Admin Response", "የአስተዳዳሪ መልስ")

  // Quick Demo Switching
  val demoLogins get() = t("⚡ Quick Demo Role Logins", "⚡ ፈጣን የሙከራ ሚናዎች")
  val loginAsManager get() = t("Manager (Full Access)", "ዋና ሥራ አስኪያጅ (ሙሉ ሥልጣን)")
  val loginAsFinance get() = t("Finance Admin", "የሒሳብ ክፍል ኃላፊ")
  val loginAsHR get() = t("HR Admin", "የሰው ኃይል ክፍል ኃላፊ")
  val loginAsEducation get() = t("Education Admin", "የትምህርት ክፍል ኃላፊ")
  val loginAsMember get() = t("Sunday School Member", "የሰንበት ት/ቤት አባል")
}
