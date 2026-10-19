package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleQuizQuestion
import com.example.data.model.BibleStory
import com.example.data.model.BibleStoryRepository
import com.example.data.model.QuizResult
import com.example.ui.components.ChurchLogo3D
import com.example.ui.components.ChurchTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel

/**
 * Interactive Bible Quiz Screen based on the Daily Bible Reading content
 * stored in the local Room Database to increase engagement among students.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleQuizScreen(
  viewModel: SundaySchoolViewModel,
  initialStoryId: String? = null,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val dbQuestions by viewModel.quizQuestions.collectAsState()
  val dbStories by viewModel.bibleStories.collectAsState()
  val quizResults by viewModel.quizResults.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()

  val stories = remember(dbStories) {
    if (dbStories.isNotEmpty()) dbStories else BibleStoryRepository.dailyStories
  }

  // Selected story for the quiz (default to initialStoryId or first story)
  var selectedStoryId by remember {
    mutableStateOf(initialStoryId ?: stories.firstOrNull()?.id ?: "story_1")
  }

  val currentStory = remember(selectedStoryId, stories) {
    stories.find { it.id == selectedStoryId } ?: stories.firstOrNull() ?: BibleStoryRepository.dailyStories[0]
  }

  // Questions for the current story
  val storyQuestions = remember(selectedStoryId, dbQuestions) {
    val filtered = dbQuestions.filter { it.storyId == selectedStoryId }
    if (filtered.isNotEmpty()) filtered else dbQuestions.take(2)
  }

  // Quiz progression state
  var currentQuestionIndex by remember(selectedStoryId) { mutableIntStateOf(0) }
  var selectedAnswerIndex by remember(selectedStoryId, currentQuestionIndex) { mutableStateOf<Int?>(null) }
  var isAnswerSubmitted by remember(selectedStoryId, currentQuestionIndex) { mutableStateOf(false) }
  var correctAnswersCount by remember(selectedStoryId) { mutableIntStateOf(0) }
  var isQuizCompleted by remember(selectedStoryId) { mutableStateOf(false) }
  var hasSavedResult by remember(selectedStoryId) { mutableStateOf(false) }
  var showHistoryDialog by remember { mutableStateOf(false) }

  val totalQuestions = storyQuestions.size.coerceAtLeast(1)
  val safeQuestionIndex = currentQuestionIndex.coerceIn(0, (storyQuestions.size - 1).coerceAtLeast(0))
  val currentQuestion = if (storyQuestions.isNotEmpty()) storyQuestions[safeQuestionIndex] else null

  Scaffold(
    topBar = {
      ChurchTopAppBar(
        title = "የዕለቱ የመጽሐፍ ቅዱስ ጥያቄና መልስ",
        subtitle = "Daily Bible Reading Interactive Quiz",
        onBackClick = onNavigateBack,
        actions = {
          IconButton(onClick = { showHistoryDialog = true }) {
            Icon(Icons.Default.Leaderboard, contentDescription = "Quiz Scores", tint = Color.White)
          }
        }
      )
    },
    containerColor = ChurchIvory
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Story Selector Tabs
      Surface(
        color = ChurchDeepBlue,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
          Text(
            text = "Select Daily Reading Topic / ርዕስ ይምረጡ:",
            style = MaterialTheme.typography.labelSmall,
            color = ChurchLightGold
          )
          Spacer(modifier = Modifier.height(6.dp))
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(stories) { story ->
              val isSelected = story.id == selectedStoryId
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) ChurchGold else ChurchMediumBlue,
                modifier = Modifier.clickable {
                  selectedStoryId = story.id
                  currentQuestionIndex = 0
                  selectedAnswerIndex = null
                  isAnswerSubmitted = false
                  correctAnswersCount = 0
                  isQuizCompleted = false
                  hasSavedResult = false
                }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Day ${story.dayNumber}: ${story.titleAmharic.take(16)}...",
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) ChurchDarkBlue else Color.White
                  )
                }
              }
            }
          }
        }
      }

      if (isQuizCompleted) {
        // Quiz Results View
        QuizCompletedView(
          storyTitle = "${currentStory.titleAmharic} (${currentStory.titleEnglish})",
          score = correctAnswersCount,
          totalQuestions = storyQuestions.size,
          participantName = currentUser?.fullName ?: "Student",
          hasSaved = hasSavedResult,
          onSaveResult = {
            viewModel.submitQuizResult(
              storyId = selectedStoryId,
              storyTitle = currentStory.titleAmharic,
              participantName = currentUser?.fullName ?: "Student",
              score = correctAnswersCount,
              totalQuestions = storyQuestions.size
            ) {
              hasSavedResult = true
              Toast.makeText(context, "Score successfully saved to church records! / ውጤት ተመዝግቧል!", Toast.LENGTH_SHORT).show()
            }
          },
          onRetake = {
            currentQuestionIndex = 0
            selectedAnswerIndex = null
            isAnswerSubmitted = false
            correctAnswersCount = 0
            isQuizCompleted = false
            hasSavedResult = false
          },
          onViewHistory = { showHistoryDialog = true }
        )
      } else if (currentQuestion != null) {
        // Active Quiz Question View
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Progress & Header Card
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = ChurchDeepBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "ጥያቄ ${safeQuestionIndex + 1} ከ ${storyQuestions.size}",
                      fontWeight = FontWeight.Bold,
                      color = ChurchDeepBlue,
                      fontSize = 13.sp
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChurchPaleGold
                  ) {
                    Text(
                      text = "ውጤት: $correctAnswersCount / ${storyQuestions.size}",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = ChurchDarkGold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                  progress = { (safeQuestionIndex + 1).toFloat() / storyQuestions.size.toFloat() },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = ChurchDeepBlue,
                  trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "መንፈሳዊ ምንጭ: ${currentStory.verseReference}",
                  style = MaterialTheme.typography.labelSmall,
                  color = ChurchTextMuted
                )
              }
            }
          }

          // Question Card
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = ChurchPaleGold.copy(alpha = 0.4f)),
              border = BorderStroke(1.dp, ChurchGold.copy(alpha = 0.5f))
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = currentQuestion.questionAmharic,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = ChurchDarkBlue,
                  lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = currentQuestion.questionEnglish,
                  style = MaterialTheme.typography.bodySmall,
                  color = ChurchTextSecondary,
                  lineHeight = 18.sp
                )
              }
            }
          }

          // Options A, B, C, D
          val options = listOf(
            0 to currentQuestion.optionA,
            1 to currentQuestion.optionB,
            2 to currentQuestion.optionC,
            3 to currentQuestion.optionD
          )

          items(options) { (index, text) ->
            val isSelected = selectedAnswerIndex == index
            val isCorrect = index == currentQuestion.correctOptionIndex
            val optionLetter = when (index) {
              0 -> "ሀ / A"
              1 -> "ለ / B"
              2 -> "ሐ / C"
              else -> "መ / D"
            }

            val (bgColor, borderColor, textColor) = when {
              isAnswerSubmitted && isCorrect -> Triple(Color(0xFFDCFCE7), ChurchGreen, ChurchGreen)
              isAnswerSubmitted && isSelected && !isCorrect -> Triple(Color(0xFFFEE2E2), ChurchRed, ChurchRed)
              isSelected -> Triple(ChurchPaleGold, ChurchGold, ChurchDeepBlue)
              else -> Triple(MaterialTheme.colorScheme.surface, Color(0xFFE2E8F0), ChurchTextPrimary)
            }

            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = bgColor),
              border = BorderStroke(if (isSelected || (isAnswerSubmitted && isCorrect)) 2.dp else 1.dp, borderColor),
              modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isAnswerSubmitted) {
                  selectedAnswerIndex = index
                }
                .testTag("quiz_option_$index")
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = CircleShape,
                  color = if (isSelected || (isAnswerSubmitted && isCorrect)) borderColor else Color(0xFFF1F5F9),
                  modifier = Modifier.size(34.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = optionLetter,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected || (isAnswerSubmitted && isCorrect)) Color.White else ChurchDarkBlue
                    )
                  }
                }

                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = text,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isSelected || (isAnswerSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                  color = textColor,
                  modifier = Modifier.weight(1f)
                )

                if (isAnswerSubmitted) {
                  if (isCorrect) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = ChurchGreen, modifier = Modifier.size(24.dp))
                  } else if (isSelected) {
                    Icon(Icons.Default.Cancel, contentDescription = "Incorrect", tint = ChurchRed, modifier = Modifier.size(24.dp))
                  }
                }
              }
            }
          }

          // Explanation box when answered
          if (isAnswerSubmitted) {
            item {
              val wasCorrect = selectedAnswerIndex == currentQuestion.correctOptionIndex
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (wasCorrect) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)),
                border = BorderStroke(1.dp, if (wasCorrect) ChurchGreen.copy(alpha = 0.4f) else ChurchAmber.copy(alpha = 0.4f))
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = if (wasCorrect) Icons.Default.Verified else Icons.Default.Info,
                      contentDescription = null,
                      tint = if (wasCorrect) ChurchGreen else ChurchAmber,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = if (wasCorrect) "ትክክለኛ መልስ! / Correct Answer (+10 Pts)" else "ትምህርታዊ ማብራሪያ / Biblical Explanation:",
                      fontWeight = FontWeight.Bold,
                      color = if (wasCorrect) ChurchGreen else ChurchDarkGold,
                      fontSize = 12.sp
                    )
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = currentQuestion.explanationAmharic,
                    style = MaterialTheme.typography.bodySmall,
                    color = ChurchTextPrimary,
                    lineHeight = 18.sp
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = currentQuestion.explanationEnglish,
                    style = MaterialTheme.typography.labelSmall,
                    color = ChurchTextSecondary,
                    lineHeight = 16.sp
                  )
                }
              }
            }
          }

          // Action Button (Submit or Next)
          item {
            Spacer(modifier = Modifier.height(8.dp))
            if (!isAnswerSubmitted) {
              Button(
                onClick = {
                  if (selectedAnswerIndex != null) {
                    isAnswerSubmitted = true
                    if (selectedAnswerIndex == currentQuestion.correctOptionIndex) {
                      correctAnswersCount++
                    }
                  }
                },
                enabled = selectedAnswerIndex != null,
                colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("submit_answer_button")
              ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("መልስህን አረጋግጥ / Submit Answer", fontWeight = FontWeight.Bold)
              }
            } else {
              val isLast = safeQuestionIndex == storyQuestions.size - 1
              Button(
                onClick = {
                  if (isLast) {
                    isQuizCompleted = true
                  } else {
                    currentQuestionIndex++
                    selectedAnswerIndex = null
                    isAnswerSubmitted = false
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChurchGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("next_question_button")
              ) {
                Text(
                  text = if (isLast) "ፈተናውን አጠናቅቅ (Complete Quiz)" else "ቀጣይ ጥያቄ (Next Question)",
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
              }
            }
          }
        }
      } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("No questions found for this topic.", color = ChurchTextMuted)
        }
      }
    }
  }

  // Quiz History Dialog
  if (showHistoryDialog) {
    AlertDialog(
      onDismissRequest = { showHistoryDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Leaderboard, contentDescription = null, tint = ChurchDeepBlue)
          Spacer(modifier = Modifier.width(8.dp))
          Text("የተማሪዎች የፈተና ውጤት መዝገብ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Scores saved in Room Database (ውጤቶች በሰንበት ት/ቤት መዝገብ):",
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextMuted
          )
          Spacer(modifier = Modifier.height(10.dp))
          if (quizResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
              Text("No quiz attempts recorded yet. Take a quiz to record your first score!", fontSize = 11.sp, color = ChurchTextSecondary)
            }
          } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(260.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              items(quizResults) { result ->
                Card(
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(result.storyTitle, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ChurchDarkBlue, maxLines = 1)
                      Text("ተማሪ: ${result.participantName} • ${result.formattedDate}", fontSize = 9.sp, color = ChurchTextSecondary)
                    }
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = if (result.percentage >= 80) ChurchGreen.copy(alpha = 0.15f) else ChurchAmber.copy(alpha = 0.15f)
                    ) {
                      Text(
                        text = "${result.score}/${result.totalQuestions} (${result.percentage.toInt()}%)",
                        fontWeight = FontWeight.Bold,
                        color = if (result.percentage >= 80) ChurchGreen else ChurchDarkGold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showHistoryDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue)
        ) {
          Text("Close")
        }
      }
    )
  }
}

@Composable
private fun QuizCompletedView(
  storyTitle: String,
  score: Int,
  totalQuestions: Int,
  participantName: String,
  hasSaved: Boolean,
  onSaveResult: () -> Unit,
  onRetake: () -> Unit,
  onViewHistory: () -> Unit
) {
  val percentage = if (totalQuestions > 0) (score * 100 / totalQuestions) else 0
  val (badgeTitle, badgeDesc, badgeColor) = when {
    percentage >= 100 -> Triple("ሊቀ መዘምራን (Biblical Scholar)", "ድንቅ እውቀት! ቃለ ሕይወትን ያሰማልን። ፍጹም ውጤት!", ChurchGreen)
    percentage >= 50 -> Triple("ተግሣጽን የሚወድድ ተማሪ (Diligent Student)", "ጥሩ ውጤት! በዕለቱ የመጽሐፍ ቅዱስ ንባብ ቀጥሉ።", ChurchGold)
    else -> Triple("ተስፋ ያለው አገልጋይ (Faithful Inquirer)", "የዕለቱን ታሪክ ደግመው ያንብቡና እንደገና ይሞክሩ።", ChurchDeepBlue)
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(10.dp))
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = CircleShape,
            color = badgeColor.copy(alpha = 0.15f),
            modifier = Modifier.size(70.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (percentage >= 80) Icons.Default.EmojiEvents else Icons.Default.Star,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(38.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "ፈተናውን አጠናቀዋል!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = ChurchDarkBlue
          )
          Text(
            text = storyTitle,
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = ChurchPaleGold,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "$score / $totalQuestions",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ChurchDarkBlue
              )
              Text(
                text = "የውጤት መጠን: $percentage%",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChurchDarkGold
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = badgeTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = badgeColor
          )
          Text(
            text = badgeDesc,
            style = MaterialTheme.typography.bodySmall,
            color = ChurchTextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }

    item {
      // Save Score Button
      Button(
        onClick = onSaveResult,
        enabled = !hasSaved,
        colors = ButtonDefaults.buttonColors(containerColor = ChurchDeepBlue),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("save_quiz_score_btn")
      ) {
        Icon(if (hasSaved) Icons.Default.Check else Icons.Default.Save, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (hasSaved) "በመዝገብ ተቀምጧል (Saved to DB)" else "ውጤቴን በዳታቤዝ መዝግብ (Save Score to Room DB)")
      }
    }

    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
          onClick = onRetake,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("በድጋሚ ፈትን")
        }
        OutlinedButton(
          onClick = onViewHistory,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("ውጤቶች")
        }
      }
    }
  }
}
