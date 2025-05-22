package com.example.wisdomreminder.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.GlassSurface
import com.example.wisdomreminder.ui.theme.NebulaPurple

/**
 * A futuristic card with glowing borders and glass-like appearance
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier, // This modifier is passed from the call site (e.g., fillMaxWidth from AllWisdomItem)
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha_transition" // Added a more specific label for the transition
    )

    Box( // This Box takes the size from the 'modifier' argument.
        modifier = modifier // e.g., from AllWisdomItem, this is fillMaxWidth(). Height is effectively wrapContent.
    ) {
        Card(
            // Let the Card determine its own size based on its content,
            // but still respect the width constraints from the parent Box.
            // Height will be determined by 'content()'.
            modifier = Modifier
                .fillMaxWidth() // Ensure card itself is full width; height wraps content.
                .border( // Moved border to Card as it's more conventional for Card styling
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            NebulaPurple.copy(alpha = 0.7f * glowAlpha), // Apply glow to border too
                            CyberBlue.copy(alpha = 0.7f * glowAlpha),
                            NebulaPurple.copy(alpha = 0.7f * glowAlpha)
                        )
                    ),
                    shape = MaterialTheme.shapes.medium
                ),
            colors = CardDefaults.cardColors(
                containerColor = GlassSurface // GlassSurface already has transparency
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.medium // Ensure shape is applied for border clipping
        ) {
            // Apply an inner padding to the content if desired,
            // or let the content composable handle its own padding.
            // For this case, AllWisdomItem's Column already has padding.
            content() // This content (Column in AllWisdomItem) will define the Card's height.
        }
    }
}