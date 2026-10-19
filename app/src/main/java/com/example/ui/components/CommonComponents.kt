package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.language.AppLanguage
import com.example.ui.language.AppStrings
import com.example.ui.language.LanguageState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChurchTopAppBar(
  userName: String? = null,
  userRole: String? = null,
  onLogout: (() -> Unit)? = null,
  onBackClick: (() -> Unit)? = null,
  title: String = AppStrings.appName,
  subtitle: String = AppStrings.churchName,
  actions: @Composable (() -> Unit)? = null,
  extraAction: @Composable (() -> Unit)? = null
) {
  TopAppBar(
    navigationIcon = {
      if (onBackClick != null) {
        IconButton(onClick = onBackClick) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }
      }
    },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        ChurchLogo3D(
          size = 40.dp,
          continuousRotation = true
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = ChurchLightGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 11.sp
          )
        }
      }
    },
    actions = {
      if (actions != null) {
        actions()
      }
      if (extraAction != null) {
        extraAction()
      }
      // Ethiopian Calendar badge
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = ChurchMediumBlue,
        modifier = Modifier.padding(end = 6.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = "Calendar",
            tint = ChurchLightGold,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (LanguageState.currentLanguage == AppLanguage.AMHARIC) "መስከረም 2019" else "Meskerem 2019",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Language Switcher Toggle
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ChurchGold,
        modifier = Modifier
          .padding(end = 6.dp)
          .clickable { LanguageState.toggleLanguage() }
          .testTag("language_toggle_button")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Translate,
            contentDescription = "Language Switcher",
            tint = ChurchDarkBlue,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (LanguageState.currentLanguage == AppLanguage.AMHARIC) "EN" else "አማ",
            color = ChurchDarkBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      if (onLogout != null) {
        IconButton(
          onClick = onLogout,
          modifier = Modifier.testTag("logout_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = "Logout",
            tint = Color.White
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = ChurchDeepBlue,
      titleContentColor = Color.White,
      actionIconContentColor = Color.White
    )
  )
}

@Composable
fun KpiCard(
  title: String,
  value: String,
  subtitle: String = "",
  icon: ImageVector,
  iconTint: Color = ChurchGold,
  iconBg: Color = ChurchPaleGold,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.motion3DTilt(maxTiltAngle = 8f),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = ChurchTextMuted,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = value,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = ChurchDeepBlue
        )
        if (subtitle.isNotEmpty()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = ChurchGreen,
            fontWeight = FontWeight.Medium
          )
        }
      }
      Box(
        modifier = Modifier
          .size(44.dp)
          .background(iconBg, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = iconTint,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

@Composable
fun RoleBadge(role: String, departmentName: String? = null) {
  val (label, bg, fg) = when (role) {
    "Manager" -> Triple(AppStrings.roleManager, ChurchPaleGold, ChurchDarkGold)
    "DeptAdmin" -> Triple(
      departmentName ?: AppStrings.roleDeptAdmin,
      Color(0xFFE0E7FF),
      Color(0xFF3730A3)
    )
    else -> Triple(AppStrings.roleMember, Color(0xFFDCFCE7), Color(0xFF166534))
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = bg
  ) {
    Text(
      text = label,
      color = fg,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
    )
  }
}

@Composable
fun StatusBadge(status: String) {
  val (color, bg) = when (status) {
    "Active", "Paid", "Verified", "Approved", "Present", "Resolved" -> Pair(ChurchGreen, Color(0xFFDCFCE7))
    "Pending", "Pending Verification", "InProgress" -> Pair(ChurchAmber, Color(0xFFFEF3C7))
    "Excused" -> Pair(Color(0xFF2563EB), Color(0xFFDBEAFE))
    else -> Pair(ChurchRed, Color(0xFFFEE2E2))
  }

  val localizedText = when (status) {
    "Active" -> AppStrings.active
    "Inactive" -> AppStrings.inactive
    "Distanced" -> AppStrings.distanced
    "Paid" -> AppStrings.verified
    "Unpaid" -> AppStrings.unpaidMembers
    "Pending Verification" -> AppStrings.pendingVerification
    "Verified" -> AppStrings.verified
    "Approved" -> AppStrings.approved
    "Pending" -> AppStrings.pending
    "Present" -> AppStrings.present
    "Absent" -> AppStrings.absent
    "Excused" -> AppStrings.excused
    else -> status
  }

  Surface(
    shape = RoundedCornerShape(6.dp),
    color = bg
  ) {
    Text(
      text = localizedText,
      color = color,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
    )
  }
}
