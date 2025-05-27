package com.example.wisdomreminder.ui.wisdom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
// import androidx.compose.animation.Crossfade // Removed for now, page-level alpha will handle fade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
//import androidx.compose.foundation.pager.getOffsetFractionForPage // Import for page offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.Stop // Import Stop icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // Import for alpha and other transformations
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.wisdomreminder.R
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.components.GlassCard
import com.example.wisdomreminder.ui.main.MainViewModel
import com.example.wisdomreminder.ui.navigation.Screen
import com.example.wisdomreminder.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.abs // For absolute value of offset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WisdomDetailScreen(
    initialWisdomId: Long,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val mainUiState by viewModel.uiState.collectAsState()
    val isReadingMode by viewModel.isReadingMode.collectAsState()

    var currentDisplayedWisdom by remember { mutableStateOf<Wisdom?>(null) }

    val categoryWisdomData by produceState<Pair<List<Wisdom>, Int>?>(
        initialValue = null,
        key1 = initialWisdomId,
        key2 = mainUiState
    ) {
        val initialWisdom = when (val state = mainUiState) {
            is MainViewModel.WisdomUiState.Success -> state.allWisdomFlatList.find { it.id == initialWisdomId }
            else -> null
        }

        if (initialWisdom != null) {
            val categoryItems = when (val state = mainUiState) {
                is MainViewModel.WisdomUiState.Success -> state.allWisdomFlatList
                    .filter { it.category.equals(initialWisdom.category, ignoreCase = true) }
                    .sortedWith(compareBy({ it.orderIndex }, { it.dateCreated }))
                else -> emptyList()
            }
            val currentIndexInList = categoryItems.indexOfFirst { it.id == initialWisdomId }
            value = if (currentIndexInList != -1) {
                Pair(categoryItems, currentIndexInList)
            } else {
                Pair(listOf(initialWisdom), 0)
            }
        } else if (mainUiState is MainViewModel.WisdomUiState.Loading) {
            value = null
        } else {
            val errorWisdom = Wisdom(id = -1, text = "Wisdom not found or error loading.", category = "Error")
            value = Pair(listOf(errorWisdom), 0)
        }
    }

    if (categoryWisdomData == null || mainUiState is MainViewModel.WisdomUiState.Loading) {
        Box(modifier = Modifier.fillMaxSize().background(DeepSpace), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ElectricGreen)
        }
        return
    }

    val (categoryWisdomList, initialPageIndexInList) = categoryWisdomData!!

    if (categoryWisdomList.isEmpty() || initialPageIndexInList < 0 || initialPageIndexInList >= categoryWisdomList.size) {
        Box(modifier = Modifier.fillMaxSize().background(DeepSpace), contentAlignment = Alignment.Center) {
            Text("Wisdom item not found in its category or list is empty.", color = NeonPink, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialPageIndexInList,
        pageCount = { categoryWisdomList.size }
    )

    LaunchedEffect(pagerState, categoryWisdomList) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collectLatest { settledPage ->
                if (settledPage >= 0 && settledPage < categoryWisdomList.size) {
                    currentDisplayedWisdom = categoryWisdomList[settledPage]
                }
            }
    }

    LaunchedEffect(initialPageIndexInList, categoryWisdomList) {
        if (initialPageIndexInList >= 0 && initialPageIndexInList < categoryWisdomList.size) {
            currentDisplayedWisdom = categoryWisdomList[initialPageIndexInList]
            if (pagerState.currentPage != initialPageIndexInList && pagerState.pageCount == categoryWisdomList.size) {
                try {
                    pagerState.scrollToPage(initialPageIndexInList)
                } catch (e: Exception) { /* Handle or log */ }
            }
        }
    }

    val wisdomToDisplay = currentDisplayedWisdom ?: categoryWisdomList.getOrNull(pagerState.currentPage) ?: categoryWisdomList.getOrNull(initialPageIndexInList)

    if (wisdomToDisplay == null) {
        Box(modifier = Modifier.fillMaxSize().background(DeepSpace), contentAlignment = Alignment.Center) {
            Text("Loading wisdom...", color = StarWhite)
        }
        return
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        key = { pageIndex -> categoryWisdomList[pageIndex].id }
    ) { pageIndex ->
        val pageOffset = pagerState.getOffsetFractionForPage(pageIndex)
        val pageAlpha = 1f - abs(pageOffset) // Fade out as it moves away from center

        if (pageIndex >= 0 && pageIndex < categoryWisdomList.size) {
            // Apply graphicsLayer to the item content of the pager
            Box(modifier = Modifier.graphicsLayer { alpha = pageAlpha }) {
                WisdomItemDetailContent(
                    wisdom = categoryWisdomList[pageIndex], // Use the wisdom for *this* page
                    viewModel = viewModel,
                    onBackClick = onBackClick,
                    isFirstInCategory = pageIndex == 0,
                    isLastInCategory = pageIndex == categoryWisdomList.size - 1,
                    isReadingMode = isReadingMode,
                    onToggleReadingMode = { viewModel.toggleReadingMode() }
                )
            }
        } else {
            // Placeholder for safety, though should not be reached with current logic
            Box(modifier = Modifier.fillMaxSize().background(DeepSpace.copy(alpha = pageAlpha)), contentAlignment = Alignment.Center) {
                Text("Invalid page index.", color = NeonPink)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisdomItemDetailContent(
    wisdom: Wisdom,
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    isFirstInCategory: Boolean,
    isLastInCategory: Boolean,
    isReadingMode: Boolean,
    onToggleReadingMode: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val topBarAlpha by animateFloatAsState(
        targetValue = if (isReadingMode) 0f else 1f,
        animationSpec = tween(300), label = "topbar_alpha"
    )
    val contentPaddingTop by animateDpAsState(
        targetValue = if (isReadingMode) 0.dp else 16.dp,
        animationSpec = tween(300), label = "content_padding_top"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace) // This background will be visible as pages fade
            .drawBehind { // Stars drawn on the base Box, not per page
                drawRect(brush = Brush.verticalGradient(colors = listOf(CosmicBlack, DeepSpace)))
                for (i in 0..100) {
                    val x = (Math.random() * size.width).toFloat()
                    val y = (Math.random() * size.height).toFloat()
                    val radius = (Math.random() * 2f + 0.5f).toFloat()
                    val alpha = (Math.random() * 0.8f + 0.2f).toFloat()
                    drawCircle(color = StarWhite.copy(alpha = alpha), radius = radius, center = Offset(x, y))
                }
            }
    ) {
        // Background image, also part of the page content that will fade
        Image(
            painter = painterResource(id = R.drawable.ic_wisdom),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(if (isReadingMode) 0.02f else 0.1f).blur(if (isReadingMode) 30.dp else 20.dp)
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Scaffold is transparent to let page alpha control visibility
            contentColor = StarWhite,
            topBar = {
                if (!isReadingMode) {
                    TopAppBar(
                        modifier = Modifier.alpha(topBarAlpha),
                        title = {
                            Text(
                                wisdom.category.uppercase(),
                                style = MaterialTheme.typography.headlineLarge,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = StarWhite)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = GlassSurface.copy(alpha = 0.5f * topBarAlpha), // Fade TopAppBar background too
                            titleContentColor = StarWhite,
                            navigationIconContentColor = StarWhite
                        ),
                        actions = {
                            IconButton(onClick = onToggleReadingMode) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_menu_book),
                                    contentDescription = "Toggle Reading Mode",
                                    tint = StarWhite
                                )
                            }
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = StarWhite)
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonPink)
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                Crossfade(targetState = isReadingMode, animationSpec = tween(500), label = "fab_crossfade") { inReadingMode ->
                    if (inReadingMode) {
                        FloatingActionButton(
                            onClick = onToggleReadingMode,
                            containerColor = NebulaPurple.copy(alpha = 0.8f),
                            contentColor = StarWhite
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_close), contentDescription = "Exit Reading Mode")
                        }
                    } else if (wisdom.isActive) { // Show Deactivate if active
                        FloatingActionButton(
                            onClick = { viewModel.deactivateWisdom(wisdom.id) },
                            containerColor = AccentOrange.copy(alpha = 0.9f), // Or another distinct color
                            contentColor = StarWhite
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = "Deactivate Wisdom") // STOP Icon
                        }
                    } else if (!wisdom.isActive && wisdom.dateCompleted == null) { // Show Activate if queued
                        FloatingActionButton(
                            onClick = { viewModel.activateWisdom(wisdom.id) },
                            containerColor = NebulaPurple,
                            contentColor = StarWhite
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Activate Wisdom")
                        }
                    } else if (wisdom.dateCompleted != null) { // Show Reactivate if completed
                        FloatingActionButton(
                            onClick = { viewModel.activateWisdom(wisdom.id) }, // Same action as activate for now
                            containerColor = CyberBlue.copy(alpha = 0.8f),
                            contentColor = StarWhite
                        ) {
                            Icon(painterResource(id = R.drawable.ic_replay), contentDescription = "Reactivate Wisdom")
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = contentPaddingTop)
                    .verticalScroll(scrollState)
                    .padding(horizontal = if (isReadingMode) 24.dp else 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = if (isReadingMode) Arrangement.Center else Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = !isReadingMode, enter = fadeIn(), exit = fadeOut()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val (statusColor, statusText) = when {
                            wisdom.isActive -> NeonPink to "DAY ${wisdom.currentDay}/21"
                            wisdom.dateCompleted != null -> CyberBlue to "COMPLETED"
                            else -> NebulaPurple to "QUEUED"
                        }
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(statusColor.copy(alpha = 0.2f))
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(text = statusText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = statusColor)
                        }
                    }
                }

                AnimatedVisibility(visible = !isReadingMode && wisdom.isActive, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("21/21 RULE PROGRESS", style = MaterialTheme.typography.titleMedium, color = ElectricGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ProgressCircle("DAYS", wisdom.currentDay.toString(), NeonPink)
                                ProgressCircle("TODAY", wisdom.exposuresToday.toString(), ElectricGreen)
                                ProgressCircle("TOTAL", wisdom.exposuresTotal.toString(), CyberBlue, "exposures")
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Today's Exposures: ${wisdom.exposuresToday}/21", style = MaterialTheme.typography.bodySmall, color = StarWhite.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress =  (wisdom.exposuresToday.toFloat() / 21f).coerceIn(0f, 1f) ,
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = ElectricGreen,
                                trackColor = GlassSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Days Completed: ${wisdom.currentDay}/21", style = MaterialTheme.typography.bodySmall, color = StarWhite.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress =  (wisdom.currentDay.toFloat() / 21f).coerceIn(0f, 1f) ,
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = NeonPink,
                                trackColor = GlassSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxHeight(if (isReadingMode) 0.9f else 1f)) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isReadingMode) Modifier.fillMaxHeight() else Modifier)
                            .padding(vertical = if (isReadingMode) 32.dp else 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (isReadingMode) 32.dp else 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AnimatedVisibility(visible = !isReadingMode, enter = fadeIn(), exit = fadeOut()) {
                                Text("WISDOM", style = MaterialTheme.typography.titleMedium, color = NebulaPurple)
                            }
                            Spacer(modifier = Modifier.height(if (isReadingMode) 0.dp else 16.dp))

                            Text( // No Crossfade here, page alpha handles it
                                "\"${wisdom.text}\"",
                                style = if (isReadingMode) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineLarge,
                                color = StarWhite,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (wisdom.source.isNotBlank()) { // No Crossfade here
                                Text(
                                    "— ${wisdom.source}",
                                    style = if (isReadingMode) MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic) else MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                                    color = CyberBlue,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = !isReadingMode, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                Text("DETAILS", style = MaterialTheme.typography.titleMedium, color = CyberBlue)
                                Spacer(modifier = Modifier.height(16.dp))
                                val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, uuuu 'at' h:mm a") }
                                DetailRow("Category", wisdom.category, CyberBlue)
                                DetailRow("Created", wisdom.dateCreated.format(dateFormatter), CyberBlue)
                                wisdom.startDate?.let { DetailRow("Started", it.format(dateFormatter), CyberBlue) }
                                wisdom.dateCompleted?.let { DetailRow("Completed", it.format(dateFormatter), CyberBlue) }
                                DetailRow("Status", when {
                                    wisdom.isActive -> "Active (Day ${wisdom.currentDay}/21)"
                                    wisdom.dateCompleted != null -> "Completed"
                                    else -> "Queued"
                                }, CyberBlue)
                                if (wisdom.isActive || wisdom.dateCompleted != null) {
                                    DetailRow("Total Exposures", wisdom.exposuresTotal.toString(), CyberBlue)
                                }
                            }
                        }

                        // Action buttons section is now handled by the FAB based on state
                    }
                }
                AnimatedVisibility(visible = !isReadingMode) {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            if (showEditDialog) {
                EditWisdomDialog(
                    wisdom = wisdom,
                    onDismiss = { showEditDialog = false },
                    onSave = { text, source, category ->
                        viewModel.updateWisdom(wisdom.copy(text = text, source = source, category = category))
                        showEditDialog = false
                    }
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Wisdom") },
                    text = { Text("Are you sure you want to delete this wisdom? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteWisdom(wisdom)
                                showDeleteDialog = false
                                onBackClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                        ) { Text("DELETE") }
                    },
                    dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL") } }
                )
            }
        }
    }
}

@Composable
private fun ProgressCircle(label: String, value: String, color: Color, subLabel: String = "of 21") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(GlassSurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape)
                    .background(brush = Brush.radialGradient(colors = listOf(color.copy(alpha = 0.7f), color.copy(alpha = 0.1f)))),
                contentAlignment = Alignment.Center
            ) {
                Text(value, style = MaterialTheme.typography.displayMedium, color = StarWhite)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = color)
        if (label != "TOTAL") {
            Text(subLabel, style = MaterialTheme.typography.bodySmall, color = StarWhite.copy(alpha = 0.7f))
        } else {
            Text(subLabel, style = MaterialTheme.typography.bodySmall, color = StarWhite.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    labelColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = StarWhite
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWisdomDialog(
    wisdom: Wisdom,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var wisdomText by remember { mutableStateOf(wisdom.text) }
    var wisdomSource by remember { mutableStateOf(wisdom.source) }
    var wisdomCategory by remember { mutableStateOf(wisdom.category) }
    val categories = listOf("General", "Personal", "Professional", "Health", "Relationships", "Philosophy", "Motivation")
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface, contentColor = StarWhite),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("EDIT WISDOM", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CyberBlue)
                OutlinedTextField(
                    value = wisdomText,
                    onValueChange = { wisdomText = it },
                    label = { Text("Wisdom Text") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = CyberBlue,
                        focusedLabelColor = CyberBlue,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ),
                    minLines = 3
                )
                OutlinedTextField(
                    value = wisdomSource,
                    onValueChange = { wisdomSource = it },
                    label = { Text("Source (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = CyberBlue,
                        focusedLabelColor = CyberBlue,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = wisdomCategory,
                        onValueChange = { wisdomCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberBlue,
                            unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite,
                            cursorColor = CyberBlue,
                            focusedLabelColor = CyberBlue,
                            unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                        ),
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showCategoryDropdown = !showCategoryDropdown }) {
                                Icon(
                                    painter = if (showCategoryDropdown) painterResource(id = R.drawable.ic_arrow_drop_up) else painterResource(id = R.drawable.ic_arrow_drop_down),
                                    contentDescription = "Toggle Categories",
                                    tint = CyberBlue
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f).background(GlassSurfaceDark.copy(alpha = 0.95f))
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = if (category == wisdomCategory) CyberBlue else StarWhite) },
                                onClick = {
                                    wisdomCategory = category
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f)), modifier = Modifier.padding(end = 16.dp)) { Text("CANCEL") }
                    Button(
                        onClick = { if (wisdomText.isNotBlank()) onSave(wisdomText.trim(), wisdomSource.trim(), wisdomCategory.trim()) },
                        enabled = wisdomText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue)
                    ) { Text("UPDATE") }
                }
            }
        }
    }
}