package com.example.wisdomreminder.ui.components

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.theme.ElectricGreen
import com.example.wisdomreminder.ui.theme.NeonPink
import com.example.wisdomreminder.ui.theme.StarWhite
import kotlinx.coroutines.launch
import kotlin.math.abs

// Define a height for the explorer's pager area
private val EXPLORER_PAGER_HEIGHT = 340.dp // Consistent with SwipeableWisdomCards, adjust if needed

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryExplorerCard(
    allWisdom: List<Wisdom>,
    selectedCategory: String?, // Current filter from MainScreen state
    allCategories: List<String>, // For the filter dropdown
    onWisdomClick: (Long) -> Unit,
    onCategorySelected: (String?) -> Unit, // Callback when filter changes
    modifier: Modifier = Modifier
) {
    // Filter wisdom based on the selectedCategory.
    val filteredWisdom = remember(allWisdom, selectedCategory) {
        if (selectedCategory != null && selectedCategory != "All Categories") {
            allWisdom.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        } else {
            allWisdom // Show all wisdom if "All Categories" or null is selected
        }
    }

    Log.d("CategoryExplorerCard", "Selected Category: $selectedCategory, Filtered Wisdom Count: ${filteredWisdom.size}")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Category Filter at the top
        CategoryFilter(
            allCategories = allCategories, // Pass all available categories for the dropdown
            selectedCategory = selectedCategory, // Current selection
            onCategorySelected = onCategorySelected, // Callback to update selection in MainViewModel
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Apply consistent padding
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredWisdom.isEmpty()) {
            EmptyExplorerDisplay( // Use the updated empty state display
                modifier = Modifier
                    .height(EXPLORER_PAGER_HEIGHT)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // Consistent padding
                selectedCategory = selectedCategory
            )
        } else {
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { filteredWisdom.size }
            )

            // When the filter changes (selectedCategory) and thus filteredWisdom updates,
            // reset the pager to the first page.
            LaunchedEffect(selectedCategory, filteredWisdom.size) { // Keyed on selectedCategory and size
                if (filteredWisdom.isNotEmpty()) {
                    // If current page is invalid for new list, or if category changed, scroll to 0
                    if (pagerState.currentPage >= filteredWisdom.size || (selectedCategory != pagerState.settledPage.toString() && pagerState.currentPage !=0) ) {
                        Log.d("CategoryExplorerCard", "Filter changed or list size changed. Scrolling to page 0. Current Page: ${pagerState.currentPage}, New list size: ${filteredWisdom.size}")
                        pagerState.scrollToPage(0)
                    }
                }
            }

            val coroutineScope = rememberCoroutineScope()

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(EXPLORER_PAGER_HEIGHT),
                // Make cards wider by reducing horizontal contentPadding
                contentPadding = PaddingValues(horizontal = 24.dp), // Allows glimpses, cards are wider
                pageSize = PageSize.Fill
            ) { pageIndex ->
                if (pageIndex < filteredWisdom.size) { // Safety check
                    val wisdom = filteredWisdom[pageIndex]
                    SingleWisdomDisplayCard( // Reusing the styled card
                        wisdom = wisdom,
                        onClick = { onWisdomClick(wisdom.id) },
                        modifier = Modifier
                            .fillMaxSize() // Card fills the Pager item
                            .graphicsLayer { // Optional: Page transition effects
                                val pageOffset = pagerState.getOffsetFractionForPage(pageIndex)
                                alpha = 1f - abs(pageOffset * 0.6f) // Slightly more aggressive fade for further items
                                scaleX = 1f - abs(pageOffset * 0.20f) // Slightly more scaling
                                scaleY = 1f - abs(pageOffset * 0.20f)
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Controls (Arrows and Page Indicator)
            if (filteredWisdom.size > 1) {
                Row(
                    Modifier
                        .height(48.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val prevPage = (pagerState.currentPage - 1).coerceAtLeast(0)
                                pagerState.animateScrollToPage(prevPage)
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Wisdom in Explorer",
                            tint = if (pagerState.currentPage > 0) ElectricGreen else ElectricGreen.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    PageIndicator( // Reusing PageIndicator
                        pageCount = filteredWisdom.size,
                        currentPage = pagerState.currentPage,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        selectedColor = NeonPink,
                        unselectedColor = StarWhite.copy(alpha = 0.5f)
                    )

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val nextPage = (pagerState.currentPage + 1).coerceAtMost(filteredWisdom.size - 1)
                                pagerState.animateScrollToPage(nextPage)
                            }
                        },
                        enabled = pagerState.currentPage < filteredWisdom.size - 1
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Wisdom in Explorer",
                            tint = if (pagerState.currentPage < filteredWisdom.size - 1) ElectricGreen else ElectricGreen.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp)) // Maintain layout space
            }
        }
    }
}

@Composable
private fun EmptyExplorerDisplay( // Renamed from EmptyWisdomCard for clarity
    modifier: Modifier = Modifier,
    selectedCategory: String?
) {
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp), // Match horizontal padding for consistency
        contentAlignment = Alignment.Center
    ) {
        // Using SingleWisdomDisplayCard to show the empty message with consistent styling
        SingleWisdomDisplayCard(
            wisdom = Wisdom(
                id = -1L, // Dummy ID for empty state
                text = if (selectedCategory != null && selectedCategory != "All Categories") {
                    "No wisdom items found in the \"$selectedCategory\" category to explore."
                } else {
                    "No wisdom items available to explore. Add some!"
                },
                category = selectedCategory ?: "Info", // Use selected category or a generic one
                source = ""
            ),
            onClick = {}, // No action for the empty state card
            modifier = Modifier.fillMaxSize() // Let the card fill the pager item slot
        )
    }
}

// Ensure SingleWisdomDisplayCard, PageIndicator, and CategoryFilter composables
// are accessible (e.g., in the same package 'ui.components' or imported).
// These were defined/updated in previous steps.