package com.example.wisdomreminder.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear // For clearing search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search // For search icon
// Removed: import androidx.compose.material.icons.filled.AccessTime // Using painterResource now
// Removed: import androidx.compose.material.icons.filled.SortByAlpha // Will use painterResource for ic_sort_by_alpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Added for painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.navigation.NavHostController
import com.example.wisdomreminder.R // Ensure R is imported for drawables
import com.example.wisdomreminder.model.Wisdom // Import Wisdom model
import com.example.wisdomreminder.ui.components.GlassCard
import com.example.wisdomreminder.ui.main.MainViewModel
import com.example.wisdomreminder.ui.navigation.Screen
import com.example.wisdomreminder.ui.theme.*
import java.time.LocalDateTime

// Updated Enum for Sort Order
enum class CategorySortOrder {
    ALPHABETICAL_ASC,
    ALPHABETICAL_DESC,
    NEWEST_WISDOM_FIRST, // Categories with newest wisdom items first
    OLDEST_WISDOM_FIRST  // Categories with oldest wisdom items first
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    navController: NavHostController,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var categoryToRename by remember { mutableStateOf<String?>(null) }
    var categoryToClear by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(CategorySortOrder.ALPHABETICAL_ASC) } // Default sort

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MainViewModel.UiEvent.CategoryOperationSuccess -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    categoryToRename = null
                    categoryToClear = null
                }
                is MainViewModel.UiEvent.Error -> {
                    Toast.makeText(context, "Error: ${event.message}", Toast.LENGTH_LONG).show()
                }
                is MainViewModel.UiEvent.MainScreenExplorerCategoryAdded -> {
                    Toast.makeText(context, "Category added to Main Screen explorers", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.MainScreenExplorerCategoryRemoved -> {
                    Toast.makeText(context, "Category removed from Main Screen explorers", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MANAGE CATEGORIES", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = StarWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GlassSurface.copy(alpha = 0.5f),
                    titleContentColor = StarWhite,
                    navigationIconContentColor = StarWhite
                )
            )
        },
        containerColor = DeepSpace
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = { Text("Search Categories", color = StarWhite.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = StarWhite.copy(alpha = 0.7f)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = StarWhite.copy(alpha = 0.7f))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberBlue,
                    unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                    focusedTextColor = StarWhite,
                    unfocusedTextColor = StarWhite,
                    cursorColor = CyberBlue,
                    focusedLabelColor = CyberBlue,
                    unfocusedLabelColor = StarWhite.copy(alpha = 0.7f),
                    focusedContainerColor = GlassSurface.copy(alpha = 0.3f),
                    unfocusedContainerColor = GlassSurface.copy(alpha = 0.2f),
                ),
                shape = MaterialTheme.shapes.medium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sort by:", style = MaterialTheme.typography.bodyMedium, color = StarWhite.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    sortOrder = when (sortOrder) {
                        CategorySortOrder.ALPHABETICAL_ASC -> CategorySortOrder.ALPHABETICAL_DESC
                        CategorySortOrder.ALPHABETICAL_DESC -> CategorySortOrder.NEWEST_WISDOM_FIRST
                        CategorySortOrder.NEWEST_WISDOM_FIRST -> CategorySortOrder.OLDEST_WISDOM_FIRST
                        CategorySortOrder.OLDEST_WISDOM_FIRST -> CategorySortOrder.ALPHABETICAL_ASC
                    }
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val sortIcon = when (sortOrder) {
                            CategorySortOrder.ALPHABETICAL_ASC, CategorySortOrder.ALPHABETICAL_DESC -> painterResource(id = R.drawable.ic_sort_by_alpha)
                            CategorySortOrder.NEWEST_WISDOM_FIRST, CategorySortOrder.OLDEST_WISDOM_FIRST -> painterResource(id = R.drawable.ic_access_time)
                        }
                        val sortText = when (sortOrder) {
                            CategorySortOrder.ALPHABETICAL_ASC -> "A-Z"
                            CategorySortOrder.ALPHABETICAL_DESC -> "Z-A"
                            CategorySortOrder.NEWEST_WISDOM_FIRST -> "Newest"
                            CategorySortOrder.OLDEST_WISDOM_FIRST -> "Oldest"
                        }
                        Icon(
                            painter = sortIcon,
                            contentDescription = "Sort Categories",
                            tint = ElectricGreen
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            sortText,
                            color = ElectricGreen,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            when (val state = uiState) {
                is MainViewModel.WisdomUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is MainViewModel.WisdomUiState.Success -> {
                    val filteredAndSortedCategories = remember(state.allCategories, state.allWisdomFlatList, searchQuery, sortOrder) {
                        val generalCategory = MainViewModel.DEFAULT_CATEGORY

                        // Filter by search query first
                        val searchedCategories = state.allCategories.filter {
                            it.contains(searchQuery, ignoreCase = true)
                        }

                        // Prepare data for date-based sorting
                        val categoryMaxDates = if (sortOrder == CategorySortOrder.NEWEST_WISDOM_FIRST || sortOrder == CategorySortOrder.OLDEST_WISDOM_FIRST) {
                            state.allWisdomFlatList
                                .groupBy { it.category }
                                .mapValues { entry ->
                                    entry.value.maxOfOrNull { it.dateCreated } ?: LocalDateTime.MIN
                                }
                        } else {
                            emptyMap()
                        }

                        val sortedSearchedCategories = searchedCategories
                            .filterNot { it.equals(generalCategory, ignoreCase = true) } // Exclude General for now
                            .let { list ->
                                when (sortOrder) {
                                    CategorySortOrder.ALPHABETICAL_ASC -> list.sortedBy { it.lowercase() }
                                    CategorySortOrder.ALPHABETICAL_DESC -> list.sortedByDescending { it.lowercase() }
                                    CategorySortOrder.NEWEST_WISDOM_FIRST -> list.sortedByDescending { categoryMaxDates[it] ?: LocalDateTime.MIN }
                                    CategorySortOrder.OLDEST_WISDOM_FIRST -> list.sortedBy { categoryMaxDates[it] ?: LocalDateTime.MAX }
                                }
                            }

                        val generalList = if (searchedCategories.any { it.equals(generalCategory, ignoreCase = true) }) {
                            listOf(generalCategory)
                        } else {
                            emptyList()
                        }
                        sortedSearchedCategories + generalList // Add General category at the end
                    }

                    if (filteredAndSortedCategories.isEmpty()) {
                        Text(
                            if (searchQuery.isNotEmpty()) "No categories matching '$searchQuery'."
                            else "No categories found. Add wisdom with categories to manage them here.",
                            color = StarWhite.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                        ) {
                            items(filteredAndSortedCategories, key = { it }) { category ->
                                val isGeneralCategory = category.equals(MainViewModel.DEFAULT_CATEGORY, ignoreCase = true)
                                val isSelectedForMainScreenExplorer = state.mainScreenExplorerCategories.contains(category)

                                ManageCategoryItem(
                                    categoryName = category,
                                    onRenameClick = { if (!isGeneralCategory) categoryToRename = category },
                                    onClearCategoryClick = { if (!isGeneralCategory) categoryToClear = category },
                                    isGeneral = isGeneralCategory,
                                    isSelectedForMainScreenExplorer = isSelectedForMainScreenExplorer,
                                    onToggleMainScreenExplorerClick = {
                                        if (isSelectedForMainScreenExplorer) {
                                            viewModel.removeCategoryFromMainScreenExplorers(category)
                                        } else {
                                            viewModel.addCategoryToMainScreenExplorers(category)
                                        }
                                    },
                                    onCategoryClick = { categoryName ->
                                        navController.navigate(Screen.WisdomList.createRoute(initialTabName = "all", categoryName = categoryName))
                                    }
                                )
                            }
                        }
                    }
                }
                is MainViewModel.WisdomUiState.Error -> {
                    Text("Error loading categories: ${state.message}", color = NeonPink, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }

    if (categoryToRename != null) {
        RenameCategoryDialog(
            currentCategoryName = categoryToRename!!,
            onDismiss = { categoryToRename = null },
            onSave = { oldName, newName ->
                viewModel.renameWisdomCategory(oldName, newName)
            }
        )
    }

    if (categoryToClear != null) {
        DeleteCategoryConfirmationDialog(
            categoryName = categoryToClear!!,
            onConfirm = {
                viewModel.clearWisdomCategory(it)
            },
            onDismiss = { categoryToClear = null }
        )
    }
}

@Composable
fun ManageCategoryItem(
    categoryName: String,
    isGeneral: Boolean,
    isSelectedForMainScreenExplorer: Boolean,
    onRenameClick: () -> Unit,
    onClearCategoryClick: () -> Unit,
    onToggleMainScreenExplorerClick: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCategoryClick(categoryName) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onToggleMainScreenExplorerClick) {
                Icon(
                    painter = if (isSelectedForMainScreenExplorer)
                        painterResource(id = R.drawable.ic_remove_circle_outline)
                    else
                        painterResource(id = R.drawable.ic_add_circle_outline),
                    contentDescription = if (isSelectedForMainScreenExplorer) "Remove from Main Screen Explorers" else "Add to Main Screen Explorers",
                    tint = if (isSelectedForMainScreenExplorer) NeonPink.copy(alpha = 0.8f) else ElectricGreen.copy(alpha = 0.8f)
                )
            }

            if (!isGeneral) {
                IconButton(onClick = onRenameClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Edit, "Rename Category", tint = CyberBlue)
                }
                IconButton(onClick = onClearCategoryClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete_outline),
                        contentDescription = "Clear Category (move items to General)",
                        tint = AccentOrange
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameCategoryDialog(
    currentCategoryName: String,
    onDismiss: () -> Unit,
    onSave: (oldName: String, newName: String) -> Unit
) {
    var newCategoryName by remember { mutableStateOf(currentCategoryName) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Category", color = ElectricGreen) },
        text = {
            OutlinedTextField(
                value = newCategoryName,
                onValueChange = { newCategoryName = it },
                label = { Text("New category name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricGreen,
                    unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                    focusedTextColor = StarWhite,
                    unfocusedTextColor = StarWhite,
                    cursorColor = ElectricGreen,
                    focusedLabelColor = ElectricGreen,
                    unfocusedLabelColor = StarWhite.copy(alpha = 0.7f),
                    focusedContainerColor = GlassSurfaceDark,
                    unfocusedContainerColor = GlassSurfaceDark,
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newCategoryName.isNotBlank() && newCategoryName != currentCategoryName) {
                        onSave(currentCategoryName, newCategoryName.trim())
                    } else if (newCategoryName.isBlank()){
                        Toast.makeText(context, "Category name cannot be empty.", Toast.LENGTH_SHORT).show()
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple)
            ) { Text("SAVE") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = StarWhite.copy(alpha = 0.7f)) }
        },
        containerColor = GlassSurface
    )
}

@Composable
fun DeleteCategoryConfirmationDialog(
    categoryName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear Category '$categoryName'?", color = NeonPink) },
        text = { Text("All wisdom items in '$categoryName' will be moved to the '${MainViewModel.DEFAULT_CATEGORY}' category. This action doesn't delete the wisdom items themselves. Are you sure?", color = StarWhite) },
        confirmButton = {
            Button(
                onClick = { onConfirm(categoryName) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
            ) { Text("CONFIRM & REASSIGN") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = StarWhite.copy(alpha = 0.7f)) }
        },
        containerColor = GlassSurface
    )
}
