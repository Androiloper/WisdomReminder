package com.example.wisdomreminder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch
import kotlin.math.abs

// Define a new, larger height for the component
private val SWIPEABLE_CARDS_PAGER_HEIGHT = 340.dp
private val EMPTY_CARD_HEIGHT = 340.dp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SwipeableWisdomCards(
    allWisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (allWisdom.isEmpty()) {
        EmptyWisdomCard(
            modifier = modifier
                .height(EMPTY_CARD_HEIGHT)
                .padding(horizontal = 16.dp)
        )
        return
    }

    val pagerState = rememberPagerState(pageCount = { allWisdom.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(SWIPEABLE_CARDS_PAGER_HEIGHT),
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSize = PageSize.Fill
        ) { pageIndex ->
            val wisdom = allWisdom[pageIndex]
            SingleWisdomDisplayCard(
                wisdom = wisdom,
                onClick = { onWisdomClick(wisdom.id) },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val pageOffset = pagerState.getOffsetFractionForPage(pageIndex)
                        alpha = 1f - abs(pageOffset * 0.3f)
                        scaleX = 1f - abs(pageOffset * 0.1f)
                        scaleY = 1f - abs(pageOffset * 0.1f)
                    }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                    contentDescription = "Previous Wisdom",
                    tint = if (pagerState.currentPage > 0) ElectricGreen else ElectricGreen.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp)
                )
            }

            PageIndicator(
                pageCount = allWisdom.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(horizontal = 16.dp),
                selectedColor = NeonPink,
                unselectedColor = StarWhite.copy(alpha = 0.5f)
            )

            IconButton(
                onClick = {
                    coroutineScope.launch {
                        val nextPage = (pagerState.currentPage + 1).coerceAtMost(allWisdom.size - 1)
                        pagerState.animateScrollToPage(nextPage)
                    }
                },
                enabled = pagerState.currentPage < allWisdom.size - 1
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next Wisdom",
                    tint = if (pagerState.currentPage < allWisdom.size - 1) ElectricGreen else ElectricGreen.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleWisdomDisplayCard(
    wisdom: Wisdom,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(vertical = 8.dp)
            .then(CosmicAnimations.floatEffect(floatMagnitude = 1.5f, floatSpeed = 3500))
            .then(CosmicAnimations.glowEffect(glowColor = NebulaPurple.copy(alpha = 0.6f), glowAlphaRange = 0.2f..0.5f))
            .energyFlowEffect(colors = listOf(NebulaPurple.copy(alpha = 0.8f), CyberBlue.copy(alpha = 0.7f), NeonPink.copy(alpha = 0.6f)), flowSpeed = 4000)
            .glitchEffect(intensity = 0.005f, glitchInterval = 12000L),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface.copy(alpha = 0.75f),
            contentColor = StarWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    NebulaPurple.copy(alpha = 0.8f),
                    CyberBlue.copy(alpha = 0.8f),
                    NeonPink.copy(alpha = 0.8f)
                )
            )
        ),
        onClick = onClick,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp), // Adjusted vertical padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Section
            val titleText = wisdom.source.takeIf { it.isNotBlank() }
                ?: wisdom.category.takeIf { it.isNotBlank() }
                ?: "Wisdom"
            val titleColor = if (wisdom.source.isNotBlank()) CyberBlue else NebulaPurple

            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = titleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 10.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Scrollable Wisdom Text Section
            Column(
                modifier = Modifier
                    .weight(1f) // This Column will take available vertical space
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // Scroll this Column
            ) {
                Text(
                    text = "\"${wisdom.text}\"",
                    style = MaterialTheme.typography.titleLarge.copy(
                        lineHeight = 28.sp
                    ),
                    color = StarWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp) // Padding for the text itself
                )
            }

            // Category Badge Section (at the bottom)
            if (wisdom.category.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp)) // Space before badge
                Surface(
                    color = NebulaPurple.copy(alpha = 0.35f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = wisdom.category.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = NebulaPurple,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                // If no category, add a spacer to maintain some bottom margin
                val badgeMinHeight = with(MaterialTheme.typography.labelMedium) { fontSize.value + 12 } // approx height of badge
                Spacer(modifier = Modifier.height(badgeMinHeight.dp + 10.dp))
            }
        }
    }
}

@Composable
private fun EmptyWisdomCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface.copy(alpha = 0.4f),
            contentColor = StarWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    NebulaPurple.copy(alpha = 0.3f),
                    CyberBlue.copy(alpha = 0.3f)
                )
            )
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Add wisdom to start your journey",
                style = MaterialTheme.typography.titleMedium,
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
    modifier: Modifier = Modifier,
    selectedColor: Color = NeonPink,
    unselectedColor: Color = StarWhite.copy(alpha = 0.3f)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { iteration ->
            val color = if (currentPage == iteration) selectedColor else unselectedColor
            val size = if (currentPage == iteration) 10.dp else 8.dp
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(1f)
                    .alpha(if (currentPage == iteration) 1f else 0.5f)
                    .background(color, shape = CircleShape)
            )
        }
    }
}