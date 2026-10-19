package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.User
import com.example.ui.components.ChurchLogo3D
import com.example.ui.components.ChurchSlidingTicker
import com.example.ui.components.ChurchTopAppBar
import com.example.ui.components.motion3DTilt
import com.example.ui.language.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel

@Composable
fun ChurchDynamicImage(
  customUriString: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop,
  contentDescription: String? = "Debre Hayil St. Gabriel & St. Arsema Church"
) {
  val context = LocalContext.current
  val customBitmap = remember(customUriString) {
    if (customUriString.isNullOrBlank()) null
    else {
      try {
        context.contentResolver.openInputStream(Uri.parse(customUriString))?.use { stream ->
          BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
      } catch (_: Exception) {
        null
      }
    }
  }

  if (customBitmap != null) {
    Image(
      bitmap = customBitmap,
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale
    )
  } else {
    Image(
      painter = painterResource(id = R.drawable.img_uploaded_church_bg_1790618101136),
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale
    )
  }
}

@Composable
fun LandingScreen(
  viewModel: SundaySchoolViewModel,
  onNavigateToRegister: () -> Unit,
  onNavigateToAiCounselor: () -> Unit = {},
  onNavigateToBibleStory: () -> Unit = {}
) {
  val context = LocalContext.current
  val allUsers by viewModel.allUsers.collectAsState()
  val departments by viewModel.departments.collectAsState()
  val cloudSyncState by viewModel.cloudSyncState.collectAsState()
  val customBgUri by viewModel.customChurchBackgroundUri.collectAsState()

  // 0 = Sign In (Login), 1 = Register (New Member / Account)
  var portalMode by remember { mutableStateOf(0) }
  // Selected Role on Sign In: "Manager", "DeptAdmin", "Member"
  var selectedRoleTab by remember { mutableStateOf("Manager") }
  var selectedDeptId by remember { mutableStateOf(1L) }

  var emailInput by remember { mutableStateOf("manager@meseretehiwot.org") }
  var passwordInput by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf("") }
  var showPendingApprovalDialog by remember { mutableStateOf<String?>(null) }
  var showConfigureAccountDialog by remember { mutableStateOf(false) }

  // Inline Registration State
  var regRole by remember { mutableStateOf("Member") }
  var regFullName by remember { mutableStateOf("") }
  var regPhone by remember { mutableStateOf("+2519") }
  var regEmail by remember { mutableStateOf("") }
  var regPassword by remember { mutableStateOf("") }
  var regCategory by remember { mutableStateOf("Student") }
  var regDeptId by remember { mutableStateOf(1L) }
  var regErrorMessage by remember { mutableStateOf("") }

  // Automatically sync emailInput when role/department selection changes if users are loaded
  LaunchedEffect(allUsers, selectedRoleTab, selectedDeptId) {
    if (allUsers.isNotEmpty() && emailInput.isBlank()) {
      val match = when (selectedRoleTab) {
        "Manager" -> allUsers.firstOrNull { it.role == "Manager" }
        "DeptAdmin" -> allUsers.firstOrNull { it.role == "DeptAdmin" && it.departmentId == selectedDeptId }
          ?: allUsers.firstOrNull { it.role == "DeptAdmin" }
        else -> allUsers.firstOrNull { it.role == "Member" }
      }
      if (match != null) {
        emailInput = match.email
      }
    }
  }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(
          uri,
          android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
      } catch (_: Exception) {
      }
      viewModel.setCustomChurchBackgroundUri(uri.toString())
    }
  }

  Scaffold(
    topBar = {
      ChurchTopAppBar(
        extraAction = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = ChurchGold.copy(alpha = 0.25f),
              border = androidx.compose.foundation.BorderStroke(1.dp, ChurchGold),
              modifier = Modifier
                .clickable { com.example.util.LocalizationManager.toggleLanguage(context) }
                .testTag("landing_language_switcher_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Translate,
                  contentDescription = null,
                  tint = ChurchLightGold,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (com.example.util.LocalizationManager.currentLanguage == com.example.ui.language.AppLanguage.AMHARIC) "EN" else "አማ",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              modifier = Modifier.testTag("upload_church_bg_button")
            ) {
              Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Change Church Background Photo",
                tint = ChurchLightGold
              )
            }
          }
        }
      )
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(ChurchIvory)
    ) {
      // Subtle Full-Screen Church Background Watermark from the Uploaded Church Photo
      ChurchDynamicImage(
        customUriString = customBgUri,
        modifier = Modifier
          .fillMaxSize()
          .alpha(0.14f),
        contentScale = ContentScale.Crop
      )

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
      ) {
        // Hero Banner featuring the Uploaded Debre Hayil St. Gabriel & St. Arsema Church Image
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(225.dp)
          ) {
            ChurchDynamicImage(
              customUriString = customBgUri,
              contentDescription = "Debre Hayil St. Gabriel and St. Arsema Church",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(
                      Color.Black.copy(alpha = 0.15f),
                      ChurchDarkBlue.copy(alpha = 0.88f)
                    )
                  )
                )
            )

            // Upload / Reset Background Photo Pill at Top-Right of Hero Banner
            Row(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = ChurchDarkBlue.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, ChurchGold),
                modifier = Modifier
                  .clickable {
                    photoPickerLauncher.launch(
                      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                  }
                  .testTag("hero_change_bg_button")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Wallpaper,
                    contentDescription = null,
                    tint = ChurchLightGold,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Church Photo / ፎቶ ቀይር",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
              if (customBgUri != null) {
                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = ChurchRed.copy(alpha = 0.85f),
                  modifier = Modifier.clickable { viewModel.setCustomChurchBackgroundUri(null) }
                ) {
                  Text(
                    text = "Reset",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                  )
                }
              }
            }

            Row(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              ChurchLogo3D(
                size = 66.dp,
                continuousRotation = true
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = ChurchGold.copy(alpha = 0.92f)
                ) {
                  Text(
                    text = "EOTC • ደብረ ኃይል ቅ/ገብርኤልና ቅ/አርሴማ",
                    color = ChurchDarkBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = AppStrings.fullAppName,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = AppStrings.churchName,
                  style = MaterialTheme.typography.bodySmall,
                  color = ChurchPaleGold
                )
              }
            }
          }
        }

        // Sliding Marquee / Live Ticker
        item {
          ChurchSlidingTicker(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
          )
        }

        // 3D Interactive Feature Cards: Spiritual AI & Daily Bible Stories
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Spiritual AI Counselor Card
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = ChurchDeepBlue),
              modifier = Modifier
                .weight(1f)
                .motion3DTilt(maxTiltAngle = 8f)
                .clickable { onNavigateToAiCounselor() }
                .testTag("nav_ai_counselor_card")
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = CircleShape,
                    color = ChurchGold,
                    modifier = Modifier.size(28.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = ChurchDarkBlue, modifier = Modifier.size(18.dp))
                    }
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "AI COUNSELOR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = ChurchLightGold
                  )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "መምህር ደብረ ኃይል",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color.White
                )
                Text(
                  text = "Spiritual counselor & prayer guidance in Amharic & English",
                  fontSize = 10.sp,
                  color = Color.White.copy(alpha = 0.8f),
                  lineHeight = 14.sp
                )
              }
            }

            // Daily Biblical Story Card
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = ChurchPaleGold),
              modifier = Modifier
                .weight(1f)
                .motion3DTilt(maxTiltAngle = 8f)
                .clickable { onNavigateToBibleStory() }
                .testTag("nav_bible_story_card")
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = CircleShape,
                    color = ChurchDeepBlue,
                    modifier = Modifier.size(28.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Default.MenuBook, contentDescription = null, tint = ChurchGold, modifier = Modifier.size(18.dp))
                    }
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "6:30 AM/PM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = ChurchDeepBlue
                  )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "የመጽሐፍ ቅዱስ ታሪክ",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = ChurchDarkBlue
                )
                Text(
                  text = "Daily biblical stories & interactive quizzes",
                  fontSize = 10.sp,
                  color = ChurchTextPrimary,
                  lineHeight = 14.sp
                )
              }
            }
          }
        }

        // UNIFIED LOGIN & REGISTER PORTAL CARD FOR MANAGER, ADMIN, STUDENT & NEW MEMBER
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)),
            border = BorderStroke(1.dp, ChurchGold.copy(alpha = 0.55f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              // Header
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
              ) {
                Surface(
                  shape = CircleShape,
                  color = ChurchPaleGold,
                  modifier = Modifier.size(42.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = if (portalMode == 0) Icons.Default.VerifiedUser else Icons.Default.PersonAdd,
                      contentDescription = null,
                      tint = ChurchDarkGold,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "መግቢያ እና ምዝገባ • Login & Register Portal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ChurchDeepBlue
                  )
                  Text(
                    text = "Easy Portal for Manager, Department Admin, Student & New Member",
                    style = MaterialTheme.typography.bodySmall,
                    color = ChurchTextMuted
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Top Segmented Switcher: [ 1. Sign In / ግባ ] vs [ 2. Register / አዲስ ምዝገባ ]
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = ChurchDarkBlue,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (portalMode == 0) ChurchGold else Color.Transparent,
                    modifier = Modifier
                      .weight(1f)
                      .clickable { portalMode = 0 }
                      .testTag("mode_login_tab")
                  ) {
                    Row(
                      modifier = Modifier.padding(vertical = 10.dp),
                      horizontalArrangement = Arrangement.Center,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        tint = if (portalMode == 0) ChurchDarkBlue else Color.White,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "Sign In / ግባ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (portalMode == 0) ChurchDarkBlue else Color.White
                      )
                    }
                  }

                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (portalMode == 1) ChurchGold else Color.Transparent,
                    modifier = Modifier
                      .weight(1f)
                      .clickable { portalMode = 1 }
                      .testTag("mode_register_tab")
                  ) {
                    Row(
                      modifier = Modifier.padding(vertical = 10.dp),
                      horizontalArrangement = Arrangement.Center,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = if (portalMode == 1) ChurchDarkBlue else Color.White,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "Register / አዲስ ምዝገባ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (portalMode == 1) ChurchDarkBlue else Color.White
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              if (portalMode == 0) {
                // =========================================================================
                // MODE 0: UNIFIED LOGIN FOR MANAGER, DEPT ADMIN, STUDENT & NEW REGISTER
                // =========================================================================
                Text(
                  text = "1. Select Your Role / ሚናዎን ይምረጡ፦",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = ChurchDeepBlue
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  // Role 1: Manager
                  RoleSelectChip(
                    titleEn = "Manager",
                    titleAm = "ሥራ አስኪያጅ",
                    icon = Icons.Default.WorkspacePremium,
                    selected = selectedRoleTab == "Manager",
                    testTag = "role_chip_manager",
                    onClick = {
                      selectedRoleTab = "Manager"
                      val mgr = allUsers.firstOrNull { it.role == "Manager" }
                      emailInput = mgr?.email ?: "manager@meseretehiwot.org"
                      errorMessage = ""
                    }
                  )

                  // Role 2: Dept Admin (All 12 Departments)
                  RoleSelectChip(
                    titleEn = "Dept Admin",
                    titleAm = "የክፍል ኃላፊ",
                    icon = Icons.Default.AdminPanelSettings,
                    selected = selectedRoleTab == "DeptAdmin",
                    testTag = "role_chip_admin",
                    onClick = {
                      selectedRoleTab = "DeptAdmin"
                      val deptAdmin = allUsers.firstOrNull {
                        it.role == "DeptAdmin" && it.departmentId == selectedDeptId
                      } ?: allUsers.firstOrNull { it.role == "DeptAdmin" }
                      emailInput = deptAdmin?.email ?: "education@meseretehiwot.org"
                      errorMessage = ""
                    }
                  )

                  // Role 3: Student / Member
                  RoleSelectChip(
                    titleEn = "Student / Member",
                    titleAm = "ተማሪ እና አባል",
                    icon = Icons.Default.School,
                    selected = selectedRoleTab == "Member",
                    testTag = "role_chip_student",
                    onClick = {
                      selectedRoleTab = "Member"
                      val student = allUsers.firstOrNull { it.role == "Member" }
                      emailInput = student?.email ?: "member@meseretehiwot.org"
                      errorMessage = ""
                    }
                  )

                  // Role 4: New Register Member -> Switches directly to Register mode
                  RoleSelectChip(
                    titleEn = "New Member",
                    titleAm = "አዲስ ተመዝጋቢ",
                    icon = Icons.Default.PersonAddAlt1,
                    selected = false,
                    testTag = "role_chip_new_member",
                    onClick = {
                      portalMode = 1
                    }
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Role-Specific Account & Department Selector Box
                val activeRoleUser: User? = when (selectedRoleTab) {
                  "Manager" -> allUsers.firstOrNull {
                    it.role == "Manager" && it.email.equals(emailInput.trim(), ignoreCase = true)
                  } ?: allUsers.firstOrNull { it.role == "Manager" }

                  "DeptAdmin" -> allUsers.firstOrNull {
                    it.role == "DeptAdmin" && it.departmentId == selectedDeptId
                  } ?: allUsers.firstOrNull {
                    it.role == "DeptAdmin" && it.email.equals(emailInput.trim(), ignoreCase = true)
                  }

                  else -> allUsers.firstOrNull {
                    it.role == "Member" && it.email.equals(emailInput.trim(), ignoreCase = true)
                  } ?: allUsers.firstOrNull { it.role == "Member" }
                }

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = ChurchIvory,
                  border = BorderStroke(1.dp, ChurchBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    when (selectedRoleTab) {
                      "Manager" -> {
                        val managers = allUsers.filter { it.role == "Manager" }
                        Text(
                          text = "Executive Manager Account / የሥራ አስኪያጅ መለያ",
                          fontWeight = FontWeight.Bold,
                          fontSize = 12.sp,
                          color = ChurchDeepBlue
                        )
                        Text(
                          text = "Full access to all 12 departments, User Credentials, Data Center, Firestore Sync & Security Audit.",
                          fontSize = 10.sp,
                          color = ChurchTextSecondary
                        )
                        if (managers.isNotEmpty()) {
                          Spacer(modifier = Modifier.height(6.dp))
                          Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                          ) {
                            managers.forEach { mgr ->
                              FilterChip(
                                selected = emailInput.equals(mgr.email, ignoreCase = true),
                                onClick = { emailInput = mgr.email },
                                label = { Text("${mgr.fullName} (${mgr.email})", fontSize = 11.sp) }
                              )
                            }
                          }
                        }
                      }

                      "DeptAdmin" -> {
                        Text(
                          text = "2. Select Department (All ${departments.size} Departments) / ክፍል ይምረጡ፦",
                          fontWeight = FontWeight.Bold,
                          fontSize = 12.sp,
                          color = ChurchDeepBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                          modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                          horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                          departments.forEach { dept ->
                            val deptAdmin = allUsers.firstOrNull {
                              it.role == "DeptAdmin" && it.departmentId == dept.id
                            }
                            val isSelected = selectedDeptId == dept.id
                            FilterChip(
                              selected = isSelected,
                              onClick = {
                                selectedDeptId = dept.id
                                if (deptAdmin != null) {
                                  emailInput = deptAdmin.email
                                }
                              },
                              label = {
                                Text(
                                  text = "${dept.code} • ${dept.nameAm}",
                                  fontSize = 11.sp,
                                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                              }
                            )
                          }
                        }
                        if (activeRoleUser != null) {
                          Spacer(modifier = Modifier.height(4.dp))
                          Text(
                            text = "Admin: ${activeRoleUser.fullName} (${activeRoleUser.email}) • " +
                              if (activeRoleUser.isApproved) "Approved ✅" else "Pending Approval ⏳",
                            fontSize = 11.sp,
                            color = if (activeRoleUser.isApproved) ChurchGreen else ChurchDarkGold,
                            fontWeight = FontWeight.SemiBold
                          )
                        }
                      }

                      else -> {
                        val studentUsers = allUsers.filter { it.role == "Member" }
                        Text(
                          text = "Student & Member Portal / የተማሪዎችና አባላት መግቢያ",
                          fontWeight = FontWeight.Bold,
                          fontSize = 12.sp,
                          color = ChurchDeepBlue
                        )
                        Text(
                          text = "Access grades, attendance, monthly contributions, hymns, Bible stories & quizzes.",
                          fontSize = 10.sp,
                          color = ChurchTextSecondary
                        )
                        if (studentUsers.isNotEmpty()) {
                          Spacer(modifier = Modifier.height(6.dp))
                          Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                          ) {
                            studentUsers.forEach { stu ->
                              FilterChip(
                                selected = emailInput.equals(stu.email, ignoreCase = true),
                                onClick = { emailInput = stu.email },
                                label = { Text("${stu.fullName} (${stu.email})", fontSize = 11.sp) }
                              )
                            }
                          }
                        }
                      }
                    }

                    // One-Tap Quick Portal Entry for Selected Role + Edit Account Button
                    if (activeRoleUser != null) {
                      Spacer(modifier = Modifier.height(8.dp))
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                      ) {
                        Button(
                          onClick = {
                            if (activeRoleUser.role == "DeptAdmin" && !activeRoleUser.isApproved) {
                              showPendingApprovalDialog = activeRoleUser.fullName
                            } else {
                              viewModel.loginAs(activeRoleUser)
                            }
                          },
                          colors = ButtonDefaults.buttonColors(containerColor = ChurchGold),
                          shape = RoundedCornerShape(8.dp),
                          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                          modifier = Modifier
                            .weight(1f)
                            .testTag("quick_enter_portal_btn")
                        ) {
                          Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = ChurchDarkBlue,
                            modifier = Modifier.size(16.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = "Quick Enter ${selectedRoleTab} Portal / ቀጥታ ግባ",
                            color = ChurchDarkBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                        }

                        OutlinedButton(
                          onClick = { showConfigureAccountDialog = true },
                          shape = RoundedCornerShape(8.dp),
                          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                          modifier = Modifier.testTag("configure_firebase_account_button")
                        ) {
                          Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = ChurchDeepBlue,
                            modifier = Modifier.size(15.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = "Edit Account",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChurchDeepBlue
                          )
                        }
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                  value = emailInput,
                  onValueChange = {
                    emailInput = it
                    errorMessage = ""
                  },
                  label = { Text(AppStrings.email) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input"),
                  singleLine = true,
                  leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ChurchDeepBlue) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                  value = passwordInput,
                  onValueChange = {
                    passwordInput = it
                    errorMessage = ""
                  },
                  label = { Text(AppStrings.password) },
                  visualTransformation = PasswordVisualTransformation(),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                  singleLine = true,
                  leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ChurchDeepBlue) }
                )

                if (errorMessage.isNotEmpty()) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChurchRed.copy(alpha = 0.1f),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(top = 10.dp)
                  ) {
                    Row(
                      modifier = Modifier.padding(8.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ChurchRed, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = errorMessage,
                        color = ChurchRed,
                        style = MaterialTheme.typography.bodySmall
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Login & Admin Portal Verification Buttons
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Button(
                    onClick = {
                      viewModel.login(emailInput.trim(), passwordInput) { success, msg ->
                        if (!success) {
                          errorMessage = msg
                        }
                      }
                    },
                    modifier = Modifier
                      .weight(1f)
                      .testTag("login_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.login, fontWeight = FontWeight.Bold)
                  }

                  OutlinedButton(
                    onClick = {
                      val targetEmail = emailInput.trim()
                      val user = allUsers.find { it.email.equals(targetEmail, ignoreCase = true) }
                      if (user == null) {
                        errorMessage = "Please enter a registered Manager or Department Admin email."
                      } else if (user.role != "Manager" && user.role != "DeptAdmin") {
                        errorMessage = "The email entered is not an administrative account."
                      } else if (user.role == "DeptAdmin" && !user.isApproved) {
                        showPendingApprovalDialog = user.fullName
                      } else {
                        viewModel.login(targetEmail, passwordInput) { success, msg ->
                          if (!success) errorMessage = msg
                        }
                      }
                    },
                    modifier = Modifier
                      .weight(1f)
                      .testTag("admin_portal_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChurchDarkGold),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ChurchDarkGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Admin / አስተዳዳሪ", fontWeight = FontWeight.Bold)
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Firebase Auth + Android Credential Manager Google Sign-In Button
                OutlinedButton(
                  onClick = {
                    viewModel.loginWithGoogleCredentialManager(context) { success, msg ->
                      if (!success) {
                        errorMessage = msg
                      }
                    }
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_credential_manager_button"),
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, ChurchDeepBlue.copy(alpha = 0.45f))
                ) {
                  Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = ChurchDeepBlue,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Sign in with Google (Firebase Credential Manager)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChurchDeepBlue
                  )
                }
              } else {
                // =========================================================================
                // MODE 1: INLINE REGISTER FORM FOR NEW MEMBER, STUDENT, ADMIN OR MANAGER
                // =========================================================================
                Text(
                  text = "Register New Account / አዲስ አባልና ተጠቃሚ መመዝገቢያ",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = ChurchDeepBlue
                )
                Text(
                  text = "Creates your Sunday School Member ID and Firebase/Room login account immediately.",
                  fontSize = 11.sp,
                  color = ChurchTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Account Role Choice
                Text("Select Account Role / ሚና ይምረጡ:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  listOf(
                    "Member" to "Student / Member (ተማሪ/አባል)",
                    "DeptAdmin" to "Dept Admin (የክፍል ኃላፊ)",
                    "Manager" to "Manager (ሥራ አስኪያጅ)"
                  ).forEach { (roleKey, label) ->
                    FilterChip(
                      selected = regRole == roleKey,
                      onClick = { regRole = roleKey },
                      label = { Text(label, fontSize = 10.sp) }
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                  value = regFullName,
                  onValueChange = { regFullName = it; regErrorMessage = "" },
                  label = { Text("Full Name / ሙሉ ስም *") },
                  singleLine = true,
                  leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ChurchDeepBlue) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_inline_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                  value = regPhone,
                  onValueChange = { regPhone = it; regErrorMessage = "" },
                  label = { Text("Phone Number / ስልክ ቁጥር *") },
                  singleLine = true,
                  leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ChurchDeepBlue) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_inline_phone")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                  value = regEmail,
                  onValueChange = { regEmail = it; regErrorMessage = "" },
                  label = { Text("Email Address / ኢሜይል *") },
                  singleLine = true,
                  leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ChurchDeepBlue) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_inline_email")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                  value = regPassword,
                  onValueChange = { regPassword = it; regErrorMessage = "" },
                  label = { Text("Create Password / የይለፍ ቃል ይፍጠሩ *") },
                  visualTransformation = PasswordVisualTransformation(),
                  singleLine = true,
                  leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ChurchDeepBlue) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_inline_password")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Selector (Student / StudentWorker / Worker)
                Text("Contribution Category / የአባልነት ደረጃ:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  listOf(
                    "Student" to "Student (ተማሪ)",
                    "StudentWorker" to "Student-Worker",
                    "Worker" to "Worker (ሰራተኛ)"
                  ).forEach { (catKey, catLabel) ->
                    FilterChip(
                      selected = regCategory == catKey,
                      onClick = { regCategory = catKey },
                      label = { Text(catLabel, fontSize = 10.sp) }
                    )
                  }
                }

                if (regRole != "Manager" && departments.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Text("Service Department / የአገልግሎት ክፍል:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    departments.forEach { dept ->
                      FilterChip(
                        selected = regDeptId == dept.id,
                        onClick = { regDeptId = dept.id },
                        label = { Text("${dept.code} - ${dept.nameAm}", fontSize = 10.sp) }
                      )
                    }
                  }
                }

                if (regErrorMessage.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = regErrorMessage,
                    color = ChurchRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                  onClick = {
                    viewModel.registerNewUserAndMember(
                      fullName = regFullName,
                      phone = regPhone,
                      email = regEmail,
                      plainPassword = regPassword,
                      role = regRole,
                      departmentId = regDeptId,
                      category = regCategory
                    ) { success, msg, _ ->
                      if (!success) {
                        regErrorMessage = msg
                      }
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inline_register_submit_button")
                ) {
                  Icon(Icons.Default.HowToReg, contentDescription = null, tint = ChurchGold)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Register & Enter Portal / ተመዝገብ እና ግባ",
                    fontWeight = FontWeight.Bold
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                  onClick = onNavigateToRegister,
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_nav_button")
                ) {
                  Icon(Icons.Default.OpenInNew, contentDescription = null, tint = ChurchDeepBlue, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Open Full Member Registration Form / ሙሉ የምዝገባ ቅጽ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChurchDeepBlue
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Cloud Sync & Post-Login Security Info Strip
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = ChurchBlueBg,
                border = BorderStroke(1.dp, ChurchDeepBlue.copy(alpha = 0.18f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = ChurchDeepBlue,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Data Center, Firestore Cloud Sync & 8-Point Security Audit for all 12 departments are enabled inside the Manager & Department Admin Dashboards after login (${cloudSyncState.statusMessage}).",
                    fontSize = 10.sp,
                    color = ChurchTextSecondary,
                    lineHeight = 14.sp
                  )
                }
              }
            }
          }

          if (showConfigureAccountDialog) {
            var selectedExistingId by remember { mutableStateOf<Long?>(null) }
            var fullName by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("+2519") }
            var role by remember { mutableStateOf(selectedRoleTab) }
            var deptId by remember { mutableStateOf(selectedDeptId) }
            var setupError by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
              val preset = allUsers.firstOrNull { it.email.equals(emailInput.trim(), ignoreCase = true) }
                ?: allUsers.firstOrNull { it.role == selectedRoleTab }
              if (preset != null) {
                selectedExistingId = preset.id
                fullName = preset.fullName
                email = preset.email
                phone = preset.phone
                role = preset.role
                deptId = preset.departmentId ?: 1L
              }
            }

            AlertDialog(
              onDismissRequest = { showConfigureAccountDialog = false },
              title = {
                Text(
                  text = "Firebase & Room Account Setup (የመለያ ማስተካከያ)",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
              },
              text = {
                LazyColumn(
                  modifier = Modifier.heightIn(max = 420.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  item {
                    Text(
                      text = "Customize login email, password, or department role for Manager, Department Admin, or Student/Member:",
                      fontSize = 11.sp,
                      color = ChurchTextSecondary
                    )
                  }
                  item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      listOf("Manager", "DeptAdmin", "Member").forEach { r ->
                        FilterChip(
                          selected = role == r,
                          onClick = {
                            role = r
                            val preset = allUsers.firstOrNull { it.role == r }
                            if (preset != null) {
                              selectedExistingId = preset.id
                              fullName = preset.fullName
                              email = preset.email
                              phone = preset.phone
                              deptId = preset.departmentId ?: 1L
                            }
                          },
                          label = { Text(r, fontSize = 11.sp) }
                        )
                      }
                    }
                  }
                  item {
                    OutlinedTextField(
                      value = fullName,
                      onValueChange = { fullName = it; setupError = "" },
                      label = { Text("Full Name / ሙሉ ስም") },
                      singleLine = true,
                      modifier = Modifier.fillMaxWidth().testTag("setup_account_name")
                    )
                  }
                  item {
                    OutlinedTextField(
                      value = email,
                      onValueChange = { email = it; setupError = "" },
                      label = { Text("Custom Login Email / ኢሜይል") },
                      singleLine = true,
                      modifier = Modifier.fillMaxWidth().testTag("setup_account_email")
                    )
                  }
                  item {
                    OutlinedTextField(
                      value = newPassword,
                      onValueChange = { newPassword = it; setupError = "" },
                      label = { Text("Set Custom Password / አዲስ የይለፍ ቃል") },
                      singleLine = true,
                      modifier = Modifier.fillMaxWidth().testTag("setup_account_password")
                    )
                  }
                  item {
                    OutlinedTextField(
                      value = phone,
                      onValueChange = { phone = it },
                      label = { Text("Phone Number / ስልክ") },
                      singleLine = true,
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                  if (role == "DeptAdmin" && departments.isNotEmpty()) {
                    item {
                      Text("Select Department / ክፍል:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                      departments.forEach { d ->
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deptId = d.id }
                        ) {
                          RadioButton(selected = deptId == d.id, onClick = { deptId = d.id })
                          Text("${d.code} - ${d.nameEn}", fontSize = 11.sp)
                        }
                      }
                    }
                  }
                  if (setupError.isNotEmpty()) {
                    item {
                      Text(setupError, color = ChurchRed, fontSize = 11.sp)
                    }
                  }
                }
              },
              confirmButton = {
                Button(
                  onClick = {
                    if (email.isBlank() || fullName.isBlank() || newPassword.isBlank()) {
                      setupError = "Please enter Full Name, Email, and your custom Password."
                      return@Button
                    }
                    val existing = allUsers.find {
                      it.id == selectedExistingId || it.email.equals(email.trim(), ignoreCase = true)
                    }
                    viewModel.saveManagedUserAccount(
                      existingUser = existing,
                      email = email.trim(),
                      plainPassword = newPassword,
                      fullName = fullName.trim(),
                      phone = phone.trim(),
                      role = role,
                      departmentId = if (role == "Manager") null else deptId,
                      isApproved = true,
                      isActive = true
                    ) { success, err ->
                      if (success) {
                        emailInput = email.trim()
                        passwordInput = newPassword
                        showConfigureAccountDialog = false
                        viewModel.login(email.trim(), newPassword) { _, _ -> }
                      } else {
                        setupError = err
                      }
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
                  modifier = Modifier.testTag("confirm_setup_account_btn")
                ) {
                  Text("Save & Sign In")
                }
              },
              dismissButton = {
                TextButton(onClick = { showConfigureAccountDialog = false }) {
                  Text("Cancel")
                }
              }
            )
          }

          if (showPendingApprovalDialog != null) {
            AlertDialog(
              onDismissRequest = { showPendingApprovalDialog = null },
              icon = { Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = ChurchAmber, modifier = Modifier.size(36.dp)) },
              title = {
                Text(
                  "በማኔጀር ማረጋገጫ በመጠባበቅ ላይ / Awaiting Manager Approval",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  textAlign = TextAlign.Center
                )
              },
              text = {
                Text(
                  text = "Department Admin account (${showPendingApprovalDialog}) requires Executive Manager approval in the Manager Dashboard -> Users & Credentials tab before access is granted.",
                  style = MaterialTheme.typography.bodySmall,
                  color = ChurchTextPrimary
                )
              },
              confirmButton = {
                TextButton(onClick = { showPendingApprovalDialog = null }) {
                  Text("Close")
                }
              }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun RoleSelectChip(
  titleEn: String,
  titleAm: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  selected: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (selected) ChurchDeepBlue else ChurchIvory,
    border = BorderStroke(1.dp, if (selected) ChurchGold else ChurchBorder),
    modifier = Modifier
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) ChurchGold else ChurchDeepBlue,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(
          text = titleEn,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (selected) Color.White else ChurchDeepBlue
        )
        Text(
          text = titleAm,
          fontSize = 9.sp,
          color = if (selected) ChurchLightGold else ChurchTextMuted
        )
      }
    }
  }
}
