package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.Rotating3DSNCrystalLogo
import com.example.ui.theme.LocalExtendedTheme
import kotlinx.coroutines.delay
import kotlin.math.sin

private data class NeonParticle(
    val xFraction: Float,
    val baseYFraction: Float,
    val radiusDp: Float,
    val speedFactor: Float,
    val alphaBase: Float
)

@Composable
fun SplashScreen(
    enable3DMotion: Boolean,
    onSplashFinished: () -> Unit
) {
    val extended = LocalExtendedTheme.current
    var showBrandingText by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }

    val particles = remember {
        listOf(
            NeonParticle(0.14f, 0.22f, 3.2f, 1.0f, 0.45f),
            NeonParticle(0.84f, 0.18f, 4.0f, 0.8f, 0.50f),
            NeonParticle(0.24f, 0.72f, 2.8f, 1.2f, 0.38f),
            NeonParticle(0.78f, 0.68f, 3.6f, 0.9f, 0.48f),
            NeonParticle(0.50f, 0.14f, 2.5f, 1.3f, 0.35f),
            NeonParticle(0.10f, 0.50f, 3.0f, 1.1f, 0.40f),
            NeonParticle(0.90f, 0.46f, 3.4f, 0.7f, 0.42f),
            NeonParticle(0.36f, 0.84f, 2.6f, 1.4f, 0.36f),
            NeonParticle(0.66f, 0.82f, 3.1f, 1.0f, 0.44f)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_particles")
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particlePhase"
    )

    LaunchedEffect(Unit) {
        delay(220L)
        showBrandingText = true
        delay(2150L)
        if (!hasNavigated) {
            hasNavigated = true
            onSplashFinished()
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val bgTop = if (extended.isNeonDark) Color(0xFF140826) else MaterialTheme.colorScheme.background
    val bgBottom = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(bgTop, bgBottom)
                )
            )
            .clickable {
                if (!hasNavigated) {
                    hasNavigated = true
                    onSplashFinished()
                }
            }
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient neon particles & perspective horizon glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEachIndexed { index, p ->
                val driftY = ((p.baseYFraction - particlePhase * p.speedFactor * 0.35f) + 1f) % 1f
                val swayX = p.xFraction + 0.025f * sin((particlePhase * 6.28f) + index)
                val color = if (index % 2 == 0) primary else tertiary
                drawCircle(
                    color = color.copy(alpha = p.alphaBase),
                    radius = p.radiusDp.dp.toPx(),
                    center = Offset(swayX * size.width, driftY * size.height)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Rotating3DSNCrystalLogo(
                modifier = Modifier
                    .size(210.dp)
                    .testTag("splash_3d_logo"),
                enable3DMotion = enable3DMotion,
                showFloorReflection = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = showBrandingText,
                enter = fadeIn(tween(650, easing = FastOutSlowInEasing)) +
                    slideInVertically(tween(650, easing = FastOutSlowInEasing)) { it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.welcome_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Next-Gen Gaming, AI Prompts & Voice Studio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    LinearProgressIndicator(
                        modifier = Modifier
                            .widthIn(max = 180.dp)
                            .fillMaxWidth(0.55f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}
