package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalExtendedTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class Vec3(val x: Float, val y: Float, val z: Float)

/**
 * Genuine 3D perspective-projected crystalline octahedron + glowing SN emblem renderer.
 * Uses real 3D rotation matrices (rotateY + rotateX) and focal perspective projection,
 * optimized to run smoothly at 60fps on ordinary Android phones without heavy GL contexts.
 */
@Composable
fun Rotating3DSNCrystalLogo(
    modifier: Modifier = Modifier,
    enable3DMotion: Boolean = true,
    showFloorReflection: Boolean = true
) {
    val extended = LocalExtendedTheme.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "sn_3d_logo")
    val yawDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "yaw"
    )
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val vertices = remember {
        listOf(
            Vec3(0f, -1.25f, 0f),   // 0: Top apex
            Vec3(1.0f, 0f, 0f),     // 1: Right
            Vec3(0f, 0f, 1.0f),     // 2: Front
            Vec3(-1.0f, 0f, 0f),    // 3: Left
            Vec3(0f, 0f, -1.0f),    // 4: Back
            Vec3(0f, 1.25f, 0f)     // 5: Bottom apex
        )
    }

    val faces = remember {
        listOf(
            Triple(0, 1, 2),
            Triple(0, 2, 3),
            Triple(0, 3, 4),
            Triple(0, 4, 1),
            Triple(5, 2, 1),
            Triple(5, 3, 2),
            Triple(5, 4, 3),
            Triple(5, 1, 4)
        )
    }

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height * (if (showFloorReflection) 0.44f else 0.5f)
        val radius = size.minDimension * 0.31f

        // Outer neon aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.42f * pulseGlow),
                    tertiaryColor.copy(alpha = 0.16f * pulseGlow),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = radius * 1.85f
            ),
            radius = radius * 1.85f,
            center = Offset(cx, cy)
        )

        val yawRad = (if (enable3DMotion) yawDegrees else 28f) * (PI.toFloat() / 180f)
        val pitchRad = 18f * (PI.toFloat() / 180f)

        fun rotateAndProject(v: Vec3): Pair<Offset, Float> {
            // Rotate around Y
            val x1 = v.x * cos(yawRad) + v.z * sin(yawRad)
            val z1 = -v.x * sin(yawRad) + v.z * cos(yawRad)
            // Rotate around X
            val y2 = v.y * cos(pitchRad) - z1 * sin(pitchRad)
            val z2 = v.y * sin(pitchRad) + z1 * cos(pitchRad)

            val cameraDist = 3.8f
            val perspective = cameraDist / (cameraDist - z2)
            val px = cx + x1 * radius * perspective
            val py = cy + y2 * radius * perspective
            return Offset(px, py) to z2
        }

        val transformed = vertices.map { rotateAndProject(it) }

        // Painter's algorithm: sort faces back-to-front by average Z depth
        val sortedFaces = faces.map { (i0, i1, i2) ->
            val avgZ = (transformed[i0].second + transformed[i1].second + transformed[i2].second) / 3f
            Triple(Triple(i0, i1, i2), avgZ, i0 == 0)
        }.sortedBy { it.second }

        sortedFaces.forEach { (indices, avgZ, isUpper) ->
            val p0 = transformed[indices.first].first
            val p1 = transformed[indices.second].first
            val p2 = transformed[indices.third].first

            val path = Path().apply {
                moveTo(p0.x, p0.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                close()
            }

            val depthBrightness = ((avgZ + 1.2f) / 2.4f).coerceIn(0.15f, 1f)
            val faceFill = if (isUpper) {
                primaryColor.copy(alpha = 0.16f + 0.34f * depthBrightness)
            } else {
                tertiaryColor.copy(alpha = 0.12f + 0.28f * depthBrightness)
            }

            drawPath(path = path, color = faceFill)
            drawPath(
                path = path,
                color = extended.glowPrimary.copy(alpha = 0.45f + 0.45f * depthBrightness),
                style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round)
            )
        }

        // Crisp glowing "SN" core monogram Floating inside the 3D crystal
        val glyphScale = radius * 0.42f
        val snStroke = Stroke(
            width = 3.2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // 'S' angular futuristic glyph (left of center)
        val sPath = Path().apply {
            val left = cx - glyphScale * 0.92f
            val right = cx - glyphScale * 0.14f
            val top = cy - glyphScale * 0.55f
            val mid = cy
            val bottom = cy + glyphScale * 0.55f
            moveTo(right, top)
            lineTo(left, top)
            lineTo(left, mid)
            lineTo(right, mid)
            lineTo(right, bottom)
            lineTo(left, bottom)
        }

        // 'N' angular futuristic glyph (right of center)
        val nPath = Path().apply {
            val left = cx + glyphScale * 0.14f
            val right = cx + glyphScale * 0.92f
            val top = cy - glyphScale * 0.55f
            val bottom = cy + glyphScale * 0.55f
            moveTo(left, bottom)
            lineTo(left, top)
            lineTo(right, bottom)
            lineTo(right, top)
        }

        drawPath(sPath, color = Color.White.copy(alpha = 0.95f), style = snStroke)
        drawPath(nPath, color = Color.White.copy(alpha = 0.95f), style = snStroke)

        // Floor reflection ellipse underneath the 3D crystal
        if (showFloorReflection) {
            val floorY = size.height * 0.86f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.40f * pulseGlow),
                        tertiaryColor.copy(alpha = 0.14f * pulseGlow),
                        Color.Transparent
                    ),
                    center = Offset(cx, floorY),
                    radius = radius * 1.15f
                ),
                topLeft = Offset(cx - radius * 1.1f, floorY - radius * 0.20f),
                size = androidx.compose.ui.geometry.Size(radius * 2.2f, radius * 0.40f)
            )
        }
    }
}

/**
 * Real touch-responsive 3D perspective tilt, press-down scale, and neon rim-glow modifier.
 * Computes pointer touch coordinates relative to card center to apply subtle X/Y 3D tilt,
 * smooth spring release, and standard Material ripple indication.
 */
fun Modifier.interactive3DTilt(
    enabled3D: Boolean = true,
    maxTiltDegrees: Float = 8f,
    pressedScale: Float = 0.965f,
    shape: Shape = RoundedCornerShape(22.dp),
    onClick: () -> Unit
): Modifier = composed {
    var componentSize by remember { mutableStateOf(IntSize.Zero) }
    var targetTiltX by remember { mutableFloatStateOf(0f) }
    var targetTiltY by remember { mutableFloatStateOf(0f) }
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val extended = LocalExtendedTheme.current

    val animatedTiltX by animateFloatAsState(
        targetValue = if (enabled3D && isPressed) targetTiltX else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tiltX"
    )
    val animatedTiltY by animateFloatAsState(
        targetValue = if (enabled3D && isPressed) targetTiltY else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tiltY"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale3d"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.42f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "glowAlpha"
    )

    this
        .onSizeChanged { componentSize = it }
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
            rotationX = animatedTiltX
            rotationY = animatedTiltY
            cameraDistance = 14f * density
        }
        .drawBehind {
            if (glowAlpha > 0.01f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            extended.glowPrimary.copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        radius = size.maxDimension * 0.72f
                    )
                )
            }
        }
        .clip(shape)
        .pointerInput(enabled3D) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                if (enabled3D && componentSize.width > 0 && componentSize.height > 0) {
                    val normX = ((down.position.x / componentSize.width) - 0.5f) * 2f
                    val normY = ((down.position.y / componentSize.height) - 0.5f) * 2f
                    targetTiltY = (normX * maxTiltDegrees).coerceIn(-maxTiltDegrees, maxTiltDegrees)
                    targetTiltX = (-normY * maxTiltDegrees).coerceIn(-maxTiltDegrees, maxTiltDegrees)
                }
                do {
                    val event = awaitPointerEvent()
                    val activeChange = event.changes.firstOrNull()
                    if (enabled3D && activeChange != null && componentSize.width > 0 && componentSize.height > 0) {
                        val normX = ((activeChange.position.x / componentSize.width) - 0.5f) * 2f
                        val normY = ((activeChange.position.y / componentSize.height) - 0.5f) * 2f
                        targetTiltY = (normX * maxTiltDegrees).coerceIn(-maxTiltDegrees, maxTiltDegrees)
                        targetTiltX = (-normY * maxTiltDegrees).coerceIn(-maxTiltDegrees, maxTiltDegrees)
                    }
                } while (event.changes.any { it.pressed })
                isPressed = false
                targetTiltX = 0f
                targetTiltY = 0f
            }
        }
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = onClick
        )
}

@Composable
fun Neon3DCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enable3DMotion: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 22.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val extended = LocalExtendedTheme.current
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = modifier.interactive3DTilt(
            enabled3D = enable3DMotion,
            shape = shape,
            onClick = onClick
        ),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = if (extended.isNeonDark) 6.dp else 2.dp,
        shadowElevation = if (extended.isNeonDark) 10.dp else 4.dp,
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    accentColor.copy(alpha = if (extended.isNeonDark) 0.55f else 0.32f),
                    extended.cardBorder.copy(alpha = 0.18f),
                    accentColor.copy(alpha = if (extended.isNeonDark) 0.35f else 0.18f)
                )
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}

@Composable
fun Neon3DActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    enable3DMotion: Boolean = true,
    isPrimary: Boolean = true
) {
    val shape = RoundedCornerShape(16.dp)
    val bgBrush = if (isPrimary) {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
    val contentColor = if (isPrimary) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val interactiveMod = if (enabled) {
        modifier
            .defaultMinSize(minHeight = 50.dp)
            .interactive3DTilt(
                enabled3D = enable3DMotion,
                maxTiltDegrees = 6f,
                pressedScale = 0.96f,
                shape = shape,
                onClick = onClick
            )
    } else {
        modifier
            .defaultMinSize(minHeight = 50.dp)
            .clip(shape)
    }

    Box(
        modifier = interactiveMod
            .background(
                brush = bgBrush,
                alpha = if (enabled) 1f else 0.45f
            )
            .padding(PaddingValues(horizontal = 18.dp, vertical = 12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}
