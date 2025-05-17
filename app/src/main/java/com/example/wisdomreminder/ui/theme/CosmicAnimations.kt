package com.example.wisdomreminder.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * Animation utility class for cosmic UI effects
 */
object CosmicAnimations {

    /**
     * Apply a subtle pulsing effect to a composable
     */
    @Composable
    fun pulseEffect(
        pulseMagnitude: Float = 0.05f,
        pulseSpeed: Int = 2000,
        initialScale: Float = 1f
    ): Modifier {
        var scale by remember { mutableFloatStateOf(initialScale) }

        LaunchedEffect(Unit) {
            val animation = TargetBasedAnimation(
                animationSpec = infiniteRepeatable(
                    animation = tween(pulseSpeed, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                typeConverter = Float.VectorConverter,
                initialValue = initialScale,
                targetValue = initialScale + pulseMagnitude
            )

            var playTime = 0L
            var lastFrameTimeNanos = 0L

            while (true) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos != 0L) {
                        playTime += frameTimeNanos - lastFrameTimeNanos
                    }
                    lastFrameTimeNanos = frameTimeNanos
                    scale = animation.getValueFromNanos(playTime)
                }
            }
        }

        return Modifier.scale(scale)
    }

    /**
     * Apply a floating effect to a composable
     */
    @Composable
    fun floatEffect(
        floatMagnitude: Float = 5f,
        floatSpeed: Int = 3000
    ): Modifier {
        var offsetY by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(Unit) {
            val animation = TargetBasedAnimation(
                animationSpec = infiniteRepeatable(
                    animation = tween(floatSpeed, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                typeConverter = Float.VectorConverter,
                initialValue = -floatMagnitude,
                targetValue = floatMagnitude
            )

            var playTime = 0L
            var lastFrameTimeNanos = 0L

            while (true) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos != 0L) {
                        playTime += frameTimeNanos - lastFrameTimeNanos
                    }
                    lastFrameTimeNanos = frameTimeNanos
                    offsetY = animation.getValueFromNanos(playTime)
                }
            }
        }

        return Modifier.offset(y = offsetY.dp)
    }

    /**
     * Apply a subtle glow effect to a composable
     */
    @Composable
    fun glowEffect(
        glowColor: Color = ElectricGreen,
        glowAlphaRange: ClosedFloatingPointRange<Float> = 0.3f..0.7f,
        glowDuration: Int = 2000
    ): Modifier {
        var glowAlpha by remember { mutableFloatStateOf(glowAlphaRange.start) }

        LaunchedEffect(Unit) {
            val animation = TargetBasedAnimation(
                animationSpec = infiniteRepeatable(
                    animation = tween(glowDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                typeConverter = Float.VectorConverter,
                initialValue = glowAlphaRange.start,
                targetValue = glowAlphaRange.endInclusive
            )

            var playTime = 0L
            var lastFrameTimeNanos = 0L

            while (true) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos != 0L) {
                        playTime += frameTimeNanos - lastFrameTimeNanos
                    }
                    lastFrameTimeNanos = frameTimeNanos
                    glowAlpha = animation.getValueFromNanos(playTime)
                }
            }
        }

        return Modifier.drawBehind {
            drawCircle(
                color = glowColor.copy(alpha = glowAlpha),
                radius = size.maxDimension * 0.6f
            )
        }
    }

    /**
     * Cosmic background particles effect
     * Use as a background decoration
     */
    @Composable
    fun CosmicParticlesEffect(
        particleCount: Int = 50,
        modifier: Modifier = Modifier
    ) {
        Box(modifier = modifier) {
            repeat(particleCount) { index ->
                val size = remember { (0.5f + Math.random() * 2.5f).toFloat() }
                val xPosition = remember { (Math.random() * 100).toFloat() }
                val yPosition = remember { (Math.random() * 100).toFloat() }
                val alpha = remember { (0.3f + Math.random() * 0.7f).toFloat() }
                val animDuration = remember { (1500 + Math.random() * 3000).toInt() }
                val animDelay = remember { (Math.random() * 2000).toLong() }

                var particleAlpha by remember { mutableStateOf(0f) }

                LaunchedEffect(key1 = Unit) {
                    delay(animDelay)
                    particleAlpha = alpha
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(particleAlpha)
                        .then(
                            pulseEffect(
                                pulseMagnitude = 0.2f,
                                pulseSpeed = animDuration
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(size / 100f)
                            .offset(
                                x = (xPosition).dp,
                                y = (yPosition).dp
                            )
                            .background(
                                color = StarWhite,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        }
    }
}

/**
 * A modifier extension that applies a beautiful glitch effect
 * Perfect for cyberpunk or cosmic UI elements
 */
@Composable
fun Modifier.glitchEffect(
    enabled: Boolean = true,
    intensity: Float = 0.02f,
    glitchInterval: Long = 5000L
): Modifier {
    if (!enabled) return this

    var xOffset by remember { mutableFloatStateOf(0f) }
    var yOffset by remember { mutableFloatStateOf(0f) }
    var rotationZ by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(enabled) {
        while (true) {
            // Random time between glitches
            delay((glitchInterval * (0.8 + Math.random() * 0.4)).toLong())

            // Quick glitch effect
            repeat(3) {
                xOffset = (Math.random() * 2 - 1).toFloat() * intensity * 10
                yOffset = (Math.random() * 2 - 1).toFloat() * intensity * 5
                rotationZ = (Math.random() * 2 - 1).toFloat() * intensity * 5
                delay(50)

                // Reset
                xOffset = 0f
                yOffset = 0f
                rotationZ = 0f
                delay(50)
            }
        }
    }

    return this.graphicsLayer {
        translationX = xOffset
        translationY = yOffset
        rotationZ = rotationZ
    }
}

/**
 * A modifier that applies a flowing energy effect to the content
 * Suitable for magic or energy-related UI elements
 */
@Composable
fun Modifier.energyFlowEffect(
    enabled: Boolean = true,
    colors: List<Color> = listOf(NebulaPurple, CyberBlue, NeonPink),
    flowSpeed: Int = 3000
): Modifier {
    if (!enabled) return this

    var rotation by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(enabled) {
        val animation = TargetBasedAnimation(
            animationSpec = infiniteRepeatable(
                animation = tween(flowSpeed),
                repeatMode = RepeatMode.Restart
            ),
            typeConverter = Float.VectorConverter,
            initialValue = 0f,
            targetValue = 360f
        )

        var playTime = 0L
        var lastFrameTimeNanos = 0L

        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos != 0L) {
                    playTime += frameTimeNanos - lastFrameTimeNanos
                }
                lastFrameTimeNanos = frameTimeNanos
                rotation = animation.getValueFromNanos(playTime)
            }
        }
    }

    return this.drawBehind {
        drawRect(
            brush = Brush.sweepGradient(
                colors = colors,
                center = center.copy(
                    x = center.x + (size.width * 0.2f) * sin(Math.toRadians(rotation.toDouble()).toFloat()),
                    y = center.y + (size.height * 0.2f) * sin(Math.toRadians((rotation + 90).toDouble()).toFloat())
                )
            ),
            alpha = 0.3f
        )
    }
}