package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleStory
import com.example.data.model.BibleStoryRepository
import com.example.ui.components.Card3D
import com.example.ui.components.ChurchLogo3D
import com.example.ui.components.ChurchTopAppBar
import com.example.ui.components.motion3DTilt
import com.example.ui.theme.*
import com.example.ui.viewmodel.SundaySchoolViewModel
import com.example.util.BibleReminderManager
import com.example.util.DataExporter
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleStoryScreen(
  viewModel: SundaySchoolViewModel? = null,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val emptyStoriesList = remember { emptyList<BibleStory>() }
  val dbStories by (viewModel?.bibleStories?.collectAsState() ?: remember { mutableStateOf(emptyStoriesList) })
  val stories = remember(dbStories) {
    if (dbStories.isNotEmpty()) dbStories else BibleStoryRepository.dailyStories
  }
  var selectedIndex by remember { mutableIntStateOf(0) }
  val safeIndex = selectedIndex.coerceIn(0, (stories.size - 1).coerceAtLeast(0))
  val currentStory = if (stories.isNotEmpty()) stories[safeIndex] else BibleStoryRepository.dailyStories[0]

  var languageMode by remember { mutableIntStateOf(0) } // 0: Both, 1: Amharic, 2: English
  var isPlayingAudio by remember { mutableStateOf(false) }
  var audioProgress by remember { mutableFloatStateOf(0f) }

  // WorkManager reminder toggle states
  var isMorningReminderEnabled by remember { mutableStateOf(BibleReminderManager.isMorningEnabled(context)) }
  var isEveningReminderEnabled by remember { mutableStateOf(BibleReminderManager.isEveningEnabled(context)) }
  var isTakingQuiz by remember { mutableStateOf(false) }

  if (isTakingQuiz && viewModel != null) {
    BibleQuizScreen(
      viewModel = viewModel,
      initialStoryId = currentStory.id,
      onNavigateBack = { isTakingQuiz = false }
    )
    return
  }

  // Simulate audio narrative playback
  LaunchedEffect(isPlayingAudio) {
    if (isPlayingAudio) {
      while (audioProgress < 1f) {
        delay(300)
        audioProgress += 0.05f
      }
      isPlayingAudio = false
      audioProgress = 0f
    }
  }

  Scaffold(
    topBar = {
      ChurchTopAppBar(
        title = "ዕለታዊ የመጽሐፍ ቅዱስ ታሪክ",
        subtitle = "Daily Biblical Stories (Amharic & English)",
        onBackClick = onNavigateBack,
        actions = {
          IconButton(onClick = { isTakingQuiz = true }) {
            Icon(Icons.Default.Quiz, contentDescription = "Take Quiz", tint = ChurchGold)
          }
          IconButton(onClick = {
            val shareContent = """
              📖 ${currentStory.titleAmharic} / ${currentStory.titleEnglish}
              
              ጥቅስ: ${currentStory.verseReference}
              ${currentStory.verseTextAmharic}
              
              ትምህርት: ${currentStory.moralAmharic}
              
              ደብረ ኃይል መሠረተ ሕይወት ሰንበት ት/ቤት
            """.trimIndent()
            DataExporter.shareText(context, currentStory.titleEnglish, shareContent)
          }) {
            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
          }
        }
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(ChurchIvory)
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 6:30 AM & 6:30 PM Reminder Card with quick trigger
      item {
        Card3D(
          backgroundColor = ChurchDeepBlue,
          borderColor = ChurchGold
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              ChurchLogo3D(size = 40.dp, continuousRotation = true)
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "የዕለት ጸሎትና የመጽሐፍ ቅዱስ ንባብ ማስታወሻ",
                  color = ChurchLightGold,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                Text(
                  text = "Daily Reminder at 6:30 AM & 6:30 PM",
                  color = Color.White.copy(alpha = 0.85f),
                  fontSize = 11.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = { BibleReminderManager.triggerMorningNotification(context) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChurchLightGold),
                border = androidx.compose.foundation.BorderStroke(1.dp, ChurchGold),
                modifier = Modifier
                  .weight(1f)
                  .testTag("test_morning_reminder_btn")
              ) {
                Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("6:30 AM Test", fontSize = 11.sp)
              }

              Button(
                onClick = { BibleReminderManager.triggerEveningNotification(context) },
                colors = ButtonDefaults.buttonColors(containerColor = ChurchGold, contentColor = ChurchDarkBlue),
                modifier = Modifier
                  .weight(1f)
                  .testTag("test_evening_reminder_btn")
              ) {
                Icon(Icons.Default.NightsStay, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("6:30 PM Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Story Selector Pills
      item {
        Column {
          Text(
            text = "ይምረጡ / Choose Story of the Day:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ChurchTextSecondary
          )
          Spacer(modifier = Modifier.height(6.dp))
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            itemsIndexed(stories) { index, story ->
              val isSelected = selectedIndex == index
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) ChurchDeepBlue else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) ChurchGold else ChurchPaleGold
                ),
                modifier = Modifier
                  .clickable {
                    selectedIndex = index
                    isPlayingAudio = false
                    audioProgress = 0f
                  }
                  .motion3DTilt(maxTiltAngle = 8f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    shape = CircleShape,
                    color = if (isSelected) ChurchGold else ChurchDeepBlue,
                    modifier = Modifier.size(22.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = "${story.dayNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) ChurchDarkBlue else Color.White
                      )
                    }
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = story.titleAmharic.take(18) + "...",
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else ChurchTextPrimary
                  )
                }
              }
            }
          }
        }
      }

      // Language Switcher (Amharic / Both / English)
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          listOf("ሁለቱም (Both)", "አማርኛ", "English").forEachIndexed { index, label ->
            FilterChip(
              selected = languageMode == index,
              onClick = { languageMode = index },
              label = { Text(label, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ChurchDeepBlue,
                selectedLabelColor = ChurchLightGold
              ),
              modifier = Modifier.padding(horizontal = 3.dp)
            )
          }
        }
      }

      // Main Story Display Card (with 3D depth)
      item {
        Card3D(
          backgroundColor = Color.White,
          borderColor = ChurchLightGold
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Header with badge
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = ChurchPaleGold,
                modifier = Modifier.padding(end = 8.dp)
              ) {
                Text(
                  text = "DAY ${currentStory.dayNumber}",
                  color = ChurchDarkBlue,
                  fontWeight = FontWeight.Black,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
              Text(
                text = currentStory.liturgicalConnection,
                fontSize = 11.sp,
                color = ChurchTextMuted,
                modifier = Modifier.weight(1f)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            if (languageMode == 0 || languageMode == 1) {
              Text(
                text = currentStory.titleAmharic,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = ChurchDeepBlue,
                lineHeight = 23.sp
              )
            }
            if (languageMode == 0 || languageMode == 2) {
              Text(
                text = currentStory.titleEnglish,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChurchTextSecondary,
                modifier = Modifier.padding(top = 4.dp)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scripture Verse Box
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = ChurchDeepBlue.copy(alpha = 0.05f),
              border = androidx.compose.foundation.BorderStroke(1.dp, ChurchGold.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.MenuBook, contentDescription = null, tint = ChurchDeepBlue, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = currentStory.verseReference,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ChurchDeepBlue
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (languageMode == 0 || languageMode == 1) {
                  Text(
                    text = currentStory.verseTextAmharic,
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = ChurchTextPrimary
                  )
                }
                if (languageMode == 0 || languageMode == 2) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = currentStory.verseTextEnglish,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = ChurchTextSecondary
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Audio Player Bar
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = ChurchPaleGold,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                IconButton(
                  onClick = { isPlayingAudio = !isPlayingAudio },
                  modifier = Modifier
                    .size(36.dp)
                    .background(ChurchDeepBlue, CircleShape)
                ) {
                  Icon(
                    imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play Audio",
                    tint = ChurchGold,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (isPlayingAudio) "ድምፅ እያዳመጡ ነው... / Playing Audio Narration" else "ታሪኩን በድምፅ ያዳምጡ / Listen to Narration",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChurchDarkBlue
                  )
                  LinearProgressIndicator(
                    progress = { audioProgress },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(4.dp)
                      .clip(RoundedCornerShape(2.dp)),
                    color = ChurchDeepBlue,
                    trackColor = ChurchGold.copy(alpha = 0.3f)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Story Narrative
            if (languageMode == 0 || languageMode == 1) {
              Text(
                text = "የታሪኩ ዝርዝር፦",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ChurchDeepBlue
              )
              Text(
                text = currentStory.storyAmharic,
                fontSize = 13.sp,
                lineHeight = 21.sp,
                color = ChurchTextPrimary,
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }

            if (languageMode == 0) {
              Divider(modifier = Modifier.padding(vertical = 10.dp), color = ChurchPaleGold)
            }

            if (languageMode == 0 || languageMode == 2) {
              Text(
                text = "Narrative Story:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ChurchDeepBlue
              )
              Text(
                text = currentStory.storyEnglish,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = ChurchTextPrimary,
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Moral / Spiritual Reflection
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = ChurchDeepBlue,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Lightbulb, contentDescription = null, tint = ChurchGold, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "መንፈሳዊ ምሥጢርና ትምህርት / Spiritual Reflection",
                    color = ChurchGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (languageMode == 0 || languageMode == 1) {
                  Text(
                    text = currentStory.moralAmharic,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                  )
                }
                if (languageMode == 0 || languageMode == 2) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = currentStory.moralEnglish,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daily Closing Prayer
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = ChurchAmberBg,
              border = androidx.compose.foundation.BorderStroke(1.dp, ChurchGold.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Church, contentDescription = null, tint = ChurchDeepBlue, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "የዕለቱ ጸሎት / Daily Prayer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ChurchDeepBlue
                  )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (languageMode == 0 || languageMode == 1) {
                  Text(
                    text = currentStory.prayerAmharic,
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = ChurchTextPrimary,
                    lineHeight = 22.sp
                  )
                }
                if (languageMode == 0 || languageMode == 2) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = currentStory.prayerEnglish,
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = ChurchTextSecondary,
                    lineHeight = 21.sp
                  )
                }
              }
            }
          }
        }
      }

      // Interactive Bible Quiz Invitation Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ChurchPaleGold),
          border = androidx.compose.foundation.BorderStroke(1.dp, ChurchGold),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isTakingQuiz = true }
            .testTag("start_daily_quiz_card")
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = ChurchDeepBlue,
              modifier = Modifier.size(48.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Quiz,
                  contentDescription = null,
                  tint = ChurchGold,
                  modifier = Modifier.size(26.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "የዕለቱ የመጽሐፍ ቅዱስ ጥያቄና መልስ",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ChurchDarkBlue
              )
              Text(
                text = "Daily Interactive Reading Quiz • ፈትነው ውጤትዎን ይመዝግቡ",
                style = MaterialTheme.typography.bodySmall,
                color = ChurchTextSecondary
              )
            }
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = ChurchDeepBlue, modifier = Modifier.size(16.dp))
          }
        }
      }

      // WorkManager Daily Reading Reminders Management Card
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = ChurchSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, ChurchBorder),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = ChurchGold,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "ዕለታዊ የንባብ ማሳሰቢያዎች (Daily Reminders)",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = ChurchTextPrimary
                )
                Text(
                  text = "WorkManager Scheduled at 6:30 AM & 6:30 PM",
                  style = MaterialTheme.typography.bodySmall,
                  color = ChurchTextMuted
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = ChurchGreenBg
              ) {
                Text(
                  text = "⚡ Active",
                  color = ChurchGreen,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6:30 AM Morning Toggle
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "🌅 6:30 AM • የጧት መጽሐፍ ቅዱስ ንባብ",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = ChurchTextPrimary
                )
                Text(
                  text = "Daily morning scripture verse & story notification",
                  style = MaterialTheme.typography.bodySmall,
                  color = ChurchTextSecondary
                )
              }
              Switch(
                checked = isMorningReminderEnabled,
                onCheckedChange = {
                  isMorningReminderEnabled = it
                  BibleReminderManager.setMorningEnabled(context, it)
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = ChurchDeepBlue
                )
              )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = ChurchBorder)

            // 6:30 PM Evening Toggle
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "🌆 6:30 PM • የምሽት ጸሎትና ንባብ",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = ChurchTextPrimary
                )
                Text(
                  text = "Daily evening prayer & moral reflection notification",
                  style = MaterialTheme.typography.bodySmall,
                  color = ChurchTextSecondary
                )
              }
              Switch(
                checked = isEveningReminderEnabled,
                onCheckedChange = {
                  isEveningReminderEnabled = it
                  BibleReminderManager.setEveningEnabled(context, it)
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = ChurchDeepBlue
                )
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // WorkManager Test Trigger Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  BibleReminderManager.triggerMorningNotification(context)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChurchDeepBlue)
              ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Test 6:30 AM", fontSize = 11.sp)
              }

              OutlinedButton(
                onClick = {
                  BibleReminderManager.triggerEveningNotification(context)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChurchGold)
              ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Test 6:30 PM", fontSize = 11.sp)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Room Database Offline Cache Status
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ChurchAmberBg,
              border = androidx.compose.foundation.BorderStroke(1.dp, ChurchBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Storage,
                  contentDescription = null,
                  tint = ChurchGold,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "💾 Room Database: ${stories.size} Stories Cached for Offline Access",
                  fontSize = 11.sp,
                  color = ChurchTextSecondary,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
