package com.example.wisdomreminder.ui.categories // New package

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.wisdomreminder.ui.components.GlassCard
import com.example.wisdomreminder.ui.main.MainViewModel
import com.example.wisdomreminder.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var categoryToRename by remember { mutableStateOf<String?>(null) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    // Collect events for toasts
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MainViewModel.UiEvent.CategoryOperationSuccess -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    // Potentially clear dialog states if needed, or rely on recomposition
                    categoryToRename = null
                    categoryToDelete = null
                }
                is MainViewModel.UiEvent.Error -> {
                    Toast.makeText(context, "Error: ${event.message}", Toast.LENGTH_LONG).show()
                }
                else -> {} // Other events handled elsewhere or ignored here
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
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is MainViewModel.WisdomUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is MainViewModel.WisdomUiState.Success -> {
                    if (state.allCategories.isEmpty()) {
                        Text(
                            "No categories found. Add wisdom with categories to manage them here.",
                            color = StarWhite.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.allCategories.filterNot { it.equals(MainViewModel.DEFAULT_CATEGORY, ignoreCase = true) }
                                .sorted() // Show non-default categories first, then General
                                    + (if (state.allCategories.any { it.equals(MainViewModel.DEFAULT_CATEGORY, ignoreCase = true) }) listOf(MainViewModel.DEFAULT_CATEGORY) else emptyList())
                            ) { category ->
                                val isGeneralCategory = category.equals(MainViewModel.DEFAULT_CATEGORY, ignoreCase = true)
                                ManageCategoryItem(
                                    categoryName = category,
                                    onRenameClick = { if (!isGeneralCategory) categoryToRename = category },
                                    onDeleteClick = { if (!isGeneralCategory) categoryToDelete = category },
                                    isGeneral = isGeneralCategory
                                )
                            }
                        }
                    }
                }
                is MainViewModel.WisdomUiState.Error -> {
                    Text("Error loading categories: ${state.message}", color = NeonPink)
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
                categoryToRename = null // Dialog will be dismissed by this state change
            }
        )
    }

    if (categoryToDelete != null) {
        DeleteCategoryConfirmationDialog(
            categoryName = categoryToDelete!!,
            onConfirm = {
                viewModel.clearWisdomCategory(it) // Reassigns items to "General"
                categoryToDelete = null
            },
            onDismiss = { categoryToDelete = null }
        )
    }
}

@Composable
fun ManageCategoryItem(
    categoryName: String,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isGeneral: Boolean
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                modifier = Modifier.weight(1f)
            )
            if (!isGeneral) { // "General" category cannot be renamed or deleted from here
                Row {
                    IconButton(onClick = onRenameClick) {
                        Icon(Icons.Default.Edit, "Rename Category", tint = CyberBlue)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, "Delete Category", tint = NeonPink)
                    }
                }
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
                        onDismiss() // Same name, just dismiss
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