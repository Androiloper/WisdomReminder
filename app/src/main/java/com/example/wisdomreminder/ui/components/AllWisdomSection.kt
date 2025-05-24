package com.example.wisdomreminder.ui.components

import android.util.Log // Import Log
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.ElectricGreen
import com.example.wisdomreminder.ui.theme.GlassSurface
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
    val localTag = "AllWisdomSection"

    val pageSize = 7
    val pages = remember(allWisdom.size, pageSize) {
        max(1, (allWisdom.size + pageSize - 1) / pageSize)
    }
    var currentPage by remember { mutableStateOf(0) }

    if (currentPage >= pages) {
        currentPage = max(0, pages - 1)
    }

    Log.d(localTag, "Total wisdom: ${allWisdom.size}, Page size: $pageSize, Total pages: $pages, Current page: $currentPage")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section header with page indicator
        if (pages > 1) { // Only show pagination controls if there's more than one page
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp), // Added vertical padding
                horizontalArrangement = Arrangement.End, // Changed to End as title is removed
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "ALL WISDOM" Text removed from here
                // Pagination controls
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentPage > 0) currentPage-- },
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
                        onClick = { if (currentPage < pages - 1) currentPage++ },
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
        } else {
            // If only one page or no items, provide a small spacer or nothing,
            // as the pagination controls and title are not needed.
            Spacer(modifier = Modifier.height(8.dp))
        }


        Spacer(modifier = Modifier.height(8.dp))

        if (allWisdom.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No wisdom available to display.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = StarWhite.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val startIndex = currentPage * pageSize
            val endIndex = minOf(startIndex + pageSize, allWisdom.size)
            val pageItems = if (startIndex < endIndex) allWisdom.subList(startIndex, endIndex) else emptyList()

            Log.d(localTag, "Page items for page ${currentPage + 1}: Count=${pageItems.size}, StartIndex=$startIndex, EndIndex=$endIndex")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                if (pageItems.isEmpty() && allWisdom.isNotEmpty()) {
                    Text(
                        "Error: No items for current page, but wisdom exists.",
                        color = NeonPink,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (pageItems.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No wisdom on this page.", color = StarWhite.copy(alpha = 0.7f))
                            }
                        } else {
                            pageItems.forEach { wisdom ->
                                Log.d(localTag, "Displaying item: ID=${wisdom.id}, Text='${wisdom.text.take(30)}...'")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                        }.uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            wisdom.isActive -> NeonPink
                            wisdom.dateCompleted != null -> CyberBlue
                            else -> NebulaPurple
                        }
                    )
                }
                Text(
                    text = wisdom.category.ifEmpty { "General" },
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = wisdom.text.ifEmpty { "(No wisdom text)" },
                style = MaterialTheme.typography.bodyMedium,
                color = StarWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (wisdom.source.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = wisdom.source,
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = CyberBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}