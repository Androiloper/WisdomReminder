

package com.example.wisdomreminder.ui.wisdom

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add // Added import PlaylistAdd
import androidx.compose.material.icons.filled.Info // Added import RadioButtonUnchecked
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
import androidx.compose.ui.text.font.FontWeight
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
    var selectedTabIndex by remember { mutableIntStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddWisdomDialog by remember { mutableStateOf(false) }
    var showCategorySelectorForSevenWisdom by remember { mutableStateOf(false) }


    val tabs = listOf("Active", "Queued", "Completed")

    val activeWisdomListFromState = (uiState as? MainViewModel.WisdomUiState.Success)?.activeWisdom ?: emptyList()
    val favoriteQueuedWisdomFromState = (uiState as? MainViewModel.WisdomUiState.Success)?.favoriteQueuedWisdom ?: emptyList()
    val sevenWisdomPlaylistFromState = (uiState as? MainViewModel.WisdomUiState.Success)?.sevenWisdomPlaylist ?: emptyList()
    val selectedCategoryForSevenWisdomState = (uiState as? MainViewModel.WisdomUiState.Success)?.selectedCategoryForSevenWisdom
    // This variable holds the data from the ViewModel
    val otherQueuedWisdomFromState = (uiState as? MainViewModel.WisdomUiState.Success)?.otherQueuedWisdom ?: emptyList()
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
                    title = { Text("WISDOM LISTS", style = MaterialTheme.typography.headlineLarge) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Filled.ArrowBack, "Back", tint = StarWhite)
                        }
                    },
                    actions = {
                        IconButton(onClick = onManageCategoriesClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings),
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
                        unfocusedContainerColor = GlassSurface.copy(alpha = 0.2f),
                        focusedLabelColor = CyberBlue,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
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
                    0 -> ActiveWisdomList(activeWisdomListFromState, onWisdomClick, searchQuery, viewModel)
                    1 -> QueuedWisdomPlaylistsScreen(
                        sevenWisdomPlaylist = sevenWisdomPlaylistFromState,
                        favoriteQueuedWisdom = favoriteQueuedWisdomFromState,
                        otherRandomQueuedWisdom = otherQueuedWisdomFromState, // Passed here
                        selectedCategoryForSeven = selectedCategoryForSevenWisdomState,
                        onWisdomClick = onWisdomClick,
                        viewModel = viewModel,
                        searchQuery = searchQuery,
                        onSelectCategoryForSevenWisdom = {
                            showCategorySelectorForSevenWisdom = true
                        }
                    )
                    2 -> CompletedWisdomList(completedWisdomList, onWisdomClick, { viewModel.activateWisdom(it) }, searchQuery, viewModel)
                }
            }
        }

        if (showAddWisdomDialog) {
            val currentCategories = (uiState as? MainViewModel.WisdomUiState.Success)?.allCategories ?: emptyList()
            AddWisdomDialog(
                allExistingCategories = currentCategories,
                onDismiss = { showAddWisdomDialog = false },
                onSave = { text, source, category ->
                    viewModel.addWisdom(text, source, category)
                    showAddWisdomDialog = false
                }
            )
        }

        if (showCategorySelectorForSevenWisdom) {
            CategorySelectorDialog(
                allCategories = allCategoriesFromState,
                currentSelectedCategory = selectedCategoryForSevenWisdomState,
                onDismiss = { showCategorySelectorForSevenWisdom = false },
                onCategorySelected = { category ->
                    viewModel.setCategoryForSevenWisdomPlaylist(category)
                    showCategorySelectorForSevenWisdom = false
                }
            )
        }
    }
}

@Composable
fun ActiveWisdomList(
    wisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,
    searchQuery: String,
    viewModel: MainViewModel
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
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(items = filteredWisdom, key = { it.id }) { item ->
                ActiveWisdomItemSimplified(
                    wisdom = item,
                    onClick = { onWisdomClick(item.id) },
                    onToggleFavorite = { viewModel.toggleFavoriteStatus(item.id) }
                )
            }
        }
    }
}

@Composable
fun ActiveWisdomItemSimplified(
    wisdom: Wisdom,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (wisdom.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (wisdom.isFavorite) NeonPink else StarWhite.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${wisdom.exposuresToday}/21 today",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricGreen
                    )
                }
            }
            val progress = (wisdom.currentDay.toFloat() / 21f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress =  progress ,
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
                } else {
                    Spacer(Modifier.weight(1f))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueuedWisdomPlaylistsScreen(
    sevenWisdomPlaylist: List<Wisdom>,
    favoriteQueuedWisdom: List<Wisdom>,
    otherRandomQueuedWisdom: List<Wisdom>, // This is the parameter received
    selectedCategoryForSeven: String?,
    onWisdomClick: (Long) -> Unit,
    viewModel: MainViewModel,
    searchQuery: String,
    onSelectCategoryForSevenWisdom: () -> Unit
) {
    val filterPredicate: (Wisdom) -> Boolean = {
        searchQuery.isBlank() ||
                it.text.contains(searchQuery, ignoreCase = true) ||
                it.source.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    val displaySevenWisdom = sevenWisdomPlaylist.filter(filterPredicate)
    val displayFavorites = favoriteQueuedWisdom.filter(filterPredicate)
    // CORRECTED: Use the parameter 'otherRandomQueuedWisdom' here
    val displayOthers = otherRandomQueuedWisdom.filter(filterPredicate)

    val noResultsForSearch = displaySevenWisdom.isEmpty() && displayFavorites.isEmpty() && displayOthers.isEmpty() && searchQuery.isNotEmpty()
    val noItemsAtAll = sevenWisdomPlaylist.isEmpty() && favoriteQueuedWisdom.isEmpty() && otherRandomQueuedWisdom.isEmpty() && searchQuery.isEmpty()


    if (noResultsForSearch) {
        EmptyStateMessage(text = "No queued wisdom matching \"$searchQuery\"")
        return
    }
    if (noItemsAtAll){
        EmptyStateMessage(text = stringResource(R.string.empty_queued))
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionHeader(
                    title = "Selected Category Playlist ${selectedCategoryForSeven?.let { "($it)" } ?: "(None Selected)"}",
                    color = ElectricGreen
                )
                IconButton(onClick = onSelectCategoryForSevenWisdom) {
                    Icon(Icons.Filled.Add, contentDescription = "Select Category for '7 Wisdom' Playlist", tint = ElectricGreen)
                }
            }
            if (selectedCategoryForSeven == null) {
                Text(
                    "Tap the icon to select a category. The first 7 displayable items from that category will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarWhite.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
            }
        }
        if (displaySevenWisdom.isEmpty() && selectedCategoryForSeven != null && searchQuery.isEmpty()) {
            item { EmptyStateMessage(text = "No items in '$selectedCategoryForSeven' for this playlist (they might be completed, or you need to add more).") }
        } else if (displaySevenWisdom.isEmpty() && selectedCategoryForSeven != null && searchQuery.isNotEmpty()){
            item { EmptyStateMessage(text = "No items in '$selectedCategoryForSeven' match your search.") }
        }
        else {
            items(items = displaySevenWisdom, key = { "seven-${it.id}" }) { wisdom ->
                QueuedWisdomItemWithSwipe(
                    wisdom = wisdom,
                    onClick = { onWisdomClick(wisdom.id) },
                    onActivate = { viewModel.activateWisdom(wisdom.id) },
                    onDismissed = { viewModel.deleteWisdom(wisdom) },
                    onToggleFavorite = { viewModel.toggleFavoriteStatus(wisdom.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Favorites Playlist (${displayFavorites.size})", color = NeonPink)
        }
        if (displayFavorites.isEmpty() && favoriteQueuedWisdom.isNotEmpty() && searchQuery.isNotEmpty()){
            item { EmptyStateMessage(text = "No favorites match your search.") }
        } else if (favoriteQueuedWisdom.isEmpty() && searchQuery.isEmpty()){
            item { EmptyStateMessage(text = "No wisdom marked as favorite (that isn't completed).") }
        }else {
            items(items = displayFavorites, key = { "fav-${it.id}" }) { wisdom ->
                QueuedWisdomItemWithSwipe(
                    wisdom = wisdom,
                    onClick = { onWisdomClick(wisdom.id) },
                    onActivate = { viewModel.activateWisdom(wisdom.id) },
                    onDismissed = { viewModel.deleteWisdom(wisdom) },
                    onToggleFavorite = { viewModel.toggleFavoriteStatus(wisdom.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Other Queued Wisdom (${displayOthers.size})", color = CyberBlue)
        }
        if (displayOthers.isEmpty() && otherRandomQueuedWisdom.isNotEmpty() && searchQuery.isNotEmpty()){
            item { EmptyStateMessage(text = "No other queued items match your search.") }
        } else if (otherRandomQueuedWisdom.isEmpty() && searchQuery.isEmpty() && (sevenWisdomPlaylist.isNotEmpty() || favoriteQueuedWisdom.isNotEmpty())){
            item { EmptyStateMessage(text = "All other queued items are in selected category or favorites.") }
        } else if (otherRandomQueuedWisdom.isEmpty() && searchQuery.isEmpty() && sevenWisdomPlaylist.isEmpty() && favoriteQueuedWisdom.isEmpty()){
            // This state is covered by the main noItemsAtAll check
        }
        else {
            items(items = displayOthers, key = { "other-${it.id}" }) { wisdom ->
                QueuedWisdomItemWithSwipe(
                    wisdom = wisdom,
                    onClick = { onWisdomClick(wisdom.id) },
                    onActivate = { viewModel.activateWisdom(wisdom.id) },
                    onDismissed = { viewModel.deleteWisdom(wisdom) },
                    onToggleFavorite = { viewModel.toggleFavoriteStatus(wisdom.id) }
                )
            }
        }
    }
}


@Composable
fun CategorySelectorDialog(
    allCategories: List<String>,
    currentSelectedCategory: String?,
    onDismiss: () -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Category for Playlist", color = ElectricGreen) },
        text = {
            if (allCategories.isEmpty()) {
                Text("No categories available. Add some wisdom with categories first.", color = StarWhite)
            } else {
                LazyColumn {
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onCategorySelected(null) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentSelectedCategory == null) Icons.Filled.CheckCircle else Icons.Filled.Info,
                                contentDescription = "Clear Selection",
                                tint = if (currentSelectedCategory == null) ElectricGreen else StarWhite.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("None (Clear Selection)", color = StarWhite)
                        }
                    }
                    items(allCategories.sorted()) { category ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onCategorySelected(category) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (category == currentSelectedCategory) Icons.Filled.CheckCircle else Icons.Filled.Info,
                                contentDescription = category,
                                tint = if (category == currentSelectedCategory) ElectricGreen else StarWhite.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(category, color = StarWhite)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", color = StarWhite.copy(alpha = 0.7f)) }
        },
        containerColor = GlassSurfaceDark
    )
}


@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = color,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueuedWisdomItemWithSwipe(
    wisdom: Wisdom,
    onClick: () -> Unit,
    onActivate: () -> Unit,
    onDismissed: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val dismissState = rememberDismissState(
        confirmValueChange = {
            if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                onDismissed()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
        background = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    DismissValue.DismissedToEnd -> NeonPink.copy(alpha = 0.7f)
                    DismissValue.DismissedToStart -> NeonPink.copy(alpha = 0.7f)
                    else -> Color.Transparent
                }, label = "swipe_background_color_anim_queued_item"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = when (direction) {
                    DismissDirection.StartToEnd -> Alignment.CenterStart
                    DismissDirection.EndToStart -> Alignment.CenterEnd
                    null -> Alignment.Center
                }
            ) {
                if (dismissState.targetValue != DismissValue.Default) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = StarWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        dismissContent = {
            QueuedWisdomItemSimplified(
                wisdom = wisdom,
                onClick = onClick,
                onActivate = onActivate,
                onToggleFavorite = onToggleFavorite
            )
        }
    )
}


@Composable
fun QueuedWisdomItemSimplified(
    wisdom: Wisdom,
    onClick: () -> Unit,
    onActivate: () -> Unit,
    onToggleFavorite: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (wisdom.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (wisdom.isFavorite) NeonPink else StarWhite.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.clip(MaterialTheme.shapes.small)
                            .background(
                                if (wisdom.isActive) ElectricGreen.copy(alpha = 0.2f)
                                else NebulaPurple.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (wisdom.isActive) "ACTIVE IN CYCLE" else "QUEUED",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (wisdom.isActive) ElectricGreen else NebulaPurple
                        )
                    }
                }
                Button(
                    onClick = { isActivating = true; onActivate() },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isActivating) NebulaPurple.copy(alpha = 0.6f) else NebulaPurple),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    enabled = !isActivating && !wisdom.isActive
                ) {
                    if (isActivating) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = StarWhite, strokeWidth = 2.dp)
                        Spacer(Modifier.width(4.dp))
                        Text("ACTIVATING...")
                    } else if (wisdom.isActive) {
                        Icon(Icons.Default.Check, "Already Active", Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("ACTIVE")
                    }
                    else {
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
        }
    }
}


@Composable
fun CompletedWisdomList(
    wisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,
    onReactivate: (Long) -> Unit,
    searchQuery: String,
    viewModel: MainViewModel
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
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            itemsIndexed(items = filteredWisdom, key = { _, item -> item.id }) { index, item ->
                key(item.id) {
                    CompletedWisdomItemSimplified(
                        wisdom = item,
                        onClick = { onWisdomClick(item.id) },
                        onReactivate = { onReactivate(item.id) },
                        animationDelay = index * 50,
                        onToggleFavorite = { viewModel.toggleFavoriteStatus(item.id) }
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
    animationDelay: Int = 0,
    onToggleFavorite: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (wisdom.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (wisdom.isFavorite) NeonPink else StarWhite.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.clip(MaterialTheme.shapes.small).background(CyberBlue.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("COMPLETED", style = MaterialTheme.typography.bodySmall, color = CyberBlue)
                    }
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
            val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yy") }
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
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = StarWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}