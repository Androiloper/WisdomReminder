package com.example.wisdomreminder.ui.components

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
 * with category filtering capabilities
 */
@Composable
fun CategoryExplorerCard(
    allWisdom: List<Wisdom>,
    selectedCategory: String?,
    allCategories: List<String>,
    onWisdomClick: (Long) -> Unit,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter wisdom by category if needed
    val filteredWisdom = if (selectedCategory != null) {
        allWisdom.filter { it.category == selectedCategory }
    } else {
        allWisdom
    }

    Log.d("CategoryExplorerCard", "Filtered wisdom: ${filteredWisdom.size} items from ${allWisdom.size}")

    if (filteredWisdom.isEmpty()) {
        // Empty state
        EmptyExplorerCard(modifier, selectedCategory)
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

    // Reset current index when the category filter changes
    LaunchedEffect(selectedCategory) {
        currentIndex = 0
    }



    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section title with category filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 0.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

        }

        // Section title with category filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 0.dp),
            horizontalArrangement = Arrangement.Absolute.Right,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Category filter
            CategoryFilter(
                allCategories = allCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected

            )

        }

    }
}

/**
 * Empty state for when there are no wisdom items
 */
@Composable
private fun EmptyExplorerCard(
    modifier: Modifier = Modifier,
    selectedCategory: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WISDOM EXPLORER",
                style = MaterialTheme.typography.titleLarge,
                color = NeonPink
            )
        }

        // Category filter will be rendered by parent component

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
                        text = if (selectedCategory != null)
                            "No wisdom in the \"$selectedCategory\" category"
                        else
                            "Add wisdom to start your journey",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarWhite.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}