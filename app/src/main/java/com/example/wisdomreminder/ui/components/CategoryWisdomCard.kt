package com.example.wisdomreminder.ui.components

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.NebulaPurple
import com.example.wisdomreminder.ui.theme.NeonPink
import com.example.wisdomreminder.ui.theme.StarWhite

/**
 * Displays wisdom items filtered by a specific category
 */
@Composable
fun CategoryWisdomCard(
    category: String,
    wisdomList: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    Log.d("CategoryWisdomCard", "Rendering category: $category with ${wisdomList.size} items")
    wisdomList.forEachIndexed { index, wisdom ->
        Log.d("CategoryWisdomCard", "Item $index: ${wisdom.text.take(20)}...")
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
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
            // Header with category name and expand/collapse icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(NeonPink.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonPink
                    )
                }

                Row {
                    // Number of items indicator
                    Text(
                        text = "${wisdomList.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarWhite.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Remove button
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Category",
                            tint = NeonPink.copy(alpha = 0.7f)
                        )
                    }

                    // Expand/collapse button
                    IconButton(
                        onClick = {
                            expanded = !expanded
                            Log.d("CategoryWisdomCard", "Category $category expanded: $expanded")
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = CyberBlue
                        )
                    }
                }
            }

            // Divider
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NebulaPurple.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Preview or full list
            if (expanded) {
                // Show full list
                if (wisdomList.isEmpty()) {
                    Text(
                        text = "No wisdom in this category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarWhite.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(
                            items = wisdomList,
                            key = { it.id } // Use stable ID as key for better list updates
                        ) { wisdom ->
                            CategoryWisdomItem(
                                wisdom = wisdom,
                                onClick = { onWisdomClick(wisdom.id) }
                            )
                        }
                    }
                }
            } else {
                // Show just a preview
                if (wisdomList.isEmpty()) {
                    Text(
                        text = "No wisdom in this category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarWhite.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    // Display first item as preview
                    val previewWisdom = wisdomList.first()
                    Text(
                        text = previewWisdom.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = StarWhite,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (previewWisdom.source.isNotBlank()) {
                        Text(
                            text = previewWisdom.source,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = CyberBlue,
                            maxLines = 1,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }

                    if (wisdomList.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "And ${wisdomList.size - 1} more...",
                            style = MaterialTheme.typography.bodySmall,
                            color = StarWhite.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryWisdomItem(
    wisdom: Wisdom,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.small)
            .background(NebulaPurple.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = wisdom.text,
                style = MaterialTheme.typography.bodyMedium,
                color = StarWhite,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (wisdom.source.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = wisdom.source,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = CyberBlue,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}