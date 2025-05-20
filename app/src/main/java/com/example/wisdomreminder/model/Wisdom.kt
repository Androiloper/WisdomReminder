package com.example.wisdomreminder.model

/**
 * Simplified Wisdom model class with basic fields
 */
data class Wisdom(
    val id: Long = 0,
    val title: String,
    val text: String,
    val category: String = "General"
)