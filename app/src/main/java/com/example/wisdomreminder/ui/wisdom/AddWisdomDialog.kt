package com.example.wisdomreminder.ui.wisdom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.ElectricGreen
import com.example.wisdomreminder.ui.theme.GlassSurface
import com.example.wisdomreminder.ui.theme.NebulaPurple
import com.example.wisdomreminder.ui.theme.NeonPink
import com.example.wisdomreminder.ui.theme.StarWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWisdomDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var wisdomText by remember { mutableStateOf("") }
    var wisdomSource by remember { mutableStateOf("") }
    var wisdomCategory by remember { mutableStateOf("General") }

    // Available categories
    val categories = listOf("General", "Personal", "Professional", "Health", "Relationships", "Philosophy", "Motivation")
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = GlassSurface,
                contentColor = StarWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "ADD NEW WISDOM",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElectricGreen
                )

                // Wisdom text field
                OutlinedTextField(
                    value = wisdomText,
                    onValueChange = { wisdomText = it },
                    label = { Text("Wisdom Text") },
                    placeholder = { Text("Enter your wisdom") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricGreen,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = ElectricGreen,
                        focusedLabelColor = ElectricGreen,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ),
                    minLines = 3
                )

                // Source field
                OutlinedTextField(
                    value = wisdomSource,
                    onValueChange = { wisdomSource = it },
                    label = { Text("Source (Optional)") },
                    placeholder = { Text("Author, book, etc.") },
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

                // Category field with dropdown
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = wisdomCategory,
                        onValueChange = { wisdomCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite,
                            cursorColor = NeonPink,
                            focusedLabelColor = NeonPink,
                            unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                        ),
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = {
                            TextButton(onClick = { showCategoryDropdown = !showCategoryDropdown }) {
                                Text(
                                    text = if (showCategoryDropdown) "Close" else "Select",
                                    color = NeonPink
                                )
                            }
                        }
                    )

                    // Category dropdown
                    if (showCategoryDropdown) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlassSurface.copy(alpha = 0.8f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                categories.forEach { category ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (category == wisdomCategory)
                                                    NeonPink.copy(alpha = 0.2f)
                                                else
                                                    Color.Transparent
                                            )
                                            .padding(12.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (category == wisdomCategory)
                                                    NeonPink.copy(alpha = 0.2f)
                                                else
                                                    Color.Transparent
                                            ),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        TextButton(
                                            onClick = {
                                                wisdomCategory = category
                                                showCategoryDropdown = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = category,
                                                color = if (category == wisdomCategory)
                                                    NeonPink
                                                else
                                                    StarWhite,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            if (wisdomText.isNotBlank()) {
                                onSave(
                                    wisdomText.trim(),
                                    wisdomSource.trim(),
                                    wisdomCategory.trim()
                                )
                            }
                        },
                        enabled = wisdomText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NebulaPurple
                        )
                    ) {
                        Text("SAVE")
                    }
                }
            }
        }
    }
}