package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.MemberItem
import com.example.ui.components.ChurchLogo3D
import com.example.ui.components.ChurchTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel

/**
 * Dedicated UI Screen for the Song Department (የመዝሙርና ሥነ ጥበብ ክፍል)
 * Lists choir members and allows toggling live attendance status, managing
 * holiday uniform eligibility, and synchronizing with the Room Database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDepartmentAttendanceScreen(
  viewModel: SundaySchoolViewModel,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current

  // State from ViewModel (Room Database)
  val allMembers by viewModel.members.collectAsState()
  val allAttendance by viewModel.attendance.collectAsState()

  // Selected session date & service type
  var selectedDate by remember { mutableStateOf("2024-09-26") }
  var selectedServiceType by remember { mutableStateOf("Weekly Choir Practice / የሳምንቱ ልምምድ") }

  // Filter & Search states
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("All") } // "All", "Present", "Absent", "Late", "Excused", "Uniform"

  // Dialog for holiday uniform announcement
  var showUniformAnnouncementDialog by remember { mutableStateOf(false) }
  var holidayTitle by remember { mutableStateOf("የበዓለ ቅዱስ ገብርኤል ዝማሬ (St. Gabriel Feast)") }
  var pickupLocation by remember { mutableStateOf("ቅዳሜ ከቀኑ 8:00 ሰዓት ጀምሮ በሰንበት ት/ቤቱ አዳራሽ") }

  // Filter members for Song Department (or all members participating in choir)
  val choirMembers = remember(allMembers) {
    // Include all members or filter specifically those in department 6 (Song) and active choir participants
    val songDept = allMembers.filter { it.departmentId == 6L }
    if (songDept.isNotEmpty()) songDept else allMembers.take(20)
  }

  // Attendance lookup map for current date & service
  val attendanceMap = remember(allAttendance, selectedDate, selectedServiceType) {
    allAttendance
      .filter { it.date == selectedDate && it.serviceType == selectedServiceType }
      .associateBy { it.memberId }
  }

  // Computed metrics
  val totalChoir = choirMembers.size
  val presentCount = choirMembers.count { (attendanceMap[it.memberId]?.status ?: "Absent") == "Present" }
  val absentCount = choirMembers.count { (attendanceMap[it.memberId]?.status ?: "Absent") == "Absent" }
  val lateCount = choirMembers.count { attendanceMap[it.memberId]?.status == "Late" }
  val excusedCount = choirMembers.count { attendanceMap[it.memberId]?.status == "Excused" }
  val uniformApprovedCount = choirMembers.count { it.uniformAccessApproved }

  // Filtered members list
  val filteredMembers = remember(choirMembers, searchQuery, selectedFilter, attendanceMap) {
    choirMembers.filter { member ->
      val matchesSearch = member.fullName.contains(searchQuery, ignoreCase = true) ||
          member.memberId.contains(searchQuery, ignoreCase = true)

      val currentStatus = attendanceMap[member.memberId]?.status ?: "Absent"

      val matchesFilter = when (selectedFilter) {
        "Present" -> currentStatus == "Present"
        "Absent" -> currentStatus == "Absent"
        "Late" -> currentStatus == "Late"
        "Excused" -> currentStatus == "Excused"
        "Uniform" -> member.uniformAccessApproved
        else -> true
      }
      matchesSearch && matchesFilter
    }
  }

  Scaffold(
    topBar = {
      ChurchTopAppBar(
        title = "የመዝሙር ክፍል አቴንዳንስ",
        subtitle = "Song Dept Choir Roster & Attendance",
        onBackClick = onNavigateBack,
        actions = {
          IconButton(
            onClick = { showUniformAnnouncementDialog = true },
            modifier = Modifier.testTag("uniform_announcement_button")
          ) {
            Icon(
              imageVector = Icons.Default.Campaign,
              contentDescription = "Broadcast Uniform Roster",
              tint = Color.White
            )
          }
        }
      )
    },
    containerColor = ChurchIvory
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
      }

      // Department Banner & Verification Status
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ChurchDeepBlue),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            ChurchLogo3D(size = 48.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "የመዝሙርና ሥነ ጥበብ ክፍል",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Song & Sacred Arts Choir Department • 2019 E.C.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = ChurchGreenBg
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.Default.Verified,
                    contentDescription = null,
                    tint = ChurchGreen,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Active Rehearsal Session • የልምምድ ክፍለ-ጊዜ",
                    color = ChurchGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }
      }

      // Session Type & Date Selector
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = ChurchSurface),
          border = BorderStroke(1.dp, ChurchBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.Event,
                  contentDescription = null,
                  tint = ChurchGold,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "የክፍለ-ጊዜ መምረጫ / Session & Date",
                  style = MaterialTheme.typography.titleSmall,
                  color = ChurchTextPrimary
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = ChurchCardBg,
                border = BorderStroke(1.dp, ChurchBorder)
              ) {
                Text(
                  text = selectedDate,
                  style = MaterialTheme.typography.bodySmall,
                  color = ChurchTextPrimary,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Service Type Selector Chips
            val serviceTypes = listOf(
              "Weekly Choir Practice / የሳምንቱ ልምምድ",
              "Sunday Feast Liturgy / የእሑድ ቅዳሴ",
              "Holiday Vigil Rehearsal / የበዓል ዋዜማ ልምምድ",
              "Youth Choir Assembly / የወጣቶች መዘምራን"
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(serviceTypes) { service ->
                val isSelected = selectedServiceType == service
                FilterChip(
                  selected = isSelected,
                  onClick = { selectedServiceType = service },
                  label = {
                    Text(
                      text = service,
                      fontSize = 11.sp,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ChurchDeepBlue,
                    selectedLabelColor = Color.White,
                    containerColor = ChurchCardBg,
                    labelColor = ChurchTextSecondary
                  )
                )
              }
            }
          }
        }
      }

      // Summary Statistics (KPI Cards with smooth, eye-friendly contrast)
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ChoirKpiCard(
            title = "Total Choir",
            amharic = "አጠቃላይ",
            count = totalChoir,
            bgColor = ChurchCardBg,
            textColor = ChurchTextPrimary,
            modifier = Modifier.weight(1f)
          )
          ChoirKpiCard(
            title = "Present",
            amharic = "የተገኙ",
            count = presentCount,
            bgColor = ChurchGreenBg,
            textColor = ChurchGreen,
            modifier = Modifier.weight(1f)
          )
          ChoirKpiCard(
            title = "Absent",
            amharic = "የቀሩ",
            count = absentCount,
            bgColor = ChurchRedBg,
            textColor = ChurchRed,
            modifier = Modifier.weight(1f)
          )
          ChoirKpiCard(
            title = "Vestment OK",
            amharic = "ልብስ",
            count = uniformApprovedCount,
            bgColor = ChurchAmberBg,
            textColor = ChurchGold,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Batch Action Controls & Search
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
              Text(
                "Search member by name or ID (ስም ወይም መለያ ፈልግ)...",
                fontSize = 13.sp,
                color = ChurchTextMuted
              )
            },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = null, tint = ChurchTextSecondary)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear", tint = ChurchTextSecondary)
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("choir_member_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = ChurchDeepBlue,
              unfocusedBorderColor = ChurchBorder,
              focusedContainerColor = ChurchSurface,
              unfocusedContainerColor = ChurchSurface
            ),
            singleLine = true
          )

          // Filter Row & Batch Action Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            LazyRow(
              modifier = Modifier.weight(1f),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val filters = listOf(
                "All" to "ሁሉም (${choirMembers.size})",
                "Present" to "የተገኙ ($presentCount)",
                "Absent" to "የቀሩ ($absentCount)",
                "Late" to "የዘገዩ ($lateCount)",
                "Excused" to "ፍቃድ ($excusedCount)",
                "Uniform" to "ልብስ የጸደቀ ($uniformApprovedCount)"
              )

              items(filters) { (key, label) ->
                val isSelected = selectedFilter == key
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) ChurchDeepBlue else ChurchSurface,
                  border = BorderStroke(1.dp, if (isSelected) ChurchDeepBlue else ChurchBorder),
                  modifier = Modifier.clickable { selectedFilter = key }
                ) {
                  Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else ChurchTextSecondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick Action: Mark All Present
            FilledTonalButton(
              onClick = {
                viewModel.markAllPresent(filteredMembers, selectedDate, selectedServiceType)
                Toast.makeText(context, "Marked all as Present in Room Database", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = ChurchGreenBg,
                contentColor = ChurchGreen
              ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.testTag("mark_all_present_button")
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Mark All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Member Attendance Cards List
      if (filteredMembers.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ChurchSurface),
            border = BorderStroke(1.dp, ChurchBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp)
          ) {
            Column(
              modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                Icons.Default.PersonSearch,
                contentDescription = null,
                tint = ChurchTextMuted,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "ምንም የተገኘ መዘምር የለም / No Choir Members Found",
                style = MaterialTheme.typography.bodyMedium,
                color = ChurchTextSecondary
              )
            }
          }
        }
      } else {
        items(filteredMembers, key = { it.id }) { member ->
          val attendanceRecord = attendanceMap[member.memberId]
          val currentStatus = attendanceRecord?.status ?: "Absent"

          ChoirMemberAttendanceCard(
            member = member,
            currentStatus = currentStatus,
            onStatusChange = { newStatus ->
              viewModel.toggleAttendanceStatus(
                memberId = member.memberId,
                memberName = member.fullName,
                date = selectedDate,
                serviceType = selectedServiceType,
                newStatus = newStatus
              )
            },
            onToggleUniformAccess = {
              viewModel.toggleMemberUniformAccess(member.memberId, !member.uniformAccessApproved)
              val statusText = if (!member.uniformAccessApproved) "authorized" else "revoked"
              Toast.makeText(
                context,
                "Holiday vestment access $statusText for ${member.fullName}",
                Toast.LENGTH_SHORT
              ).show()
            }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

  // Uniform Roster Announcement Dialog
  if (showUniformAnnouncementDialog) {
    val qualifiedNames = choirMembers.filter { it.uniformAccessApproved }.map { it.fullName }

    AlertDialog(
      onDismissRequest = { showUniformAnnouncementDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Campaign, contentDescription = null, tint = ChurchGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            "የበዓል ደንብ ልብስ ማስታወቂያ ማውጫ",
            style = MaterialTheme.typography.titleMedium,
            color = ChurchTextPrimary
          )
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "ይህ ማስታወቂያ የደንብ ልብስ የተፈቀደላቸውን ${qualifiedNames.size} መዘምራን ስም ዝርዝር በዋናው የሰንበት ት/ቤት ማስታወቂያ ሰሌዳ ላይ ይለጥፋል።",
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextSecondary
          )

          OutlinedTextField(
            value = holidayTitle,
            onValueChange = { holidayTitle = it },
            label = { Text("የበዓሉ ስም / Holiday Occasion") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
          )

          OutlinedTextField(
            value = pickupLocation,
            onValueChange = { pickupLocation = it },
            label = { Text("ልብስ የመረከቢያ ሰዓትና ቦታ / Pick-up Details") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
          )

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchAmberBg,
            border = BorderStroke(1.dp, ChurchGold)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "የተፈቀደላቸው መዘምራን ቁጥር፦ ${qualifiedNames.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ChurchDarkGold
              )
              Text(
                text = qualifiedNames.take(5).joinToString(", ") + if (qualifiedNames.size > 5) "..." else "",
                fontSize = 11.sp,
                color = ChurchTextPrimary
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.publishHolidayUniformAnnouncement(
              holidayName = holidayTitle,
              qualifiedNames = qualifiedNames,
              pickupDetails = pickupLocation
            )
            showUniformAnnouncementDialog = false
            Toast.makeText(context, "Holiday Vestment announcement published!", Toast.LENGTH_LONG).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
        ) {
          Text("ማስታወቂያውን አውጣ (Publish)")
        }
      },
      dismissButton = {
        TextButton(onClick = { showUniformAnnouncementDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

/**
 * KPI Metric Card component with soothing, gentle tones.
 */
@Composable
private fun ChoirKpiCard(
  title: String,
  amharic: String,
  count: Int,
  bgColor: Color,
  textColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = bgColor),
    border = BorderStroke(1.dp, ChurchBorder),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = count.toString(),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
      )
      Text(
        text = amharic,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = textColor
      )
    }
  }
}

/**
 * Individual Choir Member Card with 1-Tap Attendance Toggles and Holiday Uniform Access.
 */
@Composable
private fun ChoirMemberAttendanceCard(
  member: MemberItem,
  currentStatus: String,
  onStatusChange: (String) -> Unit,
  onToggleUniformAccess: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = ChurchSurface),
    border = BorderStroke(
      1.dp,
      when (currentStatus) {
        "Present" -> ChurchGreen.copy(alpha = 0.35f)
        "Absent" -> ChurchRed.copy(alpha = 0.25f)
        "Late" -> ChurchAmber.copy(alpha = 0.35f)
        "Excused" -> ChurchLightBlue.copy(alpha = 0.35f)
        else -> ChurchBorder
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("choir_member_card_${member.memberId}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header: Avatar, Name, ID, and Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Avatar circle
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (member.gender == "Male") ChurchDeepBlue else ChurchGold),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = member.fullName.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name & Grade details
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = member.fullName,
            style = MaterialTheme.typography.titleMedium,
            color = ChurchTextPrimary,
            fontWeight = FontWeight.SemiBold
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = member.memberId,
              fontSize = 11.sp,
              color = ChurchTextMuted
            )
            Text(
              text = "•",
              fontSize = 10.sp,
              color = ChurchTextMuted
            )
            Text(
              text = member.classGrade,
              fontSize = 11.sp,
              color = ChurchTextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Current status pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = when (currentStatus) {
            "Present" -> ChurchGreenBg
            "Absent" -> ChurchRedBg
            "Late" -> ChurchAmberBg
            "Excused" -> ChurchBlueBg
            else -> ChurchCardBg
          },
          border = BorderStroke(
            1.dp,
            when (currentStatus) {
              "Present" -> ChurchGreen
              "Absent" -> ChurchRed
              "Late" -> ChurchAmber
              "Excused" -> ChurchLightBlue
              else -> ChurchBorder
            }
          )
        ) {
          Text(
            text = when (currentStatus) {
              "Present" -> "አለ ✅"
              "Absent" -> "ቀረ ❌"
              "Late" -> "ዘገየ ⏰"
              "Excused" -> "ፍቃድ 📝"
              else -> currentStatus
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = when (currentStatus) {
              "Present" -> ChurchGreen
              "Absent" -> ChurchRed
              "Late" -> ChurchAmber
              "Excused" -> ChurchLightBlue
              else -> ChurchTextSecondary
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Direct 1-Tap Attendance Toggles
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        val statuses = listOf(
          "Present" to "Present / አለ",
          "Absent" to "Absent / ቀረ",
          "Late" to "Late / ዘገየ",
          "Excused" to "Leave / ፍቃድ"
        )

        statuses.forEach { (statusKey, label) ->
          val isSelected = currentStatus == statusKey
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) {
              when (statusKey) {
                "Present" -> ChurchGreen
                "Absent" -> ChurchRed
                "Late" -> ChurchAmber
                "Excused" -> ChurchLightBlue
                else -> ChurchDeepBlue
              }
            } else {
              ChurchCardBg
            },
            border = BorderStroke(
              1.dp,
              if (isSelected) Color.Transparent else ChurchBorder
            ),
            modifier = Modifier
              .weight(1f)
              .clickable { onStatusChange(statusKey) }
              .testTag("attendance_toggle_${member.memberId}_$statusKey")
          ) {
            Box(
              modifier = Modifier.padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = when (statusKey) {
                  "Present" -> "አለ"
                  "Absent" -> "ቀረ"
                  "Late" -> "ዘገየ"
                  "Excused" -> "ፍቃድ"
                  else -> label
                },
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else ChurchTextPrimary
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Holiday Uniform Access Control Row
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (member.uniformAccessApproved) ChurchAmberBg else ChurchCardBg,
        border = BorderStroke(
          1.dp,
          if (member.uniformAccessApproved) ChurchGold else ChurchBorder
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (member.uniformAccessApproved) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
              contentDescription = null,
              tint = if (member.uniformAccessApproved) ChurchGold else ChurchTextMuted,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = if (member.uniformAccessApproved) "የበዓል ደንብ ልብስ ተፈቅዷል (Vestment Authorized)" else "የበዓል ደንብ ልብስ አልተፈቀደም (Not Authorized)",
                fontSize = 11.sp,
                fontWeight = if (member.uniformAccessApproved) FontWeight.Bold else FontWeight.Normal,
                color = if (member.uniformAccessApproved) ChurchDarkGold else ChurchTextSecondary
              )
            }
          }

          // Toggle button for uniform authorization
          TextButton(
            onClick = onToggleUniformAccess,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.testTag("toggle_uniform_btn_${member.memberId}")
          ) {
            Text(
              text = if (member.uniformAccessApproved) "Revoke (ሰርዝ)" else "Grant (ፍቀድ)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (member.uniformAccessApproved) ChurchRed else ChurchDeepBlue
            )
          }
        }
      }
    }
  }
}
