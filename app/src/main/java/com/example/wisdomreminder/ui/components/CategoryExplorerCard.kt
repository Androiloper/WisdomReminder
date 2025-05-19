package com.example.wisdomreminder.ui.components



import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.theme.CosmicAnimations
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.ElectricGreen
import com.example.wisdomreminder.ui.theme.NebulaPurple
import com.example.wisdomreminder.ui.theme.NeonPink
import com.example.wisdomreminder.ui.theme.StarWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A card component that displays wisdom in a circular explorer view
 * similar to the design shown in the screenshot
 */
@Composable
fun CategoryExplorerCard(
    allWisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (allWisdom.isEmpty()) {
        // Empty state
        EmptyExplorerCard(modifier)
        return
    }

    // State for the current wisdom index
    var currentIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Track if we're animating a transition
    var isAnimating by remember { mutableStateOf(false) }

    // Show navigation indicators when interacting
    var showNavIndicators by remember { mutableStateOf(false) }

    // Auto-hide indicators after delay
    LaunchedEffect(showNavIndicators) {
        if (showNavIndicators) {
            delay(3000)
            showNavIndicators = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section title
        Text(
            text = "WISDOM EXPLORER",
            style = MaterialTheme.typography.titleLarge,
            color = NeonPink,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Circular background container
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NebulaPurple.copy(alpha = 0.7f),
                            NebulaPurple.copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Current wisdom
            val currentWisdom = allWisdom[currentIndex]

            // Animation for card transitions
            val offsetX by animateFloatAsState(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "card offset"
            )

            // Card with wisdom content
            GlassCard(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1.6f)
                    .clickable(enabled = !isAnimating) { onWisdomClick(currentWisdom.id) }
                    .then(CosmicAnimations.floatEffect(floatMagnitude = 2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Category pill/badge
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(NebulaPurple.copy(alpha = 0.3f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentWisdom.category.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            color = NebulaPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wisdom text
                    Text(
                        text = "\"${currentWisdom.text}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = StarWhite,
                        textAlign = TextAlign.Center,
                        maxLines = 4,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Source (if available)
                    if (currentWisdom.source.isNotBlank()) {
                        Text(
                            text = currentWisdom.source,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = CyberBlue,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Navigation arrows
            if (showNavIndicators || isAnimating) {
                // Left arrow (previous)
                if (currentIndex > 0) {
                    IconButton(
                        onClick = {
                            if (!isAnimating) {
                                isAnimating = true
                                coroutineScope.launch {
                                    currentIndex--
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
                                    currentIndex++
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
        }

        // Page indicator dots
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
        ) {
            PageIndicator(
                pageCount = allWisdom.size,
                currentPage = currentIndex
            )
        }
    }
}

/**
 * Empty state for when there are no wisdom items
 */
@Composable
private fun EmptyExplorerCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WISDOM EXPLORER",
            style = MaterialTheme.typography.titleLarge,
            color = NeonPink,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NebulaPurple.copy(alpha = 0.5f),
                            NebulaPurple.copy(alpha = 0.2f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1.6f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add wisdom to start your journey",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarWhite.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}