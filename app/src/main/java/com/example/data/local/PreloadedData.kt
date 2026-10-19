package com.example.data.local

import com.example.data.model.*
import com.example.util.SecurityManager

object PreloadedData {
  val departments = listOf(
    Department(
      id = 1,
      nameEn = "Office & Coordination",
      nameAm = "ጽሕፈት ቤትና አስተባባሪ",
      code = "OFF",
      descriptionEn = "Overall Sunday School advisory, general assembly coordination and bylaws",
      descriptionAm = "አጠቃላይ የሰንበት ት/ቤት አመራር፣ የጠቅላላ ጉባኤ አስተባባሪነትና ደንቦች ቁጥጥር",
      headUserId = 14
    ),
    Department(
      id = 2,
      nameEn = "Plan & Development",
      nameAm = "ዕቅድና ልማት ክፍል",
      code = "PLN",
      descriptionEn = "Strategic plans, development projects, and income-generating efforts",
      descriptionAm = "ስልታዊ ዕቅዶች፣ የልማት ፕሮጀክቶችና የገቢ ማስገኛ ሥራዎች",
      headUserId = 4
    ),
    Department(
      id = 3,
      nameEn = "Education & Training",
      nameAm = "ትምህርትና ሥልጠና ክፍል",
      code = "EDU",
      descriptionEn = "Students, spiritual curriculum, teachers, exams and training",
      descriptionAm = "የተማሪዎች መንፈሳዊ ሥርዓተ-ትምህርት፣ መምህራን፣ ፈተናዎችና ሥልጠናዎች",
      headUserId = 5
    ),
    Department(
      id = 4,
      nameEn = "Program & Assemblies",
      nameAm = "መርሐ ግብርና ጉባኤያት ክፍል",
      code = "PRG",
      descriptionEn = "Church feasts, holidays, spiritual conferences and promotions",
      descriptionAm = "የበዓላት መርሐ ግብሮች፣ መንፈሳዊ ጉባኤያትና የማስተዋወቅ ሥራዎች",
      headUserId = 6
    ),
    Department(
      id = 5,
      nameEn = "Human Resources Management",
      nameAm = "የሰው ኃይል አስተዳደር ክፍል",
      code = "HRM",
      descriptionEn = "Member registration, profiles, recruitment, attendance and welfare",
      descriptionAm = "የአባላት ምዝገባ፣ መገለጫዎች፣ ምልመላ፣ የአገልግሎት ክትትልና ደኅንነት",
      headUserId = 7
    ),
    Department(
      id = 6,
      nameEn = "Information & Internal Relations",
      nameAm = "መረጃና የውስጥ ግንኙነት ክፍል",
      code = "INF",
      descriptionEn = "Announcements, media, archive, documents and church communication",
      descriptionAm = "ማስታወቂያዎች፣ ሚዲያ፣ ሰነዶችና የቤተክርስቲያን መረጃ ልውውጥ",
      headUserId = 8
    ),
    Department(
      id = 7,
      nameEn = "Song & Arts",
      nameAm = "መዝሙርና ሥነ ጥበብ ክፍል",
      code = "SNG",
      descriptionEn = "Hymns, choirs, spiritual plays, instruments (Begena, Kebero, Tsenatsil)",
      descriptionAm = "ዝማሬያት፣ መዘምራን፣ ድራማና ሥነ-ጥበብ፣ የዜማ መሣሪያዎች (በገና፣ ከበሮ፣ ጸናጽል)",
      headUserId = 9
    ),
    Department(
      id = 8,
      nameEn = "Finance & Property Management",
      nameAm = "ሒሳብና ንብረት አስተዳደር ክፍል",
      code = "FIN",
      descriptionEn = "Member dues, receipts, budgets, property inventory and audits",
      descriptionAm = "የአባላት ወርሃዊ መዋጮ፣ ደረሰኞች፣ በጀት፣ የንብረት ምዝገባና ቁጥጥር",
      headUserId = 10
    )
  )

  val users = listOf(
    // 2 Executive Managers (Managed via Firebase Auth & Manager Credential UI)
    User(
      id = 1,
      email = "manager@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("manager123"),
      fullName = "Dn. Yohannes Girma / ዲ/ን ዮሐንስ ግርማ",
      phone = "+251911223344",
      role = "Manager",
      departmentId = null,
      isApproved = true
    ),
    User(
      id = 2,
      email = "manager2@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("manager123"),
      fullName = "Dn. Tewodros Assefa / ዲ/ን ቴዎድሮስ አሰፋ",
      phone = "+251922334455",
      role = "Manager",
      departmentId = null,
      isApproved = true
    ),
    // 7 Department Administrators (Editable by Manager)
    User(
      id = 3,
      email = "education@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("dept123"),
      fullName = "Mihretu Belay / ምሕረቱ በላይ",
      phone = "+251944667788",
      role = "DeptAdmin",
      departmentId = 3, // Education & Training
      isApproved = true
    ),
    User(
      id = 4,
      email = "song@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("dept123"),
      fullName = "Dn. Ermias Bekele / ዲ/ን ኤርምያስ በቀለ",
      phone = "+251988001122",
      role = "DeptAdmin",
      departmentId = 7, // Song & Arts
      isApproved = true
    ),
    User(
      id = 5,
      email = "plan@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("dept123"),
      fullName = "Alemayehu Tadesse / ዓለማየሁ ታደሰ",
      phone = "+251933556677",
      role = "DeptAdmin",
      departmentId = 2, // Plan & Development
      isApproved = true
    ),
    User(
      id = 6,
      email = "program@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("dept123"),
      fullName = "Biniam Kebede / ቢንያም ከበደ",
      phone = "+251955778899",
      role = "DeptAdmin",
      departmentId = 4, // Program & Assemblies
      isApproved = true
    ),
    User(
      id = 7,
      email = "hr@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("dept123"),
      fullName = "Selamawit Desta / ሰላማዊት ደስታ",
      phone = "+251966889900",
      role = "DeptAdmin",
      departmentId = 5, // Human Resources
      isApproved = true
    ),
    User(
      id = 8,
      email = "info@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("dept123"),
      fullName = "Bereket Kassahun / በረከት ካሣሁን",
      phone = "+251977990011",
      role = "DeptAdmin",
      departmentId = 6, // Information & Relations
      isApproved = true
    ),
    User(
      id = 9,
      email = "finance@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("dept123"),
      fullName = "Martha Worku / ማርታ ወርቁ",
      phone = "+251999112233",
      role = "DeptAdmin",
      departmentId = 8, // Finance & Property
      isApproved = true
    ),
    // Pending Department Admin Candidate awaiting Manager Approval
    User(
      id = 10,
      email = "deptapplicant@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("dept123"),
      fullName = "Tilahun Abebe / ጥላሁን አበበ",
      phone = "+251911998877",
      role = "DeptAdmin",
      departmentId = 7, // Song & Arts Candidate
      isApproved = false // Awaiting Manager Approval
    ),
    // Sunday School Members
    User(
      id = 11,
      email = "ephrem@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("member123"),
      fullName = "Ephrem Hailu / ኤፍሬም ኃይሉ",
      phone = "+251912345678",
      role = "Member",
      departmentId = null,
      isApproved = true
    ),
    User(
      id = 12,
      email = "bethlehem@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("member123"),
      fullName = "Bethlehem Kifle / ቤተልሔም ክፍሌ",
      phone = "+251923456789",
      role = "Member",
      departmentId = 7,
      isApproved = true
    ),
    User(
      id = 13,
      email = "natnael@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("member123"),
      fullName = "Natnael Zewdu / ናትናኤል ዘውዱ",
      phone = "+251934567890",
      role = "Member",
      departmentId = 3,
      isApproved = true
    ),
    // Department Writer / Secretary (ጽሕፈት ቤትና የክፍሎች ቃለ-ጉባኤ ጸሐፊ)
    User(
      id = 14,
      email = "writer@meseretehiwot.org",
      passwordHash = SecurityManager.hashPassword("writer123"),
      fullName = "Kidus Melaku / ኪዱስ መላኩ",
      phone = "+251911335577",
      role = "DeptAdmin",
      departmentId = 1, // Office & Coordination / ጽሕፈት ቤትና አስተባባሪ
      isApproved = true,
      approvalStatus = "APPROVED",
      approvedBy = "Executive Manager"
    )
  )

  val paymentSettings = listOf(
    PaymentSetting(
      category = "Worker",
      nameEn = "Worker Member",
      nameAm = "የሠራተኛ አባል",
      amount = 100.0,
      effectiveFrom = "2019 E.C.",
      updatedBy = "Manager"
    ),
    PaymentSetting(
      category = "StudentWorker",
      nameEn = "Student Worker",
      nameAm = "ተማሪና ሠራተኛ አባል",
      amount = 50.0,
      effectiveFrom = "2019 E.C.",
      updatedBy = "Manager"
    ),
    PaymentSetting(
      category = "NonWorker",
      nameEn = "Non-Worker / Student",
      nameAm = "የማይሠራ / ተማሪ አባል",
      amount = 20.0,
      effectiveFrom = "2019 E.C.",
      updatedBy = "Manager"
    )
  )

  val members = listOf(
    MemberItem(
      id = 1,
      memberId = "MH-2015-001",
      fullName = "Ephrem Hailu / ኤፍሬም ኃይሉ",
      gender = "Male",
      age = 29,
      phone = "+251912345678",
      email = "ephrem@meseretehiwot.org",
      address = "Bole Arabsa Site 3, Block 24",
      memberType = "Regular",
      memberCategory = "Worker",
      classGrade = "Grade 3 - Zikre Kidusan",
      departmentId = 5,
      joinDate = "2021-09-12",
      isBaptized = true,
      confessorFather = "Kesis Melaku / ቀሲስ መላኩ",
      status = "Active",
      uniformAccessApproved = false,
      notes = "Faithful Sunday Liturgy servant and senior youth committee"
    ),
    MemberItem(
      id = 2,
      memberId = "MH-2016-042",
      fullName = "Bethlehem Kifle / ቤተልሔም ክፍሌ",
      gender = "Female",
      age = 22,
      phone = "+251923456789",
      email = "bethlehem@meseretehiwot.org",
      address = "Bole Arabsa Site 2, Block 11",
      memberType = "Regular",
      memberCategory = "StudentWorker",
      classGrade = "Grade 2 - Kidus Yared",
      departmentId = 7, // Song & Arts
      joinDate = "2022-10-05",
      isBaptized = true,
      confessorFather = "Megabe Hadis Girma / መጋቤ ሐዲስ ግርማ",
      status = "Active",
      uniformAccessApproved = true,
      notes = "Active soprano vocalist in the Sunday School choir"
    ),
    MemberItem(
      id = 3,
      memberId = "MH-2017-109",
      fullName = "Natnael Zewdu / ናትናኤል ዘውዱ",
      gender = "Male",
      age = 19,
      phone = "+251934567890",
      email = "natnael@meseretehiwot.org",
      address = "Bole Arabsa Site 1, Block 5",
      memberType = "Regular",
      memberCategory = "NonWorker",
      classGrade = "Grade 1 - Senbet Nigest",
      departmentId = 3,
      joinDate = "2023-09-15",
      isBaptized = true,
      confessorFather = "Kesis Melaku / ቀሲስ መላኩ",
      status = "Active",
      uniformAccessApproved = false,
      notes = "Dedicated student in Geez language and dogma courses"
    ),
    MemberItem(
      id = 4,
      memberId = "MH-2018-072",
      fullName = "Dn. Yared Tesfaye / ዲ/ን ያሬድ ተስፋዬ",
      gender = "Male",
      age = 25,
      phone = "+251945112233",
      email = "yared@meseretehiwot.org",
      address = "Bole Arabsa Site 1, Block 14",
      memberType = "Regular",
      memberCategory = "Worker",
      classGrade = "Grade 3 - Zikre Kidusan",
      departmentId = 7, // Song & Arts
      joinDate = "2020-03-15",
      isBaptized = true,
      confessorFather = "Kesis Melaku / ቀሲስ መላኩ",
      status = "Active",
      uniformAccessApproved = true,
      notes = "Lead tenor soloist and Kebero / Begena instructor"
    ),
    MemberItem(
      id = 5,
      memberId = "MH-2019-018",
      fullName = "Hanna Samuel / ሐና ሳሙኤል",
      gender = "Female",
      age = 21,
      phone = "+251956223344",
      email = "hanna@meseretehiwot.org",
      address = "Bole Arabsa Site 2, Block 7",
      memberType = "Regular",
      memberCategory = "StudentWorker",
      classGrade = "Grade 2 - Kidus Yared",
      departmentId = 7, // Song & Arts
      joinDate = "2021-11-20",
      isBaptized = true,
      confessorFather = "Megabe Hadis Girma / መጋቤ ሐዲስ ግርማ",
      status = "Active",
      uniformAccessApproved = true,
      notes = "Faithful alto choir vocalist with 100% rehearsal attendance"
    ),
    MemberItem(
      id = 6,
      memberId = "MH-2019-055",
      fullName = "Abel Kebede / አቤል ከበደ",
      gender = "Male",
      age = 23,
      phone = "+251967334455",
      email = "abel@meseretehiwot.org",
      address = "Bole Arabsa Site 4, Block 2",
      memberType = "Regular",
      memberCategory = "StudentWorker",
      classGrade = "Grade 2 - Kidus Yared",
      departmentId = 7, // Song & Arts
      joinDate = "2023-01-10",
      isBaptized = true,
      confessorFather = "Kesis Hailu / ቀሲስ ኃይሉ",
      status = "Active",
      uniformAccessApproved = false,
      notes = "Bass vocalist - requires 2 more rehearsals for holiday uniform"
    ),
    MemberItem(
      id = 7,
      memberId = "MH-2019-090",
      fullName = "Selamawit Girma / ሰላማዊት ግርማ",
      gender = "Female",
      age = 20,
      phone = "+251978445566",
      email = "selam@meseretehiwot.org",
      address = "Bole Arabsa Site 3, Block 19",
      memberType = "Regular",
      memberCategory = "NonWorker",
      classGrade = "Grade 1 - Senbet Nigest",
      departmentId = 7, // Song & Arts
      joinDate = "2023-06-15",
      isBaptized = true,
      confessorFather = "Kesis Melaku / ቀሲስ መላኩ",
      status = "Active",
      uniformAccessApproved = false,
      notes = "New choir apprentice - rehearsal attendance currently at 65%"
    )
  )

  val payments = listOf(
    PaymentItem(
      id = 1,
      paymentId = "PAY-2019-001",
      memberId = "MH-2015-001",
      memberName = "Ephrem Hailu",
      memberCategory = "Worker",
      amountDue = 100.0,
      amountPaid = 100.0,
      paymentMonth = "Meskerem / መስከረም",
      paymentYear = 2019,
      paymentDate = "2024-09-15",
      paymentMethod = "Telebirr",
      verifiedBy = "Martha Worku (Finance Admin)",
      verified = true,
      verificationDate = "2024-09-16",
      receiptNumber = "RCT-88421",
      notes = "Full payment received & verified"
    ),
    PaymentItem(
      id = 2,
      paymentId = "PAY-2019-002",
      memberId = "MH-2016-042",
      memberName = "Bethlehem Kifle",
      memberCategory = "StudentWorker",
      amountDue = 50.0,
      amountPaid = 50.0,
      paymentMonth = "Meskerem / መስከረም",
      paymentYear = 2019,
      paymentDate = "2024-09-18",
      paymentMethod = "CBE Birr",
      verifiedBy = "Martha Worku (Finance Admin)",
      verified = true,
      verificationDate = "2024-09-19",
      receiptNumber = "RCT-88422",
      notes = "Verified via bank SMS"
    ),
    PaymentItem(
      id = 3,
      paymentId = "PAY-2019-003",
      memberId = "MH-2017-109",
      memberName = "Natnael Zewdu",
      memberCategory = "NonWorker",
      amountDue = 20.0,
      amountPaid = 20.0,
      paymentMonth = "Meskerem / መስከረም",
      paymentYear = 2019,
      paymentDate = "2024-09-20",
      paymentMethod = "Cash at Office",
      verifiedBy = "Dn. Yohannes Girma (Manager Override)",
      verified = true,
      verificationDate = "2024-09-20",
      receiptNumber = "RCT-88423",
      notes = "Cash deposited directly to finance safe"
    ),
    PaymentItem(
      id = 4,
      paymentId = "PAY-2019-004",
      memberId = "MH-2018-072",
      memberName = "Dn. Yared Tesfaye",
      memberCategory = "Worker",
      amountDue = 100.0,
      amountPaid = 100.0,
      paymentMonth = "Meskerem / መስከረም",
      paymentYear = 2019,
      paymentDate = "2024-09-22",
      paymentMethod = "Telebirr",
      verifiedBy = "",
      verified = false,
      verificationDate = "",
      receiptNumber = "",
      notes = "Uploaded transaction screenshot, pending verification"
    ),
    PaymentItem(
      id = 5,
      paymentId = "PAY-2019-005",
      memberId = "MH-2019-018",
      memberName = "Hanna Samuel",
      memberCategory = "StudentWorker",
      amountDue = 50.0,
      amountPaid = 50.0,
      paymentMonth = "Meskerem / መስከረም",
      paymentYear = 2019,
      paymentDate = "2024-09-23",
      paymentMethod = "CBE Birr",
      verifiedBy = "Martha Worku (Finance Admin)",
      verified = true,
      verificationDate = "2024-09-24",
      receiptNumber = "RCT-88425",
      notes = "Transfer confirmed and verified"
    )
  )

  val attendanceRecords = listOf(
    AttendanceRecord(
      id = 1,
      memberId = "MH-2015-001",
      memberName = "Ephrem Hailu",
      date = "2024-09-08",
      serviceType = "Sunday Liturgy & Assembly",
      status = "Present",
      recordedBy = "Selamawit Desta (HR Admin)",
      notes = "Attended morning Qeddase and assembly"
    ),
    AttendanceRecord(
      id = 2,
      memberId = "MH-2015-001",
      memberName = "Ephrem Hailu",
      date = "2024-09-15",
      serviceType = "Sunday Liturgy & Assembly",
      status = "Present",
      recordedBy = "Selamawit Desta (HR Admin)",
      notes = "On-time attendance"
    ),
    AttendanceRecord(
      id = 3,
      memberId = "MH-2016-042",
      memberName = "Bethlehem Kifle",
      date = "2024-09-08",
      serviceType = "Choir Rehearsal",
      status = "Present",
      recordedBy = "Dn. Ermias Bekele (Song Admin)",
      notes = "Participated in St. Gabriel feast hymn preparation"
    ),
    AttendanceRecord(
      id = 4,
      memberId = "MH-2016-042",
      memberName = "Bethlehem Kifle",
      date = "2024-09-15",
      serviceType = "Sunday Liturgy & Assembly",
      status = "Excused",
      recordedBy = "Selamawit Desta (HR Admin)",
      notes = "Permitted for university examination"
    ),
    AttendanceRecord(
      id = 5,
      memberId = "MH-2017-109",
      memberName = "Natnael Zewdu",
      date = "2024-09-15",
      serviceType = "Sunday School Class",
      status = "Present",
      recordedBy = "Mihretu Belay (Education Admin)",
      notes = "Present in Dogma classroom"
    )
  )

  val plans = listOf(
    PlanItem(
      id = 1,
      title = "St. Gabriel Annual Spiritual Festival & Assembly",
      departmentId = 4,
      deptName = "Program & Assemblies",
      startDate = "2024-10-01",
      endDate = "2024-10-19",
      target = "Organize solemn liturgy, reception for 2,500 faithful and youth seminar",
      measurement = "Event report, attendance roster, financial balance sheet",
      weight = 30,
      budget = 85000.0,
      status = "Approved",
      approvedBy = "Dn. Yohannes Girma (Manager)"
    ),
    PlanItem(
      id = 2,
      title = "Curriculum Modernization & Geez Language Workshop",
      departmentId = 3,
      deptName = "Education & Training",
      startDate = "2024-09-20",
      endDate = "2024-12-30",
      target = "Train 150 students across 3 grade levels with published books",
      measurement = "Exam performance, attendance logs",
      weight = 25,
      budget = 35000.0,
      status = "Approved",
      approvedBy = "Dn. Yohannes Girma (Manager)"
    ),
    PlanItem(
      id = 3,
      title = "New Youth Member Recruitment & Spiritual Orientation",
      departmentId = 5,
      deptName = "Human Resources",
      startDate = "2024-09-10",
      endDate = "2024-11-15",
      target = "Register and integrate 80 new young Sunday School students",
      measurement = "Verified registration forms, assigned confessor fathers",
      weight = 20,
      budget = 15000.0,
      status = "InProgress",
      approvedBy = "Dn. Tewodros Assefa (Manager)"
    ),
    PlanItem(
      id = 4,
      title = "Sunday School Sound System & Begena Instrument Acquisition",
      departmentId = 7,
      deptName = "Song & Arts",
      startDate = "2024-10-15",
      endDate = "2024-12-15",
      target = "Purchase 4 traditional Begena instruments and 2 wireless microphones",
      measurement = "Property log inventory and acoustic inspection",
      weight = 15,
      budget = 42000.0,
      status = "Pending",
      approvedBy = ""
    )
  )

  val reports = listOf(
    ReportItem(
      id = 1,
      title = "Meskerem 2019 E.C. Education Progress Report",
      departmentId = 3,
      deptName = "Education & Training",
      reportType = "Monthly",
      period = "Meskerem 2019",
      submittedBy = "Mihretu Belay (Education Admin)",
      submittedDate = "2024-09-24",
      summary = "Conducted 4 weekly sessions. 142 students attended consistently. Geez grammar level 1 textbooks distributed.",
      status = "Approved",
      reviewedBy = "Dn. Yohannes Girma (Manager)",
      comments = "Commendable progress. Please ensure attendance logs are cross-checked with HR."
    ),
    ReportItem(
      id = 2,
      title = "First Quarter Financial Dues Collection & Expense Audit",
      departmentId = 8,
      deptName = "Finance & Property",
      reportType = "Quarterly",
      period = "Q1 2019 E.C.",
      submittedBy = "Martha Worku (Finance Admin)",
      submittedDate = "2024-09-25",
      summary = "Total collections ETB 128,450. Verified 86% of member monthly dues. Bank reconciliation attached.",
      status = "Pending",
      reviewedBy = "",
      comments = ""
    )
  )

  val announcements = listOf(
    AnnouncementItem(
      id = 1,
      titleEn = "Celebration of Debre Hayil St. Gabriel & St. Arsema Feast",
      titleAm = "የደብረ ኃይል ቅዱስ ገብርኤል እና ቅድስት አርሴማ ታላቅ ዓመታዊ በዓል",
      bodyEn = "All Sunday School members and faithful are cordially invited to the grand spiritual celebration with Qeddase, hymns, and blessing.",
      bodyAm = "የሰንበት ት/ቤታችን አባላትና ምእመናን በሙሉ በቅዱስ ገብርኤልና በቅድስት አርሴማ ዓመታዊ በዓል ላይ በዝማሬና በአገልግሎት እንድትገኙ ተጠርታችኋል።",
      postedBy = "Management / ዋና አስተዳደር",
      targetAudience = "All Members",
      datePosted = "2024-09-20",
      isPinned = true
    ),
    AnnouncementItem(
      id = 2,
      titleEn = "New Academic Year Spiritual Education Orientation",
      titleAm = "የ2019 ዓ.ም. የመንፈሳዊ ትምህርት አጀማመር ኦሬንቴሽን",
      bodyEn = "Sunday School classes will begin this coming Sunday morning at 8:00 AM. Please arrive on time with your exercise books.",
      bodyAm = "የ2019 ዓ.ም. የመንፈሳዊ ትምህርት ክፍል ምደባና ትምህርት እሁድ ጠዋት 2:00 ሰዓት ይጀምራል። አባላት በሰዓቱ እንድትገኙ እናሳስባለን።",
      postedBy = "Education Dept / ትምህርት ክፍል",
      targetAudience = "Students & Teachers",
      datePosted = "2024-09-22",
      isPinned = true
    ),
    AnnouncementItem(
      id = 3,
      titleEn = "Monthly Sunday School Contribution Settlement Reminder",
      titleAm = "የወርሃዊ መዋጮ ክፍያ ማሳሰቢያ",
      bodyEn = "Members are reminded to settle their monthly contribution via Telebirr or CBE Birr and upload the receipt reference.",
      bodyAm = "የሰንበት ትምህርት ቤታችን አባላት በሙሉ የዚህን ወር መዋጮ በቴሌብር ወይም በንግድ ባንክ በመክፈል ደረሰኙን እንድታስገቡ በትህትና እናሳስባለን።",
      postedBy = "Finance Dept / ሒሳብ ክፍል",
      targetAudience = "All Members",
      datePosted = "2024-09-24",
      isPinned = false
    )
  )

  val hymns = listOf(
    HymnItem(
      id = 1,
      title = "ገብርኤል ዋስ ነው ለእኔ (Gabriel Was New Lene)",
      category = "St. Gabriel",
      choirGroup = "Senior Choir / ዋናው መዘምራን",
      leadSinger = "Dn. Ermias Bekele",
      notes = "Chanted during St. Gabriel Tabot procession"
    ),
    HymnItem(
      id = 2,
      title = "አርሴማ ቅድስት ሰማዕት (Arsema Kidist Sema'it)",
      category = "St. Arsema",
      choirGroup = "Youth Choir / የወጣቶች መዘምራን",
      leadSinger = "Bethlehem Kifle",
      notes = "Traditional Ethiopian hymn honoring the martyrdom of St. Arsema"
    ),
    HymnItem(
      id = 3,
      title = "በመስቀሉ ሰላምን አደረገ (BeMeskelu Selamn Aderege)",
      category = "Meskel",
      choirGroup = "General Choir / ጠቅላላ መዘምራን",
      leadSinger = "Dn. Yohannes Girma",
      notes = "Meskel feast Demera hymn"
    )
  )

  val grades = listOf(
    GradeItem(
      id = 1,
      studentId = "MH-2015-001",
      studentName = "Ephrem Hailu",
      subject = "Orthodox Dogma & Sacraments / ሃይማኖተ አበውና ምሥጢራተ ቤተክርስቲያን",
      term = "Term 1",
      score = 94.5,
      grade = "A",
      teacherName = "Mihretu Belay",
      date = "2024-06-20",
      comments = "Exemplary understanding of the holy mystery of the Trinity"
    ),
    GradeItem(
      id = 2,
      studentId = "MH-2016-042",
      studentName = "Bethlehem Kifle",
      subject = "Geez Language Grammar & Hymns / የግዕዝ ቋንቋና ዜማ",
      term = "Term 1",
      score = 89.0,
      grade = "A-",
      teacherName = "Mihretu Belay",
      date = "2024-06-20",
      comments = "Strong melodic recitation and verb conjugations"
    ),
    GradeItem(
      id = 3,
      studentId = "MH-2017-109",
      studentName = "Natnael Zewdu",
      subject = "Church History & Patrology / የቤተክርስቲያን ታሪክ",
      term = "Term 1",
      score = 91.0,
      grade = "A",
      teacherName = "Mihretu Belay",
      date = "2024-06-20",
      comments = "Active participation and dedicated research"
    )
  )

  val documents = listOf(
    DocumentItem(
      id = 1,
      title = "Bylaws of Meserete Hiwot Sunday School (ደንብና መመሪያ)",
      category = "Bylaws",
      uploadedBy = "Manager",
      date = "2024-01-10",
      accessLevel = "All",
      fileDescription = "Approved foundational bylaws governing Sunday School membership and roles"
    ),
    DocumentItem(
      id = 2,
      title = "Annual Strategic Plan 2019 E.C. (ዓመታዊ ዕቅድ 2019)",
      category = "Plan",
      uploadedBy = "Plan & Development",
      date = "2024-09-01",
      accessLevel = "Admins",
      fileDescription = "Consolidated budget and milestone plan for all 7 departments"
    ),
    DocumentItem(
      id = 3,
      title = "Sunday School Financial Contribution Policy (የመዋጮ መመሪያ)",
      category = "Financial Policy",
      uploadedBy = "Finance & Property",
      date = "2024-08-15",
      accessLevel = "All",
      fileDescription = "Rules for Worker, Student Worker, and Non-Worker contribution dues"
    )
  )

  val feedbackList = listOf(
    FeedbackItem(
      id = 1,
      submittedBy = "MH-2015-001",
      submitterName = "Ephrem Hailu",
      type = "Suggestion",
      subject = "Spiritual Library Expansion / የመንፈሳዊ ቤተ-መጻሕፍት ማስፋፊያ",
      message = "It would be wonderful to add more Ge'ez-Amharic translation prayer and commentary books to our Sunday School library.",
      date = "2024-09-18",
      status = "Addressed",
      handledBy = "Dn. Yohannes Girma (Manager)",
      response = "Thank you brother Ephrem! The Education department has already set aside ETB 10,000 for new book purchases this quarter."
    ),
    FeedbackItem(
      id = 2,
      submittedBy = "MH-2016-042",
      submitterName = "Bethlehem Kifle",
      type = "Question",
      subject = "Choir Rehearsal Schedule for Meskel / የመስቀል ዝማሬ ልምምድ ሰዓት",
      message = "Will extra evening choir rehearsals be held on Wednesdays ahead of the Demera festival?",
      date = "2024-09-21",
      status = "Resolved",
      handledBy = "Dn. Ermias Bekele (Song Admin)",
      response = "Yes, rehearsals will take place Wednesdays from 5:30 PM to 7:00 PM at the main hall."
    )
  )

  val messages = listOf(
    MessageItem(
      id = 1,
      senderId = "admin@meseretehiwot.org",
      senderName = "Sunday School Office",
      receiverId = "MH-2015-001",
      subject = "Welcome to the 2019 E.C. Sunday School Service Year",
      body = "Peace and blessings of St. Gabriel and St. Arsema be with you and your family. We thank you for your committed service.",
      dateSent = "2024-09-15 09:30",
      isRead = true
    ),
    MessageItem(
      id = 2,
      senderId = "MH-2015-001",
      senderName = "Ephrem Hailu",
      receiverId = "admin@meseretehiwot.org",
      subject = "Re: Confessor Father Meeting",
      body = "Greetings Father, I would like to arrange spiritual counseling with Kesis Melaku this coming Saturday.",
      dateSent = "2024-09-22 14:15",
      isRead = false
    )
  )

  val bibleQuizQuestions = listOf(
    // Story 1: Archangel St. Gabriel & the Three Children
    BibleQuizQuestion(
      id = "q1_1",
      storyId = "story_1",
      questionAmharic = "በባቢሎን ንጉሥ በናቡከደነፆር ዘመን ለወርቁ ምስል አንሰግድም ያሉት ሦስቱ ወጣቶች እነማን ነበሩ?",
      questionEnglish = "Who were the three holy youths that refused to worship the golden idol in Babylon?",
      optionA = "ጴጥሮስ፣ ዮሐንስና ያዕቆብ",
      optionB = "ሲድራቅ፣ ሚሳቅና አብደናጎ (አናንያ፣ አዛርያና ሚሳኤል)",
      optionC = "ኤልያስ፣ ኤልሳዕና ኢሳይያስ",
      optionD = "ዳዊት፣ ሰሎሞንና ዮናታን",
      correctOptionIndex = 1,
      explanationAmharic = "በዳንኤል ምዕራፍ 3 ላይ እንደተመዘገበው ሲድራቅ፣ ሚሳቅና አብደናጎ (አናንያ፣ አዛርያና ሚሳኤል) ለእውነተኛው አምላክ በመታመን ለጣዖት አልሰገዱም።",
      explanationEnglish = "According to Daniel 3, Shadrach, Meshach, and Abednego (Ananias, Azarias, and Misael) refused to bow to Nebuchadnezzar's golden image."
    ),
    BibleQuizQuestion(
      id = "q1_2",
      storyId = "story_1",
      questionAmharic = "ወደ እቶነ እሳቱ ተወርውረው በነበሩበት ጊዜ ሊቀ መላእክት ቅዱስ ገብርኤል እሳቱን ምን አደረገው?",
      questionEnglish = "When the youths were cast into the furnace, what did Archangel Gabriel turn the fire into?",
      optionA = "ወደ ውኃ ጎርፍ",
      optionB = "ወደ ከባድ አውሎ ነፋስ",
      optionC = "እንደ ቀዝቃዛ ጤዛና ነፋስ አደረገው",
      optionD = "ወደ ድንጋይ ለወጠው",
      correctOptionIndex = 2,
      explanationAmharic = "ቅዱስ ገብርኤል ፈጥኖ በመውረድ እሳቱን እንደ ቀዝቃዛ ጤዛ አደረገው፤ ፀጉራቸው እንኳ ሳይቃጠል አዳናቸው።",
      explanationEnglish = "Archangel Gabriel drove the flame away and made the midst of the furnace like a moist and cooling wind."
    ),
    // Story 2: St. Arsema
    BibleQuizQuestion(
      id = "q2_1",
      storyId = "story_2",
      questionAmharic = "ቅድስት አርሴማ ንጉሥ ዲዮቅልጥያኖስ በውበቷ ተማርኮ ሊያገባት በፈለገ ጊዜ ምን አደረገች?",
      questionEnglish = "What did St. Arsema do when Emperor Diocletian sought to marry her?",
      optionA = "የዓለምን ክብር ንቃ ከቅድስት አጋታ ጋር ወደ አርሜንያ ተሰደደች",
      optionB = "ንግሥት ለመሆን ተስማማች",
      optionC = "ከጦር ሠራዊት ጋር ተዋጋች",
      optionD = "ሀብቱን ሁሉ ወሰደች",
      correctOptionIndex = 0,
      explanationAmharic = "ቅድስት አርሴማ የክርስቶስ ሙሽራ በመሆኗ የዚህን ዓለም ከንቱ ክብር ንቃ በጾምና በጸሎት ጸንታ ወደ አርሜንያ ተሰደደች።",
      explanationEnglish = "St. Arsema renounced all imperial pomp and fled with her mother St. Agatha and monastic sisters to Armenia."
    ),
    BibleQuizQuestion(
      id = "q2_2",
      storyId = "story_2",
      questionAmharic = "በራእይ 2፥10 ላይ ለታመኑ ሰማዕታት ምን እንደሚሰጣቸው ጌታ ተናገረ?",
      questionEnglish = "According to Revelation 2:10, what does the Lord promise to those faithful unto death?",
      optionA = "የብር አክሊል",
      optionB = "የምድር መንግሥት",
      optionC = "የሕይወት አክሊል (The Crown of Life)",
      optionD = "የወርቅ ቤተመንግሥት",
      correctOptionIndex = 2,
      explanationAmharic = "«እስከ ሞት ድረስ የታመንህ ሁን የሕይወትንም አክሊል እሰጥሃለሁ» (ራእይ 2፥10)።",
      explanationEnglish = "«Be faithful until death, and I will give you the crown of life» (Revelation 2:10)."
    ),
    // Story 3: King David & Ark of Covenant
    BibleQuizQuestion(
      id = "q3_1",
      storyId = "story_3",
      questionAmharic = "ንጉሥ ቅዱስ ዳዊት ታቦተ ጽዮን ወደ ኢየሩሳሌም በገባች ጊዜ እንዴት አመሰገነ?",
      questionEnglish = "How did King David praise God when the Ark of the Covenant entered Jerusalem?",
      optionA = "በዙፋኑ ላይ ተቀምጦ ተመለከተ",
      optionB = "የበፍታ ኤፉድ ለብሶ በሙሉ ኃይሉ፣ በከበሮና በጸናጽል አመሰገነ",
      optionC = "ዝምታን መረጠ",
      optionD = "በሠረገላ ብቻ ተጓዘ",
      correctOptionIndex = 1,
      explanationAmharic = "ቅዱስ ዳዊት የንጉሥነቱን ልብስ አውልቆ በትሕትና የበፍታ ልብስ ለብሶ በከበሮና በጸናጽል በእግዚአብሔር ፊት ዘመረ።",
      explanationEnglish = "Wearing a simple linen ephod, King David danced and praised before the Ark with Kebero and cymbals with all his heart."
    ),
    // Story 4: Good Samaritan
    BibleQuizQuestion(
      id = "q4_1",
      storyId = "story_4",
      questionAmharic = "በወንበዴዎች እጅ ለወደቀውና ለቆሰለው ሰው እውነተኛ ርኅራኄን ያሳየው ማነው?",
      questionEnglish = "Who showed true mercy and bound the wounds of the man fallen among thieves?",
      optionA = "መንገድ ቀይሮ ያለፈው ካህን",
      optionB = "ሌዋዊው",
      optionC = "ቸሩ ሳምራዊ (The Good Samaritan)",
      optionD = "የከተማው ገዢ",
      correctOptionIndex = 2,
      explanationAmharic = "ካህኑና ሌዋዊው አልፈው ሲሄዱ፣ ሳምራዊው ቁስሉን በዘይትና በወይን ጠጅ አክሞ፣ በበቅሎው ጭኖ ወደ እንግዳ ማረፊያ ወሰደው።",
      explanationEnglish = "While the priest and Levite walked by, the Samaritan bound his wounds, paid for his lodging, and showed Christ-like mercy."
    ),
    // Story 5: Wedding at Cana
    BibleQuizQuestion(
      id = "q5_1",
      storyId = "story_5",
      questionAmharic = "በቃና ዘገሊላ ሰርግ ላይ የወይን ጠጅ ባለቀ ጊዜ እመቤታችን ቅድስት ድንግል ማርያም ለአገልጋዮቹ ምን አለቻቸው?",
      questionEnglish = "At the wedding at Cana, what did the Virgin Mary instruct the servants when the wine ran out?",
      optionA = "«ወደ ገበያ ሂዱ»",
      optionB = "«የሚላችሁን ሁሉ አድርጉ» (Whatever He says to you, do it)",
      optionC = "«ሰርጉን አቁሙ»",
      optionD = "«ውኃ አትቅዱ»",
      correctOptionIndex = 1,
      explanationAmharic = "እመቤታችን ለአገልጋዮቹ «የሚላችሁን ሁሉ አድርጉ» (ዮሐ 2፥5) በማለት ፍጹም ታዛዥነትን አስተማረችን።",
      explanationEnglish = "The Blessed Mother said: 'Whatever He says to you, do it' (John 2:5), turning sorrow into miracle through Christ."
    )
  )

  val lessonNotes = listOf(
    LessonNote(
      id = 1,
      lessonId = "LESSON-2024-W1",
      title = "The Orthodox Dogma of the Holy Trinity",
      titleAmharic = "የቅድስት ሥላሴ አንድነትና ሦስትነት ምስጢር",
      subject = "Orthodox Dogma / ሃይማኖት",
      classGrade = "Grade 9-12 / 9-12ኛ ክፍል",
      weekNumber = 1,
      datePosted = "2024-10-06",
      scriptureReference = "ማቴ 28፥19 / Matthew 28:19",
      summary = "በስመ አብ ወወልድ ወመንፈስ ቅዱስ አሐዱ አምላክ አሜን። እግዚአብሔር በአካል፣ በስም፣ በግብር ሦስት ሲሆን፤ በመለኮት፣ በፈቃድ፣ በሥልጣን፣ በአገዛዝ አንድ አምላክ ነው። አብ ወላዲ፣ ወልድ ተወላዲ፣ መንፈስ ቅዱስ ሠራፂ ነው። ይህ በኦርቶዶክስ ተዋሕዶ ቤተክርስቲያን የእምነታችን መሠረት ነው።",
      memoryVerse = "«እንግዲህ ሂዱና አሕዛብን ሁሉ በአብ በወልድና በመንፈስ ቅዱስ ስም እያጠመቃችኋቸው ደቀ መዛሙርት አድርጓቸው» (ማቴ 28፥19)",
      teacherName = "መምህር ደረጀ / Memhir Dereje",
      teacherEmail = "dereje@meseretehiwot.org"
    ),
    LessonNote(
      id = 2,
      lessonId = "LESSON-2024-W2",
      title = "The Parable of the Good Samaritan: Christian Mercy",
      titleAmharic = "የቸሩ ሳምራዊ ምሳሌና ክርስቲያናዊ ምሕረት",
      subject = "Bible Study / መጽሐፍ ቅዱስ",
      classGrade = "All Classes / ሁሉም ክፍሎች",
      weekNumber = 2,
      datePosted = "2024-10-06",
      scriptureReference = "ሉቃስ 10፥25-37 / Luke 10:25-37",
      summary = "በወንበዴዎች እጅ ለወደቀው ሰው እውነተኛ ፍቅርና ርኅራኄን ያሳየው ሳምራዊው ክርስቶስን ይመስላል። እውነተኛ ባልንጀራነት ያለ አድልዎ የተቸገሩትን መርዳት፣ የታመሙትን ማጽናናትና ክርስቲያናዊ ምሕረትን በተግባር ማሳየት ነው።",
      memoryVerse = "«ሂድ አንተም እንዲሁ አድርግ» (ሉቃስ 10፥37)",
      teacherName = "ዲ/ን ሳሙኤል / Dn. Samuel",
      teacherEmail = "samuel@meseretehiwot.org"
    ),
    LessonNote(
      id = 3,
      lessonId = "LESSON-2024-W3",
      title = "Liturgical Hymns & The Order of St. Yared",
      titleAmharic = "የቅዱስ ያሬድ ዜማ ሥርዓትና የቅዳሴ ዝማሬ",
      subject = "Ethiopic Hymns / ዜማ",
      classGrade = "Choir & Youth / መዘምራንና ወጣቶች",
      weekNumber = 3,
      datePosted = "2024-10-07",
      scriptureReference = "መዝ 150 / Psalms 150",
      summary = "ቅዱስ ያሬድ በስድስተኛው መቶ ክፍለ ዘመን ከመላእክት የተማረውን ሦስቱን የዜማ ስልቶች (ግዕዝ፣ ዕዝል፣ አራራይ) ለኢትዮጵያ ኦርቶዶክስ ተዋሕዶ ቤተክርስቲያን አበርክቷል። በቅዳሴ ጊዜ የሚዘመሩት ምስጋናዎች እግዚአብሔርን በቅድስና የምናመሰግንባቸው የሰማይ ዜማዎች ናቸው።",
      memoryVerse = "«እግዚአብሔርን በመቅደሱ አመስግኑት፤ በኃይሉ ጠፈር አመስግኑት» (መዝ 150፥1)",
      teacherName = "መምህር ኪዳነ / Memhir Kidane",
      teacherEmail = "kidane@meseretehiwot.org"
    )
  )
}
