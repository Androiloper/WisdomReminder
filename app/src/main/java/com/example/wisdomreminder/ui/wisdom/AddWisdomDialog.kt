package com.example.wisdomreminder.ui.wisdom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.wisdomreminder.ui.main.MainViewModel // For DEFAULT_CATEGORY
import com.example.wisdomreminder.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWisdomDialog(
    allExistingCategories: List<String>, // Pass this from MainViewModel's state.allCategories
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var wisdomText by remember { mutableStateOf("") }
    var wisdomSource by remember { mutableStateOf("") }
    var wisdomCategoryInput by remember { mutableStateOf(MainViewModel.DEFAULT_CATEGORY) } // Editable input
    var showCategoryDropdown by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Combine predefined defaults with existing unique categories, ensuring "General" is present
    val defaultSuggestions = listOf("Personal", "Professional", "Health", "Relationships", "Philosophy", "Motivation", "Quotes")
    val uniqueCategories = (allExistingCategories + defaultSuggestions + listOf(MainViewModel.DEFAULT_CATEGORY)).distinct().sorted()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface, contentColor = StarWhite),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "ADD NEW WISDOM",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElectricGreen
                )

                OutlinedTextField(
                    value = wisdomText,
                    onValueChange = { wisdomText = it },
                    label = { Text("Wisdom Text") },
                    placeholder = { Text("Enter your wisdom") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricGreen,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        cursorColor = ElectricGreen,
                        focusedLabelColor = ElectricGreen,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f),
                        focusedContainerColor = GlassSurfaceDark.copy(alpha = 0.5f),
                        unfocusedContainerColor = GlassSurfaceDark.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite
                    ),
                    minLines = 3
                )

                OutlinedTextField(
                    value = wisdomSource,
                    onValueChange = { wisdomSource = it },
                    label = { Text("Source (Optional)") },
                    placeholder = { Text("Author, book, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        cursorColor = CyberBlue,
                        focusedLabelColor = CyberBlue,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f),
                        focusedContainerColor = GlassSurfaceDark.copy(alpha = 0.5f),
                        unfocusedContainerColor = GlassSurfaceDark.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite
                    ),
                    singleLine = true
                )

                // Editable Category Field with Dropdown
                Box {
                    OutlinedTextField(
                        value = wisdomCategoryInput,
                        onValueChange = {
                            wisdomCategoryInput = it
                            showCategoryDropdown = true // Show dropdown when user types
                        },
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState -> // Show dropdown on focus too
                                if (focusState.isFocused) {
                                    showCategoryDropdown = true
                                }
                            },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                            cursorColor = NeonPink,
                            focusedLabelColor = NeonPink,
                            unfocusedLabelColor = StarWhite.copy(alpha = 0.7f),
                            focusedContainerColor = GlassSurfaceDark.copy(alpha = 0.5f),
                            unfocusedContainerColor = GlassSurfaceDark.copy(alpha = 0.5f),
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite
                        ),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showCategoryDropdown = !showCategoryDropdown }) {
                                Icon(
                                    imageVector = if (showCategoryDropdown) Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown,
                                    contentDescription = "Toggle Category Dropdown",
                                    tint = NeonPink
                                )
                            }
                        }
                    )

                    if (showCategoryDropdown) {
                        val filteredCategories = uniqueCategories.filter {
                            it.contains(wisdomCategoryInput, ignoreCase = true) || wisdomCategoryInput.isBlank()
                        }.take(10) // Limit suggestions for performance

                        if (filteredCategories.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp) // Position below the TextField
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GlassSurfaceDark.copy(alpha = 0.95f))
                                    .border(1.dp, NeonPink.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp) // Constrain height
                                        .padding(vertical = 4.dp)
                                ) {
                                    items(filteredCategories) { category ->
                                        CategoryDropdownItem(
                                            text = category,
                                            isSelected = category.equals(wisdomCategoryInput, ignoreCase = true),
                                            onClick = {
                                                wisdomCategoryInput = category
                                                showCategoryDropdown = false
                                                focusManager.clearFocus() // Clear focus to hide keyboard
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = StarWhite.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (wisdomText.isNotBlank()) {
                                onSave(
                                    wisdomText.trim(),
                                    wisdomSource.trim(),
                                    wisdomCategoryInput.trim().ifEmpty { MainViewModel.DEFAULT_CATEGORY }
                                )
                            }
                        },
                        enabled = wisdomText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple)
                    ) {
                        Text("SAVE")
                    }
                }
            }
        }
    }
}

// Ensure CategoryDropdownItem is accessible (e.g., public or in the same file)
@Composable
fun CategoryDropdownItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) NeonPink.copy(alpha = 0.2f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp) // Adjusted padding
    ) {
        Text(
            text = text,
            color = if (isSelected) NeonPink else StarWhite,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}