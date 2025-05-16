package com.example.wisdomreminder.model

import java.time.LocalDateTime

/**
 * Represents a piece of wisdom that follows the 21/21 rule
 * (displayed 21 times over 21 days)
 */
data class Wisdom(
    val id: Long = System.currentTimeMillis(),
    val text: String,                      // The wisdom text content
    val source: String = "",               // Optional source/reference
    val category: String = "General",      // Category for organization
    val dateCreated: LocalDateTime = LocalDateTime.now(),
    val dateCompleted: LocalDateTime? = null,

    // 21/21 Rule tracking
    val isActive: Boolean = false,         // Whether this wisdom is currently active
    val startDate: LocalDateTime? = null,  // When the 21-day cycle started
    val currentDay: Int = 0,               // Current day in the 21-day cycle (1-21)
    val exposuresTotal: Int = 0,           // Total exposures so far (should reach 21*21=441)
    val exposuresToday: Int = 0,           // Exposures today (resets daily, target: 21/day)
    val lastExposureTime: LocalDateTime? = null, // Time of last exposure

    // Additional features
    val isFavorite: Boolean = false,
    val backgroundColor: String? = null,   // Optional color for display
    val fontStyle: String? = null,         // Optional font customization
    val imageBackground: String? = null    // Optional path to background image
)