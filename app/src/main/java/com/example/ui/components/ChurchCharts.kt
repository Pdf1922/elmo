package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class BarChartItem(val label: String, val value: Float, val color: Color)
data class PieChartSlice(val label: String, val value: Float, val color: Color)

@Composable
fun SimpleBarChart(
  title: String,
  subtitle: String = "",
  items: List<BarChartItem>,
  modifier: Modifier = Modifier,
  valueFormatter: (Float) -> String = { it.toInt().toString() }
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ChurchTextPrimary
      )
      if (subtitle.isNotEmpty()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = ChurchTextMuted,
          modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )
      } else {
        Spacer(modifier = Modifier.height(12.dp))
      }

      val maxValue = (items.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
      var animatedProgress by remember { mutableStateOf(0f) }
      LaunchedEffect(items) {
        animatedProgress = 1f
      }
      val progress by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 800),
        label = "barAnimation"
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
      ) {
        items.forEach { item ->
          val barHeightFraction = (item.value / maxValue).coerceIn(0.05f, 1f) * progress
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
          ) {
            Text(
              text = valueFormatter(item.value),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              fontSize = 10.sp,
              color = ChurchTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
              modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(barHeightFraction)
                .background(
                  brush = Brush.verticalGradient(
                    colors = listOf(item.color, item.color.copy(alpha = 0.75f))
                  ),
                  shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = item.label,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = ChurchTextSecondary,
              maxLines = 1
            )
          }
        }
      }
    }
  }
}

@Composable
fun DonutDistributionChart(
  title: String,
  slices: List<PieChartSlice>,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ChurchTextPrimary
      )
      Spacer(modifier = Modifier.height(12.dp))

      val total = (slices.sumOf { it.value.toDouble() }).toFloat().coerceAtLeast(1f)

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        // Donut Canvas
        Box(
          modifier = Modifier.size(110.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.size(100.dp)) {
            var startAngle = -90f
            slices.forEach { slice ->
              val sweepAngle = (slice.value / total) * 360f
              drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx())
              )
              startAngle += sweepAngle
            }
          }
          Text(
            text = "${total.toInt()}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ChurchDeepBlue
          )
        }

        // Legend
        Column(
          modifier = Modifier.padding(start = 12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          slices.forEach { slice ->
            val percentage = ((slice.value / total) * 100).toInt()
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .background(slice.color, shape = CircleShape)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "${slice.label}: ${slice.value.toInt()} ($percentage%)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = ChurchTextSecondary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun SimpleLineTrendChart(
  title: String,
  subtitle: String = "",
  values: List<Float>,
  labels: List<String>,
  lineColor: Color = ChurchDeepBlue,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ChurchTextPrimary
      )
      if (subtitle.isNotEmpty()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = ChurchTextMuted,
          modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )
      } else {
        Spacer(modifier = Modifier.height(12.dp))
      }

      val maxVal = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
      val minVal = (values.minOrNull() ?: 0f)

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          if (values.size < 2) return@Canvas
          val stepX = size.width / (values.size - 1)
          val range = (maxVal - minVal).coerceAtLeast(1f)

          val points = values.mapIndexed { index, v ->
            val x = index * stepX
            val y = size.height - ((v - minVal) / range) * (size.height - 20.dp.toPx()) - 10.dp.toPx()
            Offset(x, y)
          }

          val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
              lineTo(points[i].x, points[i].y)
            }
          }

          drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
          )

          points.forEach { pt ->
            drawCircle(
              color = ChurchGold,
              radius = 4.dp.toPx(),
              center = pt
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        labels.forEach { lbl ->
          Text(
            text = lbl,
            style = MaterialTheme.typography.labelSmall,
            color = ChurchTextMuted,
            fontSize = 10.sp
          )
        }
      }
    }
  }
}

@Composable
fun ComparisonBarChart(
  title: String,
  firstLabel: String,
  firstValue: Float,
  firstColor: Color,
  secondLabel: String,
  secondValue: Float,
  secondColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ChurchTextPrimary
      )
      Spacer(modifier = Modifier.height(12.dp))

      val total = (firstValue + secondValue).coerceAtLeast(1f)
      val firstPct = ((firstValue / total) * 100).toInt()
      val secondPct = 100 - firstPct

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(20.dp)
          .background(Color(0xFFE2E8F0), shape = RoundedCornerShape(10.dp))
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .weight((firstValue.coerceAtLeast(0.1f) / total))
            .background(firstColor, shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
        )
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .weight((secondValue.coerceAtLeast(0.1f) / total))
            .background(secondColor, shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(10.dp).background(firstColor, CircleShape))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "$firstLabel: ${firstValue.toInt()} ($firstPct%)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(10.dp).background(secondColor, CircleShape))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "$secondLabel: ${secondValue.toInt()} ($secondPct%)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}
