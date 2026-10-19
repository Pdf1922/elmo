package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LessonNote
import com.example.data.model.User
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel

/**
 * LessonNotesScreen provides:
 * 1. For Teachers and Admins: An authoring section to compose and publish weekly lesson summaries,
 *    storing them locally in Room and syncing them to Firebase Firestore (church_lessons collection).
 * 2. For Students, Parents, and Teachers: A clear, organized, searchable list of lesson notes
 *    broken down by subject, week, scripture reading, and memory verse.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonNotesScreen(
  viewModel: SundaySchoolViewModel,
  currentUser: User,
  onNavigateBack: () -> Unit = {}
) {
  val roleLower = currentUser.role.lowercase()
  val isTeacherOrAdmin = roleLower in listOf("manager", "super admin", "admin", "deptadmin") ||
    roleLower.contains("teacher") || roleLower.contains("instructor")

  val allLessons by viewModel.lessonNotes.collectAsState(initial = emptyList())

  var activeTab by remember { mutableStateOf(if (isTeacherOrAdmin) 0 else 0) } // 0: View Lessons, 1: Post Lesson
  var selectedSubjectFilter by remember { mutableStateOf("All") }
  var searchQuery by remember { mutableStateOf("") }
  var expandedLessonId by remember { mutableStateOf<String?>(null) }

  // Post Lesson Form States
  var inputTitle by remember { mutableStateOf("") }
  var inputTitleAmharic by remember { mutableStateOf("") }
  var inputSubject by remember { mutableStateOf("Bible Study") }
  var inputGrade by remember { mutableStateOf("All Classes") }
  var inputWeekNumber by remember { mutableStateOf("1") }
  var inputScripture by remember { mutableStateOf("") }
  var inputMemoryVerse by remember { mutableStateOf("") }
  var inputSummary by remember { mutableStateOf("") }
  var isSubmitting by remember { mutableStateOf(false) }
  var postSuccessMessage by remember { mutableStateOf<String?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val subjectOptions = listOf(
    "Bible Study",
    "Church History",
    "Hymns & Liturgy",
    "Ge'ez & Amharic",
    "Christian Ethics"
  )

  val gradeOptions = listOf(
    "All Classes",
    "Kindergarten - Grade 2",
    "Grade 3-5",
    "Grade 6-8",
    "Grade 9-12",
    "Youth Fellowship"
  )

  val filteredLessons = remember(allLessons, selectedSubjectFilter, searchQuery) {
    allLessons.filter { lesson ->
      val matchesSubject = selectedSubjectFilter == "All" || lesson.subject.contains(selectedSubjectFilter, ignoreCase = true)
      val matchesSearch = searchQuery.isBlank() ||
        lesson.title.contains(searchQuery, ignoreCase = true) ||
        lesson.titleAmharic.contains(searchQuery, ignoreCase = true) ||
        lesson.summary.contains(searchQuery, ignoreCase = true) ||
        lesson.scriptureReference.contains(searchQuery, ignoreCase = true) ||
        lesson.memoryVerse.contains(searchQuery, ignoreCase = true)
      matchesSubject && matchesSearch
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ChurchIvory)
      .padding(16.dp)
  ) {
    // Header Banner
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = ChurchDeepBlue)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(ChurchGold),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.MenuBook,
              contentDescription = null,
              tint = ChurchDarkBlue,
              modifier = Modifier.size(26.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Weekly Lesson Notes / የትምህርት ማስታወሻ",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (isTeacherOrAdmin) "Teacher Portal • Post & Cloud Sync" else "Student Portal • Study & Reflection",
              color = ChurchLightGold,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // If teacher or admin, show tabs to Switch between "Browse Lessons" and "Post Lesson"
    if (isTeacherOrAdmin) {
      TabRow(
        selectedTabIndex = activeTab,
        containerColor = Color.White,
        contentColor = ChurchDeepBlue,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .border(1.dp, ChurchBorder, RoundedCornerShape(12.dp))
      ) {
        Tab(
          selected = activeTab == 0,
          onClick = { activeTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Lesson List (${filteredLessons.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
          },
          modifier = Modifier.testTag("tab_lesson_list")
        )
        Tab(
          selected = activeTab == 1,
          onClick = { activeTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Post New Lesson", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
          },
          modifier = Modifier.testTag("tab_post_lesson")
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    if (activeTab == 1 && isTeacherOrAdmin) {
      // POST LESSON NOTE FORM
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("post_lesson_form"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                text = "Publish Weekly Sunday School Summary",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ChurchDeepBlue
              )

              postSuccessMessage?.let { msg ->
                Surface(
                  color = ChurchGreenBg,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChurchGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(msg, color = ChurchGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                  }
                }
              }

              errorMessage?.let { err ->
                Surface(
                  color = ChurchRedBg,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = ChurchRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(err, color = ChurchRed, fontSize = 12.sp)
                  }
                }
              }

              // Title (English)
              OutlinedTextField(
                value = inputTitle,
                onValueChange = { inputTitle = it },
                label = { Text("Lesson Title / Topic (e.g., The Holy Trinity)") },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_lesson_title"),
                singleLine = true
              )

              // Title (Amharic)
              OutlinedTextField(
                value = inputTitleAmharic,
                onValueChange = { inputTitleAmharic = it },
                label = { Text("ርዕስ በአማርኛ (ምሳሌ፡ ምስጢረ ሥላሴ)") },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_lesson_title_amharic"),
                singleLine = true
              )

              // Subject Selection Chips
              Text("Subject / የትምህርት ዓይነት:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ChurchTextSecondary)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                subjectOptions.take(3).forEach { subj ->
                  FilterChip(
                    selected = inputSubject == subj,
                    onClick = { inputSubject = subj },
                    label = { Text(subj, fontSize = 11.sp) },
                    modifier = Modifier.testTag("chip_subject_$subj")
                  )
                }
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                subjectOptions.drop(3).forEach { subj ->
                  FilterChip(
                    selected = inputSubject == subj,
                    onClick = { inputSubject = subj },
                    label = { Text(subj, fontSize = 11.sp) },
                    modifier = Modifier.testTag("chip_subject_$subj")
                  )
                }
              }

              // Class Grade & Week Number
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = inputWeekNumber,
                  onValueChange = { inputWeekNumber = it.filter { c -> c.isDigit() } },
                  label = { Text("Week #") },
                  modifier = Modifier
                    .weight(1f)
                    .testTag("input_week_number"),
                  singleLine = true
                )

                OutlinedTextField(
                  value = inputGrade,
                  onValueChange = { inputGrade = it },
                  label = { Text("Grade / Class") },
                  modifier = Modifier
                    .weight(2f)
                    .testTag("input_grade"),
                  singleLine = true
                )
              }

              // Scripture Reference
              OutlinedTextField(
                value = inputScripture,
                onValueChange = { inputScripture = it },
                label = { Text("Scripture Reference / ምንባብ (e.g., John 1:1-14)") },
                leadingIcon = { Icon(Icons.Default.Book, contentDescription = null, tint = ChurchGold) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_scripture"),
                singleLine = true
              )

              // Memory Verse
              OutlinedTextField(
                value = inputMemoryVerse,
                onValueChange = { inputMemoryVerse = it },
                label = { Text("Memory Verse / የቃል ጥቅስ (e.g., 'በመጀመሪያ ቃል ነበረ')") },
                leadingIcon = { Icon(Icons.Default.FormatQuote, contentDescription = null, tint = ChurchGold) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_memory_verse"),
                singleLine = true
              )

              // Lesson Summary
              OutlinedTextField(
                value = inputSummary,
                onValueChange = { inputSummary = it },
                label = { Text("Weekly Lesson Summary & Key Takeaways / የትምህርቱ ማጠቃለያ") },
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 120.dp)
                  .testTag("input_summary"),
                maxLines = 8
              )

              Spacer(modifier = Modifier.height(4.dp))

              // Submit Button
              Button(
                onClick = {
                  if (inputTitle.isBlank() || inputSummary.isBlank()) {
                    errorMessage = "Please enter both a title and summary text."
                    return@Button
                  }
                  errorMessage = null
                  isSubmitting = true
                  val weekInt = inputWeekNumber.toIntOrNull() ?: 1
                  viewModel.postWeeklyLessonNote(
                    title = inputTitle,
                    titleAmharic = inputTitleAmharic,
                    subject = inputSubject,
                    classGrade = inputGrade,
                    weekNumber = weekInt,
                    scriptureReference = inputScripture,
                    summary = inputSummary,
                    memoryVerse = inputMemoryVerse
                  ) { success ->
                    isSubmitting = false
                    if (success) {
                      postSuccessMessage = "Lesson note successfully published & synced to Cloud Firestore!"
                      inputTitle = ""
                      inputTitleAmharic = ""
                      inputScripture = ""
                      inputMemoryVerse = ""
                      inputSummary = ""
                    } else {
                      errorMessage = "Failed to sync note. Saved locally in Room."
                    }
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("submit_lesson_note_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
                shape = RoundedCornerShape(10.dp),
                enabled = !isSubmitting
              ) {
                if (isSubmitting) {
                  CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Publishing & Syncing...", fontSize = 13.sp)
                } else {
                  Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp), tint = ChurchLightGold)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Publish & Sync to Cloud", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
              }
            }
          }
        }
      }
    } else {
      // LESSON LIST VIEW FOR STUDENTS, PARENTS, AND TEACHERS
      Column(modifier = Modifier.fillMaxSize()) {
        // Search & Subject Filter
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search lessons, scripture, topics...", fontSize = 12.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ChurchTextSecondary) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_lessons_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subject Filter Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("All", "Bible", "History", "Hymns", "Ethics").forEach { filter ->
            val isSelected = (filter == "All" && selectedSubjectFilter == "All") ||
              (filter != "All" && selectedSubjectFilter.contains(filter))
            FilterChip(
              selected = isSelected,
              onClick = {
                selectedSubjectFilter = if (filter == "All") "All" else filter
              },
              label = { Text(filter, fontSize = 11.sp) },
              modifier = Modifier.testTag("filter_chip_$filter")
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredLessons.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .testTag("empty_lessons_state"),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.MenuBook, contentDescription = null, tint = ChurchGold, modifier = Modifier.size(48.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text("No lesson notes found", fontWeight = FontWeight.Bold, color = ChurchDeepBlue)
              Text("Check back soon or adjust search filters", fontSize = 12.sp, color = ChurchTextSecondary)
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .testTag("lesson_notes_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(filteredLessons, key = { it.lessonId }) { note ->
              val isExpanded = expandedLessonId == note.lessonId

              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { expandedLessonId = if (isExpanded) null else note.lessonId }
                  .testTag("lesson_card_${note.lessonId}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  // Top Row: Week Badge & Subject Pill
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = ChurchDeepBlue
                    ) {
                      Text(
                        text = "Week ${note.weekNumber}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                      )
                    }

                    Row(
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ChurchPaleGold
                      ) {
                        Text(
                          text = note.subject,
                          color = ChurchDarkGold,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.SemiBold,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ChurchCardBg
                      ) {
                        Text(
                          text = note.classGrade,
                          color = ChurchTextSecondary,
                          fontSize = 10.sp,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  // Title (English & Amharic)
                  Text(
                    text = note.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ChurchDeepBlue
                  )
                  if (note.titleAmharic.isNotBlank()) {
                    Text(
                      text = note.titleAmharic,
                      fontSize = 13.sp,
                      color = ChurchGold,
                      fontWeight = FontWeight.Medium
                    )
                  }

                  // Scripture Reference Pill
                  if (note.scriptureReference.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier
                        .background(ChurchBlueBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                      Icon(Icons.Default.AutoStories, contentDescription = null, tint = ChurchDeepBlue, modifier = Modifier.size(13.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = note.scriptureReference,
                        fontSize = 11.sp,
                        color = ChurchDeepBlue,
                        fontWeight = FontWeight.SemiBold
                      )
                    }
                  }

                  // Memory Verse Banner
                  if (note.memoryVerse.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                      color = ChurchPaleGold,
                      shape = RoundedCornerShape(8.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, ChurchGold.copy(alpha = 0.3f)),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                      ) {
                        Icon(Icons.Default.FormatQuote, contentDescription = null, tint = ChurchDarkGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                          Text(
                            text = "Memory Verse / የቃል ጥቅስ:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChurchDarkGold
                          )
                          Text(
                            text = "“${note.memoryVerse}”",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ChurchTextPrimary
                          )
                        }
                      }
                    }
                  }

                  // Summary Section
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = if (isExpanded) note.summary else note.summary.take(160) + if (note.summary.length > 160) "..." else "",
                    fontSize = 12.sp,
                    color = ChurchTextSecondary,
                    lineHeight = 17.sp
                  )

                  // Footer: Teacher name & Read More toggle
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "By ${note.teacherName} • ${note.datePosted}",
                      fontSize = 10.sp,
                      color = ChurchTextMuted
                    )
                    Text(
                      text = if (isExpanded) "Show Less ▲" else "Read More ▼",
                      fontSize = 11.sp,
                      color = ChurchGold,
                      fontWeight = FontWeight.Bold
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
}
