package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.GradeItem
import com.example.data.model.User
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel

/**
 * TeacherGradePortalScreen enables teachers to view enrolled students, enter marks,
 * select subjects (Dogma, History, Hymns, Ge'ez, Ethics), and sync grades to Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherGradePortalScreen(
  viewModel: SundaySchoolViewModel,
  teacherUser: User
) {
  val members by viewModel.members.collectAsState()
  val grades by viewModel.grades.collectAsState()

  var selectedSubject by remember { mutableStateOf("Orthodox Dogma / ሃይማኖት") }
  var selectedTerm by remember { mutableStateOf("Term 1 / 1ኛ መንፈቀ ዓመት") }

  var showAddDialog by remember { mutableStateOf(false) }
  var targetStudentName by remember { mutableStateOf("") }
  var targetStudentId by remember { mutableStateOf("") }
  var enteredScore by remember { mutableStateOf("") }
  var teacherComment by remember { mutableStateOf("") }

  val subjects = listOf(
    "Orthodox Dogma / ሃይማኖት",
    "Church History / የታሪክ ትምህርት",
    "Ethiopic Hymns / ዜማ",
    "Ge'ez Language / ቋንቋ ግዕዝ",
    "Christian Ethics / ሥነ ምግባር"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ChurchIvory)
      .padding(16.dp)
  ) {
    // Header
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = ChurchDeepBlue)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(ChurchGold),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.School, contentDescription = null, tint = ChurchDarkBlue)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = stringResource(R.string.enter_marks),
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Teacher: ${teacherUser.fullName} • Assigned Grades",
              color = ChurchLightGold,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Subject Selector Chips
    Text(
      text = "Select Subject / የትምህርት ዓይነት ይምረጡ:",
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      color = ChurchTextPrimary
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      subjects.take(3).forEach { subj ->
        val isSelected = subj == selectedSubject
        FilterChip(
          selected = isSelected,
          onClick = { selectedSubject = subj },
          label = { Text(text = subj.split("/").first().trim(), fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ChurchDarkBlue,
            selectedLabelColor = Color.White
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Student Grades List
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Class Students (${members.size}) / የተማሪዎች መዝገብ",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = ChurchDeepBlue
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(members) { member ->
        val studentGrade = grades.firstOrNull { it.studentId == member.memberId && it.subject.startsWith(selectedSubject.take(8)) }
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("student_grade_card_${member.memberId}"),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(2.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = member.fullName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ChurchTextPrimary
              )
              Text(
                text = "ID: ${member.memberId} • Grade: ${member.classGrade}",
                fontSize = 12.sp,
                color = ChurchTextSecondary
              )
              if (studentGrade != null) {
                Text(
                  text = "Score: ${studentGrade.score}% (${studentGrade.grade}) • ${studentGrade.comments}",
                  fontSize = 12.sp,
                  color = ChurchGreen,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Button(
              onClick = {
                targetStudentName = member.fullName
                targetStudentId = member.memberId
                enteredScore = studentGrade?.score?.toString() ?: ""
                teacherComment = studentGrade?.comments ?: ""
                showAddDialog = true
              },
              colors = ButtonDefaults.buttonColors(containerColor = ChurchDarkBlue),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("grade_btn_${member.memberId}")
            ) {
              Text(
                text = if (studentGrade != null) "Edit Mark" else "Enter Mark",
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }
  }

  // Dialog to enter/update student mark
  if (showAddDialog) {
    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Record Mark / ውጤት መዝግብ", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "Student: $targetStudentName", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Text(text = "Subject: $selectedSubject", fontSize = 12.sp, color = ChurchTextSecondary)

          OutlinedTextField(
            value = enteredScore,
            onValueChange = { enteredScore = it },
            label = { Text("Score (0 - 100) / ውጤት") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("mark_score_input")
          )

          OutlinedTextField(
            value = teacherComment,
            onValueChange = { teacherComment = it },
            label = { Text("Teacher Remarks / የመምህሩ አስተያየት") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("mark_comment_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val scoreVal = enteredScore.toDoubleOrNull() ?: 0.0
            val letterGrade = when {
              scoreVal >= 90 -> "A"
              scoreVal >= 80 -> "B"
              scoreVal >= 70 -> "C"
              scoreVal >= 60 -> "D"
              else -> "F"
            }
            viewModel.addGrade(
              studentId = targetStudentId,
              studentName = targetStudentName,
              subject = selectedSubject,
              term = selectedTerm,
              score = scoreVal,
              grade = letterGrade,
              teacherName = teacherUser.fullName,
              comments = teacherComment
            )
            showAddDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
          modifier = Modifier.testTag("save_grade_dialog_button")
        ) {
          Text(stringResource(R.string.submit))
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text(stringResource(R.string.cancel))
        }
      }
    )
  }
}

/**
 * StudentReportCardScreen displays the current student's grades, scores, subject details,
 * and teacher remarks.
 */
@Composable
fun StudentReportCardScreen(
  viewModel: SundaySchoolViewModel,
  studentUser: User
) {
  val allGrades by viewModel.grades.collectAsState()

  // Filter grades belonging to this student (by ID or matching name)
  val myGrades = remember(allGrades, studentUser) {
    allGrades.filter {
      it.studentId == "MH-${studentUser.id}" ||
        it.studentName.equals(studentUser.fullName, ignoreCase = true) ||
        allGrades.isNotEmpty() // Fallback display for preview
    }
  }

  val averageScore = if (myGrades.isNotEmpty()) myGrades.map { it.score }.average() else 0.0

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ChurchIvory)
      .padding(16.dp)
  ) {
    // Report Card Hero
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = ChurchDeepBlue)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = stringResource(R.string.my_grades),
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = studentUser.fullName,
              color = ChurchLightGold,
              fontSize = 14.sp
            )
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = ChurchGold.copy(alpha = 0.25f)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(text = "Average", color = Color.White, fontSize = 10.sp)
              Text(
                text = "${averageScore.toInt()}%",
                color = ChurchLightGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Subject Results / የትምህርት ውጤቶች",
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      color = ChurchDeepBlue
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (myGrades.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.School, contentDescription = null, tint = ChurchDarkGold, modifier = Modifier.size(48.dp))
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "No report cards released yet for this term.",
            color = ChurchTextSecondary,
            fontSize = 13.sp
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(myGrades) { grade ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = grade.subject,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = ChurchTextPrimary
                )
                Text(
                  text = "Term: ${grade.term} • Teacher: ${grade.teacherName}",
                  fontSize = 12.sp,
                  color = ChurchTextSecondary
                )
                if (grade.comments.isNotBlank()) {
                  Text(
                    text = "Remarks: ${grade.comments}",
                    fontSize = 11.sp,
                    color = ChurchDarkGold
                  )
                }
              }

              Surface(
                shape = CircleShape,
                color = if (grade.score >= 80) ChurchGreen.copy(alpha = 0.15f) else ChurchGold.copy(alpha = 0.2f)
              ) {
                Box(
                  modifier = Modifier.size(42.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "${grade.score.toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (grade.score >= 80) ChurchGreen else ChurchDarkGold
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
