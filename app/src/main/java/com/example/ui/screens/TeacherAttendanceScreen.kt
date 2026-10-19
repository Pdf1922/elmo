package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AttendanceRecord
import com.example.data.model.User
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TeacherAttendanceScreen allows Teachers and Administrators to record student attendance
 * for Sunday school sessions and immediately synchronizes the records with Cloud Firestore.
 * Access is strictly restricted: students and parents are shown an access restricted state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceScreen(
  viewModel: SundaySchoolViewModel,
  currentUser: User,
  onNavigateBack: () -> Unit = {}
) {
  var isViewingLessonNotes by remember { mutableStateOf(false) }

  val roleLower = currentUser.role.lowercase()
  val isAuthorized = roleLower in listOf("manager", "super admin", "admin", "deptadmin") ||
    roleLower.contains("teacher") || roleLower.contains("instructor")

  if (isViewingLessonNotes) {
    LessonNotesScreen(
      viewModel = viewModel,
      currentUser = currentUser,
      onNavigateBack = { isViewingLessonNotes = false }
    )
    return
  }

  if (!isAuthorized) {
    // Role-based Access Restriction Guard
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(ChurchIvory)
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = ChurchCrimson,
            modifier = Modifier.size(56.dp)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Access Restricted / የተፈቀደው ለመምህራን ብቻ ነው",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = ChurchDeepBlue
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Attendance marking for Sunday school sessions is restricted to Teachers and Administrators. As a Student or Parent, please check 'My Attendance' to view your attendance history.",
            fontSize = 13.sp,
            color = ChurchTextSecondary,
            lineHeight = 19.sp
          )
        }
      }
    }
    return
  }

  val members by viewModel.members.collectAsState()
  val students = remember(members) {
    members.filter {
      val cat = it.memberCategory.lowercase()
      cat.contains("student") || cat.contains("worker") || it.memberType == "Regular"
    }
  }

  var selectedDate by remember {
    mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
  }

  val serviceSessions = listOf(
    "Sunday School Class / የሰንበት ት/ቤት ትምህርት",
    "Sunday Liturgy / የቅዳሴ አገልግሎት",
    "Hymn Practice / የመዝሙር ጥናት",
    "Fellowship / የወጣቶች ጉባኤ"
  )
  var selectedSession by remember { mutableStateOf(serviceSessions.first()) }

  val gradeFilters = listOf(
    "All Classes / ሁሉም",
    "Grade 1-4 / 1-4ኛ ክፍል",
    "Grade 5-8 / 5-8ኛ ክፍል",
    "Grade 9-12 / 9-12ኛ ክፍል"
  )
  var selectedGradeFilter by remember { mutableStateOf(gradeFilters.first()) }

  // Attendance state map: memberId -> status ("Present", "Absent", "Excused")
  val attendanceMap = remember { mutableStateMapOf<String, String>() }
  val notesMap = remember { mutableStateMapOf<String, String>() }

  // Initialize all with Present by default
  LaunchedEffect(students) {
    students.forEach { s ->
      if (!attendanceMap.containsKey(s.memberId)) {
        attendanceMap[s.memberId] = "Present"
      }
    }
  }

  val filteredStudents = remember(students, selectedGradeFilter) {
    if (selectedGradeFilter.startsWith("All")) {
      students
    } else {
      val filterKey = selectedGradeFilter.split("/").first().trim()
      students.filter { it.classGrade.contains(filterKey.take(7), ignoreCase = true) }
    }
  }

  val presentCount = filteredStudents.count { attendanceMap[it.memberId] == "Present" }
  val absentCount = filteredStudents.count { attendanceMap[it.memberId] == "Absent" }
  val excusedCount = filteredStudents.count { attendanceMap[it.memberId] == "Excused" }

  var statusFeedback by remember { mutableStateOf<String?>(null) }
  var isSaving by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ChurchIvory)
      .padding(16.dp)
  ) {
    // Header Hero
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = ChurchDeepBlue)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(ChurchGold),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.EventAvailable,
                contentDescription = null,
                tint = ChurchDarkBlue,
                modifier = Modifier.size(26.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Session Attendance / የክፍለ ጊዜ ክትትል",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Teacher: ${currentUser.fullName} • Live Cloud Sync",
                color = ChurchLightGold,
                fontSize = 12.sp
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchGold,
            modifier = Modifier
              .clickable { isViewingLessonNotes = true }
              .testTag("teacher_switch_to_lesson_notes_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
              Icon(Icons.Default.MenuBook, contentDescription = null, tint = ChurchDarkBlue, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Lesson Notes", color = ChurchDarkBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Session Stats Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.12f),
            modifier = Modifier.weight(1f)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(vertical = 6.dp)
            ) {
              Text(text = "Enrolled", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
              Text(text = "${filteredStudents.size}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchGreen.copy(alpha = 0.35f),
            modifier = Modifier.weight(1f)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(vertical = 6.dp)
            ) {
              Text(text = "Present", color = Color.White, fontSize = 10.sp)
              Text(text = "$presentCount", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchCrimson.copy(alpha = 0.35f),
            modifier = Modifier.weight(1f)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(vertical = 6.dp)
            ) {
              Text(text = "Absent", color = Color.White, fontSize = 10.sp)
              Text(text = "$absentCount", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ChurchGold.copy(alpha = 0.35f),
            modifier = Modifier.weight(1f)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(vertical = 6.dp)
            ) {
              Text(text = "Excused", color = Color.White, fontSize = 10.sp)
              Text(text = "$excusedCount", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Session Selector & Grade Filter
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Session:",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = ChurchTextPrimary
      )
      ScrollableTabRow(
        selectedTabIndex = serviceSessions.indexOf(selectedSession).coerceAtLeast(0),
        edgePadding = 0.dp,
        containerColor = Color.Transparent,
        modifier = Modifier.weight(1f)
      ) {
        serviceSessions.forEach { sess ->
          Tab(
            selected = sess == selectedSession,
            onClick = { selectedSession = sess },
            text = { Text(sess.split("/").first().trim(), fontSize = 11.sp) }
          )
        }
      }
    }

    // Quick Action Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Mark All Present Shortcut
      OutlinedButton(
        onClick = {
          filteredStudents.forEach { s ->
            attendanceMap[s.memberId] = "Present"
          }
        },
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.testTag("mark_all_present_button")
      ) {
        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Mark All Present", fontSize = 11.sp)
      }

      // Save & Sync to Cloud Firestore Button
      Button(
        onClick = {
          isSaving = true
          val recordsToSave = filteredStudents.map { s ->
            AttendanceRecord(
              memberId = s.memberId,
              memberName = s.fullName,
              date = selectedDate,
              serviceType = selectedSession,
              status = attendanceMap[s.memberId] ?: "Present",
              recordedBy = currentUser.fullName,
              notes = notesMap[s.memberId] ?: ""
            )
          }
          viewModel.recordBatchAttendance(recordsToSave) {
            isSaving = false
            statusFeedback = "Saved ${recordsToSave.size} attendance records to Room & Cloud Firestore!"
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ChurchDarkBlue),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = Modifier.testTag("save_sync_attendance_button"),
        enabled = !isSaving
      ) {
        if (isSaving) {
          CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Syncing...", fontSize = 11.sp)
        } else {
          Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = ChurchLightGold)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Save & Sync to Firestore", fontSize = 11.sp, color = Color.White)
        }
      }
    }

    // Status Feedback Alert
    statusFeedback?.let { msg ->
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = ChurchGreen.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChurchGreen),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChurchGreen, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = msg, color = ChurchGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }

    // Student Roster
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(bottom = 24.dp)
    ) {
      items(filteredStudents) { student ->
        val currentStatus = attendanceMap[student.memberId] ?: "Present"

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_row_${student.memberId}"),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(1.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = student.fullName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = ChurchTextPrimary
                )
                Text(
                  text = "ID: ${student.memberId} • ${student.classGrade}",
                  fontSize = 11.sp,
                  color = ChurchTextSecondary
                )
              }

              // Status Selector Pill Group
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Present
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (currentStatus == "Present") ChurchGreen else Color(0xFFF0F0F0),
                  modifier = Modifier
                    .clickable { attendanceMap[student.memberId] = "Present" }
                    .testTag("status_present_${student.memberId}")
                ) {
                  Text(
                    text = "Present",
                    color = if (currentStatus == "Present") Color.White else ChurchTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }

                // Absent
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (currentStatus == "Absent") ChurchCrimson else Color(0xFFF0F0F0),
                  modifier = Modifier
                    .clickable { attendanceMap[student.memberId] = "Absent" }
                    .testTag("status_absent_${student.memberId}")
                ) {
                  Text(
                    text = "Absent",
                    color = if (currentStatus == "Absent") Color.White else ChurchTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }

                // Excused
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (currentStatus == "Excused") ChurchGold else Color(0xFFF0F0F0),
                  modifier = Modifier
                    .clickable { attendanceMap[student.memberId] = "Excused" }
                    .testTag("status_excused_${student.memberId}")
                ) {
                  Text(
                    text = "Excused",
                    color = if (currentStatus == "Excused") ChurchDarkBlue else ChurchTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
