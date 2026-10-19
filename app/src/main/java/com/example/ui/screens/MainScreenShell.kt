package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.User
import com.example.ui.language.AppLanguage
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel
import com.example.util.LocalizationManager

enum class ShellDestination(
  val labelRes: Int,
  val icon: ImageVector,
  val testTag: String
) {
  DASHBOARD(R.string.nav_dashboard, Icons.Default.Dashboard, "nav_item_dashboard"),
  ATTENDANCE(R.string.nav_attendance, Icons.Default.EventAvailable, "nav_item_attendance"),
  GRADES(R.string.nav_grades, Icons.Default.School, "nav_item_grades"),
  LESSON_NOTES(R.string.nav_lesson_notes, Icons.Default.MenuBook, "nav_item_lesson_notes"),
  MEMBERS(R.string.nav_members, Icons.Default.People, "nav_item_members"),
  DATA_CENTER(R.string.nav_data_center, Icons.Default.Storage, "nav_item_data_center"),
  BIBLE_STUDY(R.string.nav_bible_study, Icons.Default.AutoStories, "nav_item_bible"),
  COUNSELOR(R.string.nav_counselor, Icons.Default.Psychology, "nav_item_counselor")
}

/**
 * MainScreenShell provides an adaptive Navigation Rail (for wide/tablet screens) or
 * Bottom Navigation Bar (for compact/mobile screens) that dynamically shows/hides
 * menu items based on the user's role retrieved from Firebase Auth.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenShell(
  viewModel: SundaySchoolViewModel,
  user: User,
  onLogout: () -> Unit
) {
  val context = LocalContext.current
  val currentLanguage = LocalizationManager.currentLanguage

  // Determine user category from Firebase Auth / Room profile
  val roleLower = user.role.lowercase()
  val isAdmin = roleLower in listOf("manager", "super admin", "admin", "deptadmin")
  val isTeacher = !isAdmin && (roleLower.contains("teacher") || roleLower.contains("instructor"))
  val isStudent = !isAdmin && !isTeacher // Default student/member/parent

  // Define role-specific navigation menu items
  // Teachers and Admins have ATTENDANCE (marking Sunday session attendance), hidden from Students
  // All roles have LESSON_NOTES (Teachers post weekly lesson notes, students view list)
  val allowedDestinations = remember(user.role) {
    when {
      isAdmin -> listOf(
        ShellDestination.DASHBOARD,
        ShellDestination.ATTENDANCE,
        ShellDestination.LESSON_NOTES,
        ShellDestination.MEMBERS,
        ShellDestination.DATA_CENTER
      )
      isTeacher -> listOf(
        ShellDestination.DASHBOARD,
        ShellDestination.ATTENDANCE,
        ShellDestination.GRADES,
        ShellDestination.LESSON_NOTES,
        ShellDestination.BIBLE_STUDY
      )
      else -> listOf( // Student & Parent
        ShellDestination.DASHBOARD,
        ShellDestination.GRADES,
        ShellDestination.LESSON_NOTES,
        ShellDestination.BIBLE_STUDY,
        ShellDestination.COUNSELOR
      )
    }
  }

  var selectedDestination by remember { mutableStateOf(allowedDestinations.first()) }

  // Ensure current selection stays valid when role changes
  LaunchedEffect(allowedDestinations) {
    if (selectedDestination !in allowedDestinations) {
      selectedDestination = allowedDestinations.first()
    }
  }

  val roleDisplay = when {
    isAdmin -> stringResource(R.string.role_admin)
    isTeacher -> stringResource(R.string.role_teacher)
    else -> stringResource(R.string.role_student)
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isWideScreen = maxWidth >= 600.dp

    if (isWideScreen) {
      // WIDE LAYOUT: Navigation Rail on the Left + Content on the Right
      Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
          modifier = Modifier
            .fillMaxHeight()
            .testTag("shell_navigation_rail"),
          containerColor = ChurchDeepBlue,
          contentColor = Color.White,
          header = {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(vertical = 12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(ChurchGold),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Church,
                  contentDescription = null,
                  tint = ChurchDarkBlue,
                  modifier = Modifier.size(26.dp)
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = user.fullName.take(12),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
              Surface(
                color = ChurchGold.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text(
                  text = roleDisplay,
                  color = ChurchLightGold,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        ) {
          Spacer(modifier = Modifier.height(8.dp))
          allowedDestinations.forEach { destination ->
            val isSelected = destination == selectedDestination
            NavigationRailItem(
              selected = isSelected,
              onClick = { selectedDestination = destination },
              icon = {
                Icon(
                  imageVector = destination.icon,
                  contentDescription = stringResource(destination.labelRes)
                )
              },
              label = {
                Text(
                  text = stringResource(destination.labelRes),
                  fontSize = 10.sp,
                  maxLines = 1
                )
              },
              colors = NavigationRailItemDefaults.colors(
                selectedIconColor = ChurchDarkBlue,
                selectedTextColor = ChurchLightGold,
                indicatorColor = ChurchLightGold,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
              ),
              modifier = Modifier.testTag(destination.testTag)
            )
          }

          Spacer(modifier = Modifier.weight(1f))

          // Language Switcher in Rail Footer
          IconButton(
            onClick = { LocalizationManager.toggleLanguage(context) },
            modifier = Modifier.testTag("rail_language_toggle_button")
          ) {
            Icon(
              imageVector = Icons.Default.Translate,
              contentDescription = stringResource(R.string.switch_language),
              tint = ChurchLightGold
            )
          }

          // Logout Button in Rail Footer
          IconButton(
            onClick = onLogout,
            modifier = Modifier.testTag("rail_logout_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Logout,
              contentDescription = stringResource(R.string.logout),
              tint = Color.White.copy(alpha = 0.8f)
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
        }

        // Main Content Area (Wide Screen)
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        ) {
          ShellContentHost(
            destination = selectedDestination,
            viewModel = viewModel,
            user = user,
            isAdmin = isAdmin,
            isTeacher = isTeacher,
            isStudent = isStudent,
            onLogout = onLogout
          )
        }
      }
    } else {
      // COMPACT LAYOUT: Top App Bar + Content + Bottom Navigation Bar
      Scaffold(
        topBar = {
          TopAppBar(
            title = {
              Column {
                Text(
                  text = stringResource(R.string.app_name),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = "${user.fullName} • $roleDisplay",
                  fontSize = 11.sp,
                  color = ChurchLightGold
                )
              }
            },
            actions = {
              // Quick Language Switcher Pill (Amharic <-> English)
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = ChurchGold.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ChurchGold),
                modifier = Modifier
                  .clickable { LocalizationManager.toggleLanguage(context) }
                  .testTag("topbar_language_toggle_pill")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    tint = ChurchLightGold,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (currentLanguage == AppLanguage.AMHARIC) "EN" else "አማ",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              IconButton(
                onClick = onLogout,
                modifier = Modifier.testTag("topbar_logout_button")
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Logout,
                  contentDescription = stringResource(R.string.logout),
                  tint = Color.White
                )
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = ChurchDeepBlue
            )
          )
        },
        bottomBar = {
          NavigationBar(
            containerColor = ChurchDeepBlue,
            contentColor = Color.White,
            modifier = Modifier.testTag("shell_bottom_navigation_bar")
          ) {
            allowedDestinations.forEach { destination ->
              val isSelected = destination == selectedDestination
              NavigationBarItem(
                selected = isSelected,
                onClick = { selectedDestination = destination },
                icon = {
                  Icon(
                    imageVector = destination.icon,
                    contentDescription = stringResource(destination.labelRes)
                  )
                },
                label = {
                  Text(
                    text = stringResource(destination.labelRes),
                    fontSize = 10.sp,
                    maxLines = 1
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = ChurchDarkBlue,
                  selectedTextColor = ChurchLightGold,
                  indicatorColor = ChurchLightGold,
                  unselectedIconColor = Color.White.copy(alpha = 0.65f),
                  unselectedTextColor = Color.White.copy(alpha = 0.65f)
                ),
                modifier = Modifier.testTag(destination.testTag)
              )
            }
          }
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          ShellContentHost(
            destination = selectedDestination,
            viewModel = viewModel,
            user = user,
            isAdmin = isAdmin,
            isTeacher = isTeacher,
            isStudent = isStudent,
            onLogout = onLogout
          )
        }
      }
    }
  }
}

@Composable
fun ShellContentHost(
  destination: ShellDestination,
  viewModel: SundaySchoolViewModel,
  user: User,
  isAdmin: Boolean,
  isTeacher: Boolean,
  isStudent: Boolean,
  onLogout: () -> Unit
) {
  var isViewingAiCounselor by remember { mutableStateOf(false) }
  var isViewingBibleStory by remember { mutableStateOf(false) }
  var isViewingSongDepartment by remember { mutableStateOf(false) }
  var isViewingDataCenter by remember { mutableStateOf(false) }

  AnimatedContent(
    targetState = destination,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "ShellContentTransition"
  ) { target ->
    when (target) {
      ShellDestination.DASHBOARD -> {
        when {
          isAdmin && user.role == "Manager" -> {
            ManagerDashboardScreen(
              viewModel = viewModel,
              onNavigateToAiCounselor = { isViewingAiCounselor = true },
              onNavigateToBibleStory = { isViewingBibleStory = true },
              onNavigateToSongAttendance = { isViewingSongDepartment = true },
              onNavigateToDataCenter = { isViewingDataCenter = true }
            )
          }
          isAdmin && user.role == "DeptAdmin" -> {
            DepartmentAdminDashboardScreen(
              viewModel = viewModel,
              onNavigateToSongAttendance = { isViewingSongDepartment = true },
              onNavigateToDataCenter = { isViewingDataCenter = true }
            )
          }
          else -> {
            MemberPortalScreen(
              viewModel = viewModel,
              onNavigateToAiCounselor = { isViewingAiCounselor = true },
              onNavigateToBibleStory = { isViewingBibleStory = true }
            )
          }
        }
      }
      ShellDestination.GRADES -> {
        // Teacher Grade Portal or Student Report Cards
        if (isTeacher || isAdmin) {
          TeacherGradePortalScreen(viewModel = viewModel, teacherUser = user)
        } else {
          StudentReportCardScreen(viewModel = viewModel, studentUser = user)
        }
      }
      ShellDestination.ATTENDANCE -> {
        TeacherAttendanceScreen(
          viewModel = viewModel,
          currentUser = user,
          onNavigateBack = {}
        )
      }
      ShellDestination.LESSON_NOTES -> {
        LessonNotesScreen(
          viewModel = viewModel,
          currentUser = user,
          onNavigateBack = {}
        )
      }
      ShellDestination.MEMBERS -> {
        // Admin user and member management
        DepartmentAdminDashboardScreen(
          viewModel = viewModel,
          onNavigateToSongAttendance = { isViewingSongDepartment = true },
          onNavigateToDataCenter = { isViewingDataCenter = true }
        )
      }
      ShellDestination.DATA_CENTER -> {
        SecurityAndDataCenterScreen(
          viewModel = viewModel,
          onNavigateBack = {}
        )
      }
      ShellDestination.BIBLE_STUDY -> {
        BibleStoryScreen(
          viewModel = viewModel,
          onNavigateBack = {}
        )
      }
      ShellDestination.COUNSELOR -> {
        ChurchCounselorChatScreen(
          onNavigateBack = {}
        )
      }
    }
  }

  // Handle overlay full-screen sub-screens if triggered from dashboard buttons
  if (isViewingDataCenter) {
    SecurityAndDataCenterScreen(viewModel = viewModel, onNavigateBack = { isViewingDataCenter = false })
  }
  if (isViewingSongDepartment) {
    SongDepartmentAttendanceScreen(viewModel = viewModel, onNavigateBack = { isViewingSongDepartment = false })
  }
  if (isViewingAiCounselor) {
    ChurchCounselorChatScreen(onNavigateBack = { isViewingAiCounselor = false })
  }
  if (isViewingBibleStory) {
    BibleStoryScreen(viewModel = viewModel, onNavigateBack = { isViewingBibleStory = false })
  }
}
