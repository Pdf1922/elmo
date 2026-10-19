package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.components.ChurchLogo3D
import com.example.ui.components.ChurchTopAppBar
import com.example.ui.theme.*

@Composable
fun AwaitingApprovalScreen(
  user: User,
  onLogout: () -> Unit
) {
  Scaffold(
    topBar = {
      ChurchTopAppBar(
        userName = user.fullName,
        userRole = "Dept Admin (Pending)",
        onLogout = onLogout,
        title = "Meserete Hiwot Sunday School",
        subtitle = "Admin Verification Workflow"
      )
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(ChurchIvory),
      contentAlignment = Alignment.Center
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          ChurchLogo3D(size = 72.dp, continuousRotation = true)

          Surface(
            shape = CircleShape,
            color = ChurchAmber.copy(alpha = 0.15f),
            modifier = Modifier.size(54.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = ChurchAmber,
                modifier = Modifier.size(30.dp)
              )
            }
          }

          Text(
            text = "ማረጋገጫ በመጠባበቅ ላይ",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = ChurchDeepBlue,
            textAlign = TextAlign.Center
          )

          Text(
            text = "Waiting for Manager Approval",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = ChurchDarkGold,
            textAlign = TextAlign.Center
          )

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = ChurchPaleGold.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Applicant Name: ${user.fullName}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ChurchTextPrimary
              )
              Text(
                text = "Email: ${user.email}",
                fontSize = 12.sp,
                color = ChurchTextSecondary
              )
              Text(
                text = "Role: Department Administrator (DeptAdmin)",
                fontSize = 12.sp,
                color = ChurchDeepBlue,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Status: ⏳ Pending Executive Approval (2019 E.C.)",
                fontSize = 12.sp,
                color = ChurchAmber,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Text(
            text = "የክፍል አስተዳዳሪነት መለያዎ በዋና ሥራ አስኪያጅ (Dn. Yohannes Girma / Dn. Tewodros Assefa) እስኪረጋገጥ ድረስ ወደ ክፍል አስተዳደር ገጽ መግባት አይቻልም። ሥራ አስኪያጁ ባረጋገጠልዎት ቅጽበት ሙሉ ሥልጣኑ ይከፈትልዎታል።",
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
          )

          Button(
            onClick = onLogout,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("logout_pending_approval_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Switch Account / ተመለስ", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
