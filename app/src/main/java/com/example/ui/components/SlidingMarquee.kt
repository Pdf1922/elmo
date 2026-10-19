package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChurchDarkBlue
import com.example.ui.theme.ChurchDeepBlue
import com.example.ui.theme.ChurchGold
import com.example.ui.theme.ChurchLightGold
import kotlinx.coroutines.isActive

@Composable
fun ChurchSlidingTicker(
  messages: List<String> = listOf(
    "🔔 የቦሌ አረብሳ ደብረ ኃይል ቅዱስ ገብርኤልና ቅድስት አርሴማ መሠረተ ሕይወት ሰንበት ት/ቤት እንኳን ደህና መጡ! • Welcome to Meserete Hiwot Sunday School!",
    "📖 የዕለቱ የመጽሐፍ ቅዱስ ንባብ ማስታወሻ: ጧት 12:30 እና ማታ 12:30 • Daily Bible Reading Reminder at 6:30 AM & 6:30 PM",
    "✨ መስከረም 17 የመስቀል ደመራ በዓል ደማቅ የመዝሙር አገልግሎት ይኖራል • Feast of the Cross (Meskel) Choir Presentation on Meskerem 17",
    "💳 የወርሃዊ መዋጮ ክፍያዎን በቴሌብር / CBE Birr ይክፈሉ • Settle your monthly membership fees via Telebirr / CBE Birr",
    "🙏 'ሕግህ ለእግሬ መብራት ለመንገዴም ብርሃን ነው።' (መዝ 119:105) • 'Your word is a lamp to my feet and a light to my path.'"
  ),
  modifier: Modifier = Modifier,
  onItemClick: ((String) -> Unit)? = null
) {
  val fullText = remember(messages) { messages.joinToString("   ✦   ") }
  val density = LocalDensity.current
  var containerWidthPx by remember { mutableIntStateOf(0) }
  var textWidthPx by remember { mutableIntStateOf(0) }

  val transition = rememberInfiniteTransition(label = "tickerAnimation")

  val scrollTarget = if (textWidthPx > 0 && containerWidthPx > 0) {
    (textWidthPx + containerWidthPx).toFloat()
  } else {
    1000f
  }

  val duration = ((scrollTarget / 50f) * 1000).toInt().coerceAtLeast(18000)

  val animatedOffset by transition.animateFloat(
    initialValue = containerWidthPx.toFloat(),
    targetValue = -textWidthPx.toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = duration, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "tickerOffset"
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(38.dp)
      .clip(RoundedCornerShape(8.dp))
      .border(1.dp, ChurchGold.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
    color = ChurchDarkBlue
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Live Badge
      Box(
        modifier = Modifier
          .background(
            brush = Brush.horizontalGradient(
              listOf(ChurchGold, ChurchLightGold)
            )
          )
          .padding(horizontal = 8.dp, vertical = 6.dp)
          .fillMaxHeight(),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Campaign,
            contentDescription = "Announcement",
            tint = ChurchDarkBlue,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "NEWS / ዜና",
            color = ChurchDarkBlue,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
          )
        }
      }

      // Scrolling container
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .onSizeChanged { containerWidthPx = it.width },
        contentAlignment = Alignment.CenterStart
      ) {
        Text(
          text = fullText,
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          modifier = Modifier
            .offset { IntOffset(x = animatedOffset.toInt(), y = 0) }
            .onSizeChanged { textWidthPx = it.width }
            .clickable { onItemClick?.invoke(fullText) }
        )
      }
    }
  }
}
