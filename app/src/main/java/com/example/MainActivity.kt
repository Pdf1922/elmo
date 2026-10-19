package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.ChurchIvory
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SundaySchoolViewModel
import com.example.util.BibleReminderManager
import com.example.util.LocalizationManager

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize application-wide language and localization resources
    LocalizationManager.initialize(this)

    // Initialize daily Bible reminders at 6:30 AM and 6:30 PM
    BibleReminderManager.scheduleDailyReminders(this)

    setContent {
      MyApplicationTheme {
        // Notification permission request for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          val context = LocalContext.current
          val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
          ) {}

          LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
              ) != PackageManager.PERMISSION_GRANTED
            ) {
              permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
          }
        }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = ChurchIvory
        ) {
          SundaySchoolApp()
        }
      }
    }
  }
}

@Composable
fun SundaySchoolApp(viewModel: SundaySchoolViewModel = viewModel()) {
  val currentUser by viewModel.currentUser.collectAsState()
  var isRegistering by remember { mutableStateOf(false) }
  var isViewingAiCounselor by remember { mutableStateOf(false) }
  var isViewingBibleStory by remember { mutableStateOf(false) }

  when {
    isRegistering -> {
      RegistrationScreen(
        viewModel = viewModel,
        onNavigateBack = { isRegistering = false }
      )
    }
    currentUser == null -> {
      LandingScreen(
        viewModel = viewModel,
        onNavigateToRegister = { isRegistering = true },
        onNavigateToAiCounselor = { isViewingAiCounselor = true },
        onNavigateToBibleStory = { isViewingBibleStory = true }
      )
    }
    currentUser?.isApproved == false -> {
      AwaitingApprovalScreen(
        user = currentUser!!,
        onLogout = { viewModel.logout() }
      )
    }
    else -> {
      // Main screen shell with role-based Navigation Rail or Bottom Bar
      MainScreenShell(
        viewModel = viewModel,
        user = currentUser!!,
        onLogout = { viewModel.logout() }
      )
    }
  }

  // Overlay screens if accessed prior to sign-in from the landing screen
  if (currentUser == null && isViewingAiCounselor) {
    ChurchCounselorChatScreen(onNavigateBack = { isViewingAiCounselor = false })
  }
  if (currentUser == null && isViewingBibleStory) {
    BibleStoryScreen(viewModel = viewModel, onNavigateBack = { isViewingBibleStory = false })
  }
}
