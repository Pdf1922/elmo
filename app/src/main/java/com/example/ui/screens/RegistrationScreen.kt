package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ChurchTopAppBar
import com.example.ui.language.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
  viewModel: SundaySchoolViewModel,
  onNavigateBack: () -> Unit
) {
  var fullName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("+2519") }
  var email by remember { mutableStateOf("") }
  var gender by remember { mutableStateOf("Male") }
  var category by remember { mutableStateOf("Worker") }
  var classGrade by remember { mutableStateOf("Grade 1 - Senbet Nigest") }
  var confessor by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("Bole Arabsa Site 2") }
  var password by remember { mutableStateOf("") }
  var selectedRole by remember { mutableStateOf("Student") }
  var isBaptized by remember { mutableStateOf(true) }
  var registrationSubmitted by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Member Registration / የአባልነት ምዝገባ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ChurchDeepBlue)
      )
    }
  ) { padding ->
    if (registrationSubmitted) {
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
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Success",
              tint = ChurchGreen,
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Registration Successful!",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = ChurchDeepBlue
            )
            Text(
              text = "ምዝገባዎ በተሳካ ሁኔታ ተጠናቋል። ወደ ሰንበት ት/ቤታችን እንኳን ደህና መጡ!",
              style = MaterialTheme.typography.bodyMedium,
              color = ChurchTextSecondary,
              modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
              text = "Your membership has been routed to the HR Department and Management for welcome onboarding.",
              style = MaterialTheme.typography.bodySmall,
              color = ChurchTextMuted
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
              onClick = onNavigateBack,
              colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Return to Home / ወደ መነሻ")
            }
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .background(ChurchIvory)
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ChurchPaleGold),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "Meserete Hiwot Sunday School Registration",
                fontWeight = FontWeight.Bold,
                color = ChurchDarkGold
              )
              Text(
                text = "Debre Hayil St. Gabriel and St. Arsema Church, Bole Arabsa",
                style = MaterialTheme.typography.bodySmall,
                color = ChurchTextPrimary
              )
            }
          }
        }

        item {
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Christian & Civil Name / ሙሉ ስም") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("register_full_name")
          )
        }

        item {
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(AppStrings.phone) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("register_phone")
          )
        }

        item {
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(AppStrings.email) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("register_email")
          )
        }

        item {
          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Account Password / የይለፍ ቃል") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("register_password")
          )
        }

        item {
          Text("Role Assignment / የተጠቃሚ ሚና:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Student" to "Student / ተማሪ", "Parent" to "Parent / ወላጅ", "Teacher" to "Teacher / መምህር", "Member" to "Member / አባል").forEach { (roleKey, roleLabel) ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selectedRole == roleKey) ChurchDeepBlue else Color.White,
                modifier = Modifier
                  .weight(1f)
                  .clickable { selectedRole = roleKey }
              ) {
                Text(
                  text = roleLabel,
                  color = if (selectedRole == roleKey) Color.White else ChurchTextPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(8.dp)
                )
              }
            }
          }
        }

        item {
          Text("Member Category / የአባልነት ዓይነት:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Worker", "StudentWorker", "NonWorker").forEach { cat ->
              val label = when (cat) {
                "Worker" -> "Worker (100 ETB)"
                "StudentWorker" -> "Student Worker (50 ETB)"
                else -> "Non-Worker (20 ETB)"
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (category == cat) ChurchDeepBlue else Color.White,
                modifier = Modifier
                  .weight(1f)
                  .clickable { category = cat }
              ) {
                Text(
                  text = label,
                  color = if (category == cat) Color.White else ChurchTextPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(8.dp)
                )
              }
            }
          }
        }

        item {
          OutlinedTextField(
            value = classGrade,
            onValueChange = { classGrade = it },
            label = { Text("Sunday School Grade / የትምህርት ክፍል") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = confessor,
            onValueChange = { confessor = it },
            label = { Text("Confessor Father / የንስሐ አባት ስም") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("register_confessor")
          )
        }

        item {
          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Residential Address / የመኖሪያ አድራሻ") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { isBaptized = !isBaptized }
          ) {
            Checkbox(checked = isBaptized, onCheckedChange = { isBaptized = it })
            Spacer(modifier = Modifier.width(6.dp))
            Text("Baptized in Ethiopian Orthodox Tewahedo Church (በኦርቶዶክስ ተዋሕዶ የተጠመቀ/ች)", fontSize = 12.sp)
          }
        }

        item {
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = {
              if (fullName.isNotBlank()) {
                val effectiveEmail = if (email.isNotBlank()) email.trim() else "${fullName.take(5).lowercase().replace(" ", "")}${(10..99).random()}@meseretehiwot.org"
                val effectivePassword = if (password.isNotBlank()) password else "Student123#"
                viewModel.registerNewUserAndMember(
                  fullName = fullName,
                  phone = phone,
                  email = effectiveEmail,
                  plainPassword = effectivePassword,
                  role = selectedRole,
                  departmentId = 1L,
                  gender = gender,
                  category = category,
                  classGrade = classGrade,
                  confessorFather = if (confessor.isNotBlank()) confessor else "Kesis Melaku",
                  address = address,
                  autoLogin = false
                ) { success, _, _ ->
                  if (success) {
                    registrationSubmitted = true
                  }
                }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("submit_registration_button")
          ) {
            Text(AppStrings.register, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
        }
      }
    }
  }
}
