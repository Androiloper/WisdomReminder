package com.example.wisdomreminder.ui.wisdom

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import all filled icons for simplicity
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.R
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.components.GlassCard
import com.example.wisdomreminder.ui.main.MainViewModel
import com.example.wisdomreminder.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisdomListScreen(
    onBackClick: () -> Unit,
    onWisdomClick: (Long) -> Unit,
    viewModel: MainViewModel,
    onManageCategoriesClick: () -> Unit
) {

    LaunchedEffect(Unit) {
        Log.d("WisdomListScreen", "Screen opened - forcing refresh")
        viewModel.refreshData()
    }

    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddWisdomDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Active", "Queued", "Completed")

    val activeWisdomList = (uiState as? MainViewModel.WisdomUiState.Success)?.activeWisdom ?: emptyList()
    val queuedWisdomList = (uiState as? MainViewModel.WisdomUiState.Success)?.queuedWisdom ?: emptyList()
    val completedWisdomList = (uiState as? MainViewModel.WisdomUiState.Success)?.completedWisdom ?: emptyList()
    val allCategoriesFromState = (uiState as? MainViewModel.WisdomUiState.Success)?.allCategories ?: emptyList()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .drawBehind {
                drawRect(brush = Brush.verticalGradient(colors = listOf(CosmicBlack, DeepSpace)))
                for (i in 0..100) {
                    val x = (Math.random() * size.width).toFloat()
                    val y = (Math.random() * size.height).toFloat()
                    val radius = (Math.random() * 2f + 0.5f).toFloat()
                    val alphaVal = (Math.random() * 0.8f + 0.2f).toFloat()
                    drawCircle(color = StarWhite.copy(alpha = alphaVal), radius = radius, center = Offset(x, y))
                }
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_wisdom),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.1f).blur(20.dp)
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = StarWhite,
            topBar = {
                TopAppBar(
                    title = { Text("WISDOM LIST", style = MaterialTheme.typography.headlineLarge) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Filled.ArrowBack, "Back", tint = StarWhite)
                        }
                    },
                    actions = {
                        IconButton(onClick = onManageCategoriesClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings), // Using the settings icon for now
                                contentDescription = "Manage Categories",
                                tint = StarWhite
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GlassSurface.copy(alpha = 0.5f),
                        titleContentColor = StarWhite,
                        navigationIconContentColor = StarWhite
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddWisdomDialog = true },
                    containerColor = NebulaPurple,
                    contentColor = StarWhite
                ) { Icon(Icons.Default.Add, "Add Wisdom") }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Search wisdom") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = StarWhite.copy(alpha = 0.7f)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Clear search", tint = StarWhite.copy(alpha = 0.7f))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = CyberBlue,
                        focusedContainerColor = GlassSurface.copy(alpha = 0.3f),
                        unfocusedContainerColor = GlassSurface.copy(alpha = 0.2f)
                    ),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = GlassSurface.copy(alpha = 0.3f),
                    contentColor = StarWhite,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = when (selectedTabIndex) {
                                0 -> ElectricGreen
                                1 -> NebulaPurple
                                else -> CyberBlue
                            }
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title.uppercase(), style = MaterialTheme.typography.bodyMedium) },
                            selectedContentColor = when (index) {
                                0 -> ElectricGreen
                                1 -> NebulaPurple
                                else -> CyberBlue
                            },
                            unselectedContentColor = StarWhite.copy(alpha = 0.7f)
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> ActiveWisdomList(activeWisdomList, onWisdomClick, searchQuery)
                    1 -> QueuedWisdomList(queuedWisdomList, onWisdomClick, { viewModel.activateWisdom(it) }, searchQuery)
                    2 -> CompletedWisdomList(completedWisdomList, onWisdomClick, { viewModel.activateWisdom(it) }, searchQuery)
                }
            }
        }

        // Add Wisdom Dialog Call
        if (showAddWisdomDialog) {
            // Ensure we only pass allCategories if the state is Success
            val currentCategories = (uiState as? MainViewModel.WisdomUiState.Success)?.allCategories ?: emptyList()
            AddWisdomDialog(
                allExistingCategories = currentCategories, // **THIS IS THE FIX**
                onDismiss = { showAddWisdomDialog = false },
                onSave = { text, source, category ->
                    viewModel.addWisdom(text, source, category)
                    showAddWisdomDialog = false
                    // viewModel.refreshData() // You might not need this if flows update UI correctly
                }
            )
        }
    }
    // Debug button removed from here for clarity, can be added back if needed
}

// ... (Rest of the composables: ActiveWisdomList, QueuedWisdomList, CompletedWisdomList, etc. remain unchanged from previous correct versions)
// Make sure ActiveWisdomItemSimplified, QueuedWisdomItemSimplified, CompletedWisdomItemSimplified
// are used by their respective list composables as set up in previous steps.

// Example for ActiveWisdomList using the simplified item:
@Composable
fun ActiveWisdomList(
    wisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,
    searchQuery: String
) {
    val filteredWisdom = if (searchQuery.isEmpty()) {
        wisdom
    } else {
        wisdom.filter {
            it.text.contains(searchQuery, ignoreCase = true) ||
                    it.source.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    if (filteredWisdom.isEmpty()) {
        EmptyStateMessage(
            text = if (searchQuery.isEmpty()) stringResource(R.string.empty_active)
            else "No active wisdom matching \"$searchQuery\""
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // FAB spacing
        ) {
            items(items = filteredWisdom, key = { it.id }) { item ->
                ActiveWisdomItemSimplified( // Using simplified item
                    wisdom = item,
                    onClick = { onWisdomClick(item.id) }
                )
            }
        }
    }
}


@Composable
fun ActiveWisdomItemSimplified(
    wisdom: Wisdom,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
            val progress = (wisdom.currentDay.toFloat() / 21f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).height(4.dp),
                color = ElectricGreen,
                trackColor = GlassSurface.copy(alpha = 0.3f)
            )
            Text(
                text = wisdom.text,
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                maxLines = 2,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (wisdom.source.isNotBlank()) {
                    Text(
                        text = wisdom.source,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = CyberBlue,
                        maxLines = 1
                    )
                }
                Text(
                    text = wisdom.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun QueuedWisdomList(
    wisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,
    onActivate: (Long) -> Unit,
    searchQuery: String
) {
    val filteredWisdom = if (searchQuery.isEmpty()) {
        wisdom
    } else {
        wisdom.filter {
            it.text.contains(searchQuery, ignoreCase = true) ||
                    it.source.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    if (filteredWisdom.isEmpty()) {
        EmptyStateMessage(
            text = if (searchQuery.isEmpty()) stringResource(R.string.empty_queued)
            else "No queued wisdom matching \"$searchQuery\""
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // FAB spacing
        ) {
            items(items = filteredWisdom, key = { it.id }) { item ->
                QueuedWisdomItemSimplified( // Using simplified item
                    wisdom = item,
                    onClick = { onWisdomClick(item.id) },
                    onActivate = { onActivate(item.id) }
                )
            }
        }
    }
}

@Composable
fun QueuedWisdomItemSimplified(
    wisdom: Wisdom,
    onClick: () -> Unit,
    onActivate: () -> Unit
) {
    var isActivating by remember { mutableStateOf(false) }
    val animSpec = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    var animationStarted by remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = animSpec,
        label = "alpha_queued_item"
    )
    LaunchedEffect(Unit) { animationStarted = true }
    LaunchedEffect(isActivating) { if (isActivating) { delay(3000); isActivating = false } }

    GlassCard(
        modifier = Modifier.fillMaxWidth().alpha(animatedAlpha).clickable(enabled = !isActivating) { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.clip(MaterialTheme.shapes.small).background(NebulaPurple.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("QUEUED", style = MaterialTheme.typography.bodySmall, color = NebulaPurple)
                }
                Button(
                    onClick = { isActivating = true; onActivate() },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isActivating) NebulaPurple.copy(alpha = 0.6f) else NebulaPurple),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    enabled = !isActivating
                ) {
                    if (isActivating) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = StarWhite, strokeWidth = 2.dp)
                        Spacer(Modifier.width(4.dp))
                        Text("ACTIVATING...")
                    } else {
                        Icon(Icons.Default.PlayArrow, "Activate", Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("ACTIVATE")
                    }
                }
            }
            Text(
                text = wisdom.text,
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                maxLines = 2,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (wisdom.source.isNotBlank()) {
                    Text(
                        text = wisdom.source,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = CyberBlue,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false) // Ensure it doesn't push category too far
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f)) // Fill space if source is empty
                }
                Text(
                    text = wisdom.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
}


@Composable
fun CompletedWisdomList(
    wisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,
    onReactivate: (Long) -> Unit,
    searchQuery: String
) {
    val filteredWisdom = wisdom.filter { item ->
        searchQuery.isEmpty() ||
                item.text.contains(searchQuery, ignoreCase = true) ||
                item.source.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
    }

    if (filteredWisdom.isEmpty()) {
        EmptyStateMessage(
            text = if (searchQuery.isEmpty()) stringResource(R.string.empty_completed)
            else "No completed wisdom matching \"$searchQuery\""
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // FAB spacing
        ) {
            itemsIndexed(items = filteredWisdom, key = { _, item -> item.id }) { index, item ->
                key(item.id) { // Explicit key for stability
                    CompletedWisdomItemSimplified( // Using simplified item
                        wisdom = item,
                        onClick = { onWisdomClick(item.id) },
                        onReactivate = { onReactivate(item.id) },
                        animationDelay = index * 50
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedWisdomItemSimplified(
    wisdom: Wisdom,
    onClick: () -> Unit,
    onReactivate: () -> Unit,
    animationDelay: Int = 0
) {
    var isReactivating by remember { mutableStateOf(false) }
    val animSpec = tween<Float>(durationMillis = 300, delayMillis = animationDelay)
    var animationStarted by remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = animSpec,
        label = "alpha_completed_item"
    )
    LaunchedEffect(Unit) { delay(50); animationStarted = true }
    LaunchedEffect(isReactivating) { if (isReactivating) { delay(3000); isReactivating = false } }

    GlassCard(
        modifier = Modifier.fillMaxWidth().alpha(animatedAlpha).clickable(enabled = !isReactivating) { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.clip(MaterialTheme.shapes.small).background(CyberBlue.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("COMPLETED", style = MaterialTheme.typography.bodySmall, color = CyberBlue)
                }
                Button(
                    onClick = { isReactivating = true; onReactivate() },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isReactivating) CyberBlue.copy(alpha = 0.6f) else CyberBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    enabled = !isReactivating
                ) {
                    if (isReactivating) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = StarWhite, strokeWidth = 2.dp)
                        Spacer(Modifier.width(4.dp))
                        Text("REACTIVATING...")
                    } else {
                        Icon(Icons.Default.PlayArrow, "Reactivate", Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("REACTIVATE")
                    }
                }
            }
            Text(
                text = wisdom.text,
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                maxLines = 2,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (wisdom.source.isNotBlank()) {
                    Text(
                        text = wisdom.source,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = CyberBlue,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    text = wisdom.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }
            val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Completed: ${wisdom.dateCompleted?.format(dateFormatter) ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.5f)
                )
                Text(
                    "${wisdom.exposuresTotal} total exposures",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.5f)
                )
            }
        }
    }
}


@Composable
fun EmptyStateMessage(text: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp), // Added padding
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = StarWhite.copy(alpha = 0.8f), // Slightly more visible
            textAlign = TextAlign.Center
        )
    }
}

// ActiveWisdomItem, QueuedWisdomItem, CompletedWisdomItem (the non-Simplified versions with more animation)
// can be kept if you prefer their animation style, or removed if the Simplified versions are sufficient.
// For brevity, I'm assuming the simplified versions are now the primary ones for the lists.
// If you keep the original animated items, ensure their onClick and other parameters match.