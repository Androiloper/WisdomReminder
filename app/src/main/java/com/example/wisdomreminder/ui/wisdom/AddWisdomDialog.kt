package com.example.wisdomreminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWisdomDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, text: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    // List of predefined categories
    val categories = listOf("General", "Personal Development", "Philosophy", "Mindfulness", "Relationships", "Health", "Career")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add New Wisdom",
                    style = MaterialTheme.typography.headlineSmall
                )

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Enter a title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Text field
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Wisdom Text") },
                    placeholder = { Text("Enter your wisdom") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { /* Handle expansion */ }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        value = category,
                        onValueChange = { },
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
                    )

                    ExposedDropdownMenu(
                        expanded = false,
                        onDismissRequest = { /* Handle dismiss */ }
                    ) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    category = option
                                }
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && text.isNotBlank()) {
                                onSave(title.trim(), text.trim(), category)
                            }
                        },
                        enabled = title.isNotBlank() && text.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}