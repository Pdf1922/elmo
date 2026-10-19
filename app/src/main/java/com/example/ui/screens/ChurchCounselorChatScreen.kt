package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import com.example.ai.ChatMessage
import com.example.ai.ChurchAiCounselor
import com.example.ai.MessageSender
import com.example.ui.components.ChurchLogo3D
import com.example.ui.components.ChurchTopAppBar
import com.example.ui.components.motion3DTilt
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChurchCounselorChatScreen(
  onNavigateBack: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  var inputText by remember { mutableStateOf("") }
  var isGenerating by remember { mutableStateOf(false) }

  val messages = remember {
    mutableStateListOf(
      ChatMessage(
        id = "welcome_1",
        sender = MessageSender.COUNSELOR,
        text = "በስመ አብ ወወልድ ወመንፈስ ቅዱስ አሐዱ አምላክ አሜን።\n\nየተወደድክ/ሽ የሰንበት ት/ቤታችን አባል እንኳን ደኅና መጣህ/ሽ! እኔ መምህር ደብረ ኃይል (የመንፈሳዊ ምክርና ውይይት ረዳትህ) ነኝ። ስለ መንፈሳዊ ሕይወትህ፣ ስለ ጸሎትና ንስሐ፣ ስለ መጽሐፍ ቅዱስ ምስጢር ወይም ስለ ሰንበት ት/ቤታችን አሠራር ማንኛውንም ነገር ጠይቀኝ። እግዚአብሔር ማስተዋሉን ያድለን!",
        scriptureCitation = "«ምክርህን በእግዚአብሔር ላይ አኑር፥ እርሱም ይደግፍሃል» (መዝ 55፥22)"
      )
    )
  }

  val suggestedQueries = listOf(
    "ስለ ንስሐ ንገረኝ",
    "የጧት 6:30 ጸሎት",
    "የቅዱስ ገብርኤል ተራዳኢነት",
    "የወርሃዊ መዋጮ ምድቦች",
    "ቅድስት አርሴማ",
    "የ7ቱ ክፍሎች ዝርዝር"
  )

  fun sendMessage(query: String) {
    if (query.isBlank() || isGenerating) return

    val userMessage = ChatMessage(
      id = UUID.randomUUID().toString(),
      sender = MessageSender.USER,
      text = query
    )
    messages.add(userMessage)
    inputText = ""
    isGenerating = true

    coroutineScope.launch {
      listState.animateScrollToItem(messages.size - 1)

      val botMessageId = UUID.randomUUID().toString()
      val botMessage = ChatMessage(
        id = botMessageId,
        sender = MessageSender.COUNSELOR,
        text = "..."
      )
      messages.add(botMessage)

      ChurchAiCounselor.getRealisticStreamResponse(query).collectLatest { streamedText ->
        val index = messages.indexOfFirst { it.id == botMessageId }
        if (index != -1) {
          messages[index] = botMessage.copy(text = streamedText)
          listState.scrollToItem(messages.size - 1)
        }
      }
      isGenerating = false
    }
  }

  Scaffold(
    topBar = {
      ChurchTopAppBar(
        title = "መምህር ደብረ ኃይል / Spiritual AI",
        subtitle = "Orthodox Theological Counselor",
        onBackClick = onNavigateBack,
        actions = {
          IconButton(onClick = {
            messages.clear()
            messages.add(
              ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.COUNSELOR,
                text = "በስመ አብ ወወልድ ወመንፈስ ቅዱስ አሐዱ አምላክ አሜን። ውይይታችን ታድሷል። እባክህ ጥያቄህን አቅርብ።"
              )
            )
          }) {
            Icon(Icons.Default.Refresh, contentDescription = "Clear Chat", tint = Color.White)
          }
        }
      )
    },
    bottomBar = {
      Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          // Suggested Quick Chips
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            suggestedQueries.forEach { chipText ->
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = ChurchPaleGold,
                border = androidx.compose.foundation.BorderStroke(1.dp, ChurchGold.copy(alpha = 0.5f)),
                modifier = Modifier.clickable { sendMessage(chipText) }
              ) {
                Text(
                  text = chipText,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = ChurchDarkBlue,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Input Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = inputText,
              onValueChange = { inputText = it },
              placeholder = { Text("ጥያቄዎን ወይም የመንፈስ ጭንቀትዎን ይጻፉ...", fontSize = 13.sp) },
              modifier = Modifier
                .weight(1f)
                .testTag("ai_chat_input"),
              shape = RoundedCornerShape(24.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ChurchDeepBlue,
                unfocusedBorderColor = ChurchGold.copy(alpha = 0.5f)
              ),
              maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
              onClick = { sendMessage(inputText) },
              containerColor = ChurchDeepBlue,
              contentColor = ChurchGold,
              shape = CircleShape,
              modifier = Modifier
                .size(46.dp)
                .testTag("send_ai_chat_button")
            ) {
              if (isGenerating) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  color = ChurchGold,
                  strokeWidth = 2.dp
                )
              } else {
                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
              }
            }
          }
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(ChurchIvory)
    ) {
      // Counselor 3D Presence Header
      Surface(
        color = ChurchDeepBlue,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          ChurchLogo3D(size = 46.dp, continuousRotation = true)
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "መምህር ደብረ ኃይል (የሰንበት ት/ቤት መካሪ)",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .background(Color(0xFF4CAF50), CircleShape)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isGenerating) "እየመለሰ ነው... / Typing..." else "በአገልግሎት ላይ • Active Pastoral Counseling",
                color = ChurchLightGold,
                fontSize = 11.sp
              )
            }
          }
        }
      }

      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .weight(1f)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(messages, key = { it.id }) { message ->
          ChatBubbleItem(message = message)
        }
      }
    }
  }
}

@Composable
fun ChatBubbleItem(message: ChatMessage) {
  val isUser = message.sender == MessageSender.USER
  val alignment = if (isUser) Alignment.End else Alignment.Start

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = alignment
  ) {
    Row(
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
      modifier = Modifier.fillMaxWidth(0.92f)
    ) {
      if (!isUser) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(ChurchGold)
            .padding(2.dp),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = ChurchDarkBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
      }

      Surface(
        shape = RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (isUser) 16.dp else 2.dp,
          bottomEnd = if (isUser) 2.dp else 16.dp
        ),
        color = if (isUser) ChurchDeepBlue else Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier
          .motion3DTilt(maxTiltAngle = 6f)
          .border(
            width = 1.dp,
            color = if (isUser) ChurchLightGold.copy(alpha = 0.3f) else ChurchGold.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp)
          )
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          if (!isUser) {
            Text(
              text = "መምህር ደብረ ኃይል",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = ChurchGold
            )
            Spacer(modifier = Modifier.height(4.dp))
          }

          Text(
            text = message.text,
            color = if (isUser) Color.White else ChurchTextPrimary,
            fontSize = 13.sp,
            lineHeight = 19.sp
          )

          if (message.scriptureCitation != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = ChurchPaleGold,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = message.scriptureCitation,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChurchDarkBlue,
                modifier = Modifier.padding(6.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
            fontSize = 9.sp,
            color = if (isUser) Color.White.copy(alpha = 0.6f) else ChurchTextMuted,
            modifier = Modifier.align(Alignment.End)
          )
        }
      }
    }
  }
}
