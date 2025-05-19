// SwipeableWisdomCards.kt - Fixed version to avoid ambiguity
// This replaces the onCardClick with onWisdomClick for clarity

package com.example.wisdomreminder.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.theme.CosmicAnimations
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.ElectricGreen
import com.example.wisdomreminder.ui.theme.GlassSurface
import com.example.wisdomreminder.ui.theme.NebulaPurple
import com.example.wisdomreminder.ui.theme.NeonPink
import com.example.wisdomreminder.ui.theme.StarWhite
import com.example.wisdomreminder.ui.theme.energyFlowEffect
import com.example.wisdomreminder.ui.theme.glitchEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableWisdomCards(
    allWisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,  // Change to onWisdomClick with Long parameter
    modifier: Modifier = Modifier
) {
    if (allWisdom.isEmpty()) {
        EmptyWisdomCard(modifier)
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var offsetX by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Track animation state
    var isAnimating by remember { mutableStateOf(false) }

    // Control the visibility of navigation indicators
    var showNavIndicators by remember { mutableStateOf(false) }

    // Auto-hide nav indicators after delay
    LaunchedEffect(showNavIndicators) {
        if (showNavIndicators) {
            delay(3000)
            showNavIndicators = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        showNavIndicators = true
                    },
                    onDragEnd = {
                        if (abs(offsetX) > size.width / 4) {
                            isAnimating = true
                            if (offsetX > 0 && currentIndex > 0) {
                                // Swipe right (to previous)
                                currentIndex--
                            } else if (offsetX < 0 && currentIndex < allWisdom.size - 1) {
                                // Swipe left (to next)
                                currentIndex++
                            }
                        }

                        // Reset offset with animation
                        coroutineScope.launch {
                            offsetX = 0f
                            delay(300)
                            isAnimating = false
                        }
                    },
                    onDragCancel = {
                        offsetX = 0f
                        isAnimating = false
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (!isAnimating) {
                            // Limit dragging based on current index
                            when {
                                currentIndex == 0 && dragAmount > 0 -> {
                                    // First card, can't go left, limit drag
                                    offsetX = (offsetX + dragAmount).coerceIn(0f, 100f)
                                }
                                currentIndex == allWisdom.size - 1 && dragAmount < 0 -> {
                                    // Last card, can't go right, limit drag
                                    offsetX = (offsetX + dragAmount).coerceIn(-100f, 0f)
                                }
                                else -> {
                                    // Normal case, allow dragging
                                    offsetX += dragAmount
                                }
                            }
                        }
                    }
                )
            }
    ) {
        // Current wisdom card with offset animation
        val currentWisdom = allWisdom.getOrNull(currentIndex) ?: return@Box

        // Calculate card scale based on drag
        val scale = animateFloatAsState(
            targetValue = 1f - (abs(offsetX) / 2000f),
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "card scale"
        )

        // Calculate card rotation based on drag
        val rotation = animateFloatAsState(
            targetValue = offsetX / 50f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "card rotation"
        )

        // Main card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .offset { IntOffset(offsetX.toInt(), 0) }
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                }
                .then(CosmicAnimations.floatEffect(floatMagnitude = 3f))
                .then(CosmicAnimations.glowEffect(glowColor = NebulaPurple))
                .energyFlowEffect(colors = listOf(NebulaPurple, CyberBlue, NeonPink.copy(alpha = 0.5f)))
                .zIndex(1f),
            colors = CardDefaults.cardColors(
                containerColor = GlassSurface.copy(alpha = 0.7f),
                contentColor = StarWhite
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        NebulaPurple.copy(alpha = 0.7f),
                        CyberBlue.copy(alpha = 0.7f),
                        NeonPink.copy(alpha = 0.7f)
                    )
                )
            ),
            onClick = { onWisdomClick(currentWisdom.id) }  // Modified to pass the id directly
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Category badge at top
                Surface(
                    color = NebulaPurple.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = currentWisdom.category.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = NebulaPurple,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // Wisdom text
                Text(
                    text = "\"${currentWisdom.text}\"",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    ),
                    color = StarWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .glitchEffect(intensity = 0.01f, glitchInterval = 10000L)
                )

                // Source if available
                if (currentWisdom.source.isNotBlank()) {
                    Text(
                        text = "— ${currentWisdom.source}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = CyberBlue,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }
        }

        // Navigation indicators (arrows)
        AnimatedVisibility(
            visible = showNavIndicators,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            // Left arrow (previous)
            if (currentIndex > 0) {
                IconButton(
                    onClick = {
                        if (!isAnimating) {
                            isAnimating = true
                            coroutineScope.launch {
                                offsetX = 300f // Simulate right swipe
                                delay(50)
                                currentIndex--
                                delay(50)
                                offsetX = 0f
                                delay(300)
                                isAnimating = false
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous",
                        tint = ElectricGreen.copy(alpha = 0.8f),
                        modifier = Modifier.scale(1.5f)
                    )
                }
            }

            // Right arrow (next)
            if (currentIndex < allWisdom.size - 1) {
                IconButton(
                    onClick = {
                        if (!isAnimating) {
                            isAnimating = true
                            coroutineScope.launch {
                                offsetX = -300f // Simulate left swipe
                                delay(50)
                                currentIndex++
                                delay(50)
                                offsetX = 0f
                                delay(300)
                                isAnimating = false
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next",
                        tint = ElectricGreen.copy(alpha = 0.8f),
                        modifier = Modifier.scale(1.5f)
                    )
                }
            }
        }

        // Page indicator dots
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        ) {
            PageIndicator(
                pageCount = allWisdom.size,
                currentPage = currentIndex
            )
        }
    }
}

@Composable
private fun EmptyWisdomCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface.copy(alpha = 0.4f),
            contentColor = StarWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    NebulaPurple.copy(alpha = 0.3f),
                    CyberBlue.copy(alpha = 0.3f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Add wisdom to start your journey",
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    selectedColor: androidx.compose.ui.graphics.Color = NeonPink,
    unselectedColor: androidx.compose.ui.graphics.Color = StarWhite.copy(alpha = 0.3f),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        // Create a row of dots
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            repeat(pageCount) { index ->
                val isSelected = index == currentPage
                val size = animateFloatAsState(
                    targetValue = if (isSelected) 8f else 6f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "dot size"
                )

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .scale(size.value / 6f)
                        .alpha(if (isSelected) 1f else 0.5f)
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .size(6.dp)
                    ) {
                        drawCircle(
                            color = if (isSelected) selectedColor else unselectedColor
                        )
                    }
                }
            }
        }
    }
}