package com.example.wisdomreminder.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.ElectricGreen
import com.example.wisdomreminder.ui.theme.GlassSurface
import com.example.wisdomreminder.ui.theme.GlassSurfaceLight
import com.example.wisdomreminder.ui.theme.NebulaPurple
import com.example.wisdomreminder.ui.theme.NeonPink
import com.example.wisdomreminder.ui.theme.StarWhite
import java.time.format.DateTimeFormatter

@Composable
fun ActiveWisdomCard(
    wisdom: Wisdom,
    onClick: (Wisdom) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val progress = (wisdom.currentDay.toFloat() / 21f).coerceIn(0f, 1f)

    GlassCard(
        modifier = modifier
            .clickable { onClick(wisdom) }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with day counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(NeonPink.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DAY ${wisdom.currentDay}/21",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonPink
                    )
                }

                Text(
                    text = "${wisdom.exposuresToday}/21 today",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricGreen
                )
            }

            // Progress indicator
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(6.dp),
                color = ElectricGreen,
                trackColor = GlassSurfaceLight
            )

            // Wisdom text
            Text(
                text = "\"${wisdom.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Source and stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    if (wisdom.source.isNotBlank()) {
                        Text(
                            text = wisdom.source,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = CyberBlue
                        )
                    }

                    Text(
                        text = "Started: ${wisdom.startDate?.format(dateFormatter) ?: "Not started"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarWhite.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = "${wisdom.exposuresTotal} exposures",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }
        }
    }

    @Composable
    fun QueuedWisdomItem(
        wisdom: Wisdom,
        onClick: () -> Unit,
        onActivate: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = GlassSurface.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = wisdom.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarWhite,
                        maxLines = 2
                    )

                    if (wisdom.source.isNotBlank()) {
                        Text(
                            text = wisdom.source,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = CyberBlue,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onActivate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NebulaPurple
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("ACTIVATE")
                }
            }
        }
    }
}