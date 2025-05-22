package com.example.wisdomreminder.ui.components

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
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

private val EXPLORER_PAGER_HEIGHT = 340.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryExplorerCard(
    allWisdom: List<Wisdom>,
    selectedCategory: String?,
    allCategories: List<String>,
    onWisdomClick: (Long) -> Unit,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredWisdom = remember(allWisdom, selectedCategory) {
        if (selectedCategory != null && selectedCategory != "All Categories") {
            allWisdom.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        } else {
            allWisdom
        }
    }

    Log.d("CategoryExplorerCard", "Selected Category: $selectedCategory, Filtered Wisdom Count: ${filteredWisdom.size}")

    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CategoryFilter(
            allCategories = allCategories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredWisdom.isEmpty()) {
            EmptyExplorerDisplay(
                modifier = Modifier.height(EXPLORER_PAGER_HEIGHT).fillMaxWidth().padding(horizontal = 16.dp),
                selectedCategory = selectedCategory
            )
        } else {
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { filteredWisdom.size }
            )

            // Effect to scroll to page 0 when the filtered list changes significantly (e.g. category changes)
            // or if the current page becomes invalid.
            LaunchedEffect(filteredWisdom) { // Keyed on the list itself
                val newPageCount = filteredWisdom.size
                if (newPageCount > 0) {
                    if (pagerState.currentPage >= newPageCount || pagerState.currentPage != 0) { // If current page is invalid or not 0
                        Log.d("CategoryExplorerCard", "Filtered wisdom changed or current page invalid. Scrolling to page 0. New size: $newPageCount, CurrentPage: ${pagerState.currentPage}")
                        try {
                            pagerState.scrollToPage(0)
                        } catch (e: Exception) {
                            Log.e("CategoryExplorerCard", "Error scrolling to page 0: ${e.message}")
                        }
                    }
                }
                // If newPageCount is 0, Pager will handle it (no pages to show).
            }


            val coroutineScope = rememberCoroutineScope()

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(EXPLORER_PAGER_HEIGHT),
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSize = PageSize.Fill
            ) { pageIndex ->
                // Defensive check: Ensure pageIndex is valid for the current state of filteredWisdom
                if (pageIndex < filteredWisdom.size) {
                    val wisdom = filteredWisdom[pageIndex]
                    SingleWisdomDisplayCard(
                        wisdom = wisdom,
                        onClick = { onWisdomClick(wisdom.id) },
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // **THE FIX IS HERE:** Add a guard before calling getOffsetFractionForPage
                                if (pageIndex < pagerState.pageCount) {
                                    val pageOffset = pagerState.getOffsetFractionForPage(pageIndex)
                                    alpha = 1f - abs(pageOffset * 0.6f)
                                    scaleX = 1f - abs(pageOffset * 0.20f)
                                    scaleY = 1f - abs(pageOffset * 0.20f)
                                } else {
                                    // This page is temporarily out of bounds during recomposition,
                                    // make it invisible or don't apply transforms.
                                    alpha = 0f
                                    Log.w("CategoryExplorerCard", "graphicsLayer: pageIndex $pageIndex is out of bounds for pageCount ${pagerState.pageCount}. Setting alpha to 0.")
                                }
                            }
                    )
                } else {
                    Log.w("CategoryExplorerCard", "HorizontalPager content: pageIndex $pageIndex is out of bounds for filteredWisdom size ${filteredWisdom.size}. Rendering nothing for this index.")
                    // Render an empty box or nothing if pageIndex is truly out of bounds
                    // This state should be rare if pagerState.pageCount is updated correctly.
                    Box(Modifier.fillMaxSize())
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredWisdom.size > 1) {
                Row(
                    Modifier.height(48.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                if (pagerState.currentPage > 0) {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, "Previous Wisdom", tint = if (pagerState.currentPage > 0) ElectricGreen else ElectricGreen.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                    }
                    PageIndicator(
                        pageCount = filteredWisdom.size, // Use current filtered list size
                        currentPage = pagerState.currentPage,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        selectedColor = NeonPink,
                        unselectedColor = StarWhite.copy(alpha = 0.5f)
                    )
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                if (pagerState.currentPage < filteredWisdom.size - 1) {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        enabled = pagerState.currentPage < filteredWisdom.size - 1
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, "Next Wisdom", tint = if (pagerState.currentPage < filteredWisdom.size - 1) ElectricGreen else ElectricGreen.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun EmptyExplorerDisplay(
    modifier: Modifier = Modifier,
    selectedCategory: String?
) {
    Box(
        modifier = modifier.padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        SingleWisdomDisplayCard(
            wisdom = Wisdom(
                id = -1L,
                text = if (selectedCategory != null && selectedCategory != "All Categories") {
                    "No wisdom items found in the \"$selectedCategory\" category."
                } else {
                    "No wisdom items available. Add some to explore!"
                },
                category = selectedCategory ?: "Info",
                source = ""
            ),
            onClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

// Ensure SingleWisdomDisplayCard, PageIndicator, and CategoryFilter composables are correctly defined and imported.