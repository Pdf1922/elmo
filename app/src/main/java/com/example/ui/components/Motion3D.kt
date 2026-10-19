package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.ChurchGold
import com.example.ui.theme.ChurchLightGold

/**
 * 3D Tilt and Perspective modifier.
 * Reacts to touch gestures by tilting along the X and Y axes with perspective depth.
 */
fun Modifier.motion3DTilt(
  maxTiltAngle: Float = 12f,
  scaleOnPress: Float = 0.98f
): Modifier = this.then(
  Modifier.composed {
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }
    var isPressed by remember { mutableStateOf(false) }

    val animatedTiltX by animateFloatAsState(
      targetValue = tiltX,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "tiltX"
    )
    val animatedTiltY by animateFloatAsState(
      targetValue = tiltY,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "tiltY"
    )
    val animatedScale by animateFloatAsState(
      targetValue = if (isPressed) scaleOnPress else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessMedium),
      label = "scale"
    )

    this
      .graphicsLayer {
        rotationX = animatedTiltX
        rotationY = animatedTiltY
        scaleX = animatedScale
        scaleY = animatedScale
        cameraDistance = 14f * density
      }
      .pointerInput(Unit) {
        detectTapGestures(
          onPress = { offset ->
            isPressed = true
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val normX = (offset.x - centerX) / centerX
            val normY = (offset.y - centerY) / centerY

            tiltY = normX.coerceIn(-1f, 1f) * maxTiltAngle
            tiltX = -normY.coerceIn(-1f, 1f) * maxTiltAngle

            tryAwaitRelease()
            isPressed = false
            tiltX = 0f
            tiltY = 0f
          }
        )
      }
  }
)

/**
 * 3D Card with tangible physical depth, edge highlights, and tactile response.
 */
@Composable
fun Card3D(
  modifier: Modifier = Modifier,
  shape: RoundedCornerShape = RoundedCornerShape(16.dp),
  elevation: Dp = 6.dp,
  backgroundColor: Color = MaterialTheme.colorScheme.surface,
  borderColor: Color = ChurchLightGold.copy(alpha = 0.35f),
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = modifier
      .motion3DTilt(maxTiltAngle = 10f)
      .shadow(elevation, shape, ambientColor = ChurchGold.copy(alpha = 0.2f), spotColor = Color.Black.copy(alpha = 0.25f))
      .border(1.dp, borderColor, shape),
    shape = shape,
    colors = CardDefaults.cardColors(containerColor = backgroundColor)
  ) {
    content()
  }
}

/**
 * Continuous 3D oscillating Church Emblem with lighting glare and golden ring aura.
 */
@Composable
fun ChurchLogo3D(
  modifier: Modifier = Modifier,
  size: Dp = 72.dp,
  continuousRotation: Boolean = true
) {
  val infiniteTransition = rememberInfiniteTransition(label = "3DLogoRotation")
  val rotationY by if (continuousRotation) {
    infiniteTransition.animateFloat(
      initialValue = -15f,
      targetValue = 15f,
      animationSpec = infiniteRepeatable(
        animation = tween(2800, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "rotY"
    )
  } else {
    remember { mutableStateOf(0f) }
  }

  val shineOffset by infiniteTransition.animateFloat(
    initialValue = -100f,
    targetValue = 200f,
    animationSpec = infiniteRepeatable(
      animation = tween(3200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shine"
  )

  Box(
    modifier = modifier
      .size(size)
      .graphicsLayer {
        this.rotationY = rotationY
        cameraDistance = 12f * density
      }
      .shadow(8.dp, CircleShape, ambientColor = ChurchGold)
      .background(
        brush = Brush.radialGradient(
          colors = listOf(ChurchLightGold, ChurchGold, Color(0xFF8C6D1F))
        ),
        shape = CircleShape
      )
      .padding(3.dp),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(id = R.drawable.church_official_logo),
      contentDescription = "Official Church Emblem 3D",
      modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape),
      contentScale = ContentScale.Crop
    )

    // Holographic glare overlay
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
        .background(
          brush = Brush.linearGradient(
            colors = listOf(
              Color.Transparent,
              Color.White.copy(alpha = 0.25f),
              Color.Transparent
            ),
            start = Offset(shineOffset, 0f),
            end = Offset(shineOffset + 60f, shineOffset + 60f)
          )
        )
    )
  }
}
