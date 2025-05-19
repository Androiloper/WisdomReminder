package com.example.wisdomreminder.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.ElectricGreen
import com.example.wisdomreminder.ui.theme.NebulaPurple
import com.example.wisdomreminder.ui.theme.NeonPink
import com.example.wisdomreminder.ui.theme.StarWhite
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import androidx.compose.runtime.setValue

@Composable
fun AllWisdomSection(
    allWisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // State for pagination
    val pageSize = 7 // Show 7 items per page
    val pages = max(1, (allWisdom.size + pageSize - 1) / pageSize) // Math.ceil equivalent
    var currentPage by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section header with page indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ALL WISDOM",
                style = MaterialTheme.typography.titleLarge,
                color = NeonPink
            )

            if (pages > 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Navigation arrows and page counter
                    IconButton(
                        onClick = {
                            if (currentPage > 0) {
                                currentPage--
                            }
                        },
                        enabled = currentPage > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Page",
                            tint = if (currentPage > 0) ElectricGreen else ElectricGreen.copy(alpha = 0.3f)
                        )
                    }

                    Text(
                        text = "${currentPage + 1}/$pages",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarWhite
                    )

                    IconButton(
                        onClick = {
                            if (currentPage < pages - 1) {
                                currentPage++
                            }
                        },
                        enabled = currentPage < pages - 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Page",
                            tint = if (currentPage < pages - 1) ElectricGreen else ElectricGreen.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        // Content for current page
        if (allWisdom.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No wisdom available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }
        } else {
            // Calculate items for current page
            val startIndex = currentPage * pageSize
            val endIndex = minOf(startIndex + pageSize, allWisdom.size)
            val pageItems = allWisdom.subList(startIndex, endIndex)

            // Fixed height container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                // Use a Column to display items
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display each item in the current page
                    pageItems.forEach { wisdom ->
                        AllWisdomItem(
                            wisdom = wisdom,
                            onClick = { onWisdomClick(wisdom.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllWisdomItem(
    wisdom: Wisdom,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Status label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            when {
                                wisdom.isActive -> NeonPink.copy(alpha = 0.2f)
                                wisdom.dateCompleted != null -> CyberBlue.copy(alpha = 0.2f)
                                else -> NebulaPurple.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            wisdom.isActive -> "ACTIVE"
                            wisdom.dateCompleted != null -> "COMPLETED"
                            else -> "QUEUED"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            wisdom.isActive -> NeonPink
                            wisdom.dateCompleted != null -> CyberBlue
                            else -> NebulaPurple
                        }
                    )
                }

                Text(
                    text = wisdom.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }

            // Wisdom text
            Text(
                text = wisdom.text,
                style = MaterialTheme.typography.bodyMedium,
                color = StarWhite,
                maxLines = 2,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Source if available
            if (wisdom.source.isNotBlank()) {
                Text(
                    text = wisdom.source,
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = CyberBlue,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}