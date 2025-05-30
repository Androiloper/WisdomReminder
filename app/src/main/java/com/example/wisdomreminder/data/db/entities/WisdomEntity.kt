package com.example.wisdomreminder.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.wisdomreminder.data.db.Converters
import com.example.wisdomreminder.model.Wisdom
import java.time.LocalDateTime

@Entity(
    tableName = "wisdom",
    indices = [
        Index("isActive"), // Improve queries that filter by active status
        Index("category"), // Improve category-based queries
        Index("dateCreated"), // Improve sorting by creation date
        Index("isFavorite"), // Index for favorite items
        Index("orderIndex")  // Index for ordering
    ]
)
@TypeConverters(Converters::class)
data class WisdomEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    val text: String,
    val source: String = "",
    val category: String = "General",
    val dateCreated: LocalDateTime = LocalDateTime.now(),
    val dateCompleted: LocalDateTime? = null,

    // 21/21 Rule tracking
    val isActive: Boolean = false,
    val startDate: LocalDateTime? = null,
    val currentDay: Int = 0,
    val exposuresTotal: Int = 0,
    val exposuresToday: Int = 0,
    val lastExposureTime: LocalDateTime? = null,

    // Additional features
    val isFavorite: Boolean = false,
    val orderIndex: Int = 0, // New field
    val backgroundColor: String? = null,
    val fontStyle: String? = null,
    val imageBackground: String? = null
) {
    fun toWisdom(): Wisdom = Wisdom(
        id = id,
        text = text,
        source = source,
        category = category,
        dateCreated = dateCreated,
        dateCompleted = dateCompleted,
        isActive = isActive,
        startDate = startDate,
        currentDay = currentDay,
        exposuresTotal = exposuresTotal,
        exposuresToday = exposuresToday,
        lastExposureTime = lastExposureTime,
        isFavorite = isFavorite,
        orderIndex = orderIndex, // Include new field
        backgroundColor = backgroundColor,
        fontStyle = fontStyle,
        imageBackground = imageBackground
    )

    companion object {
        fun fromWisdom(wisdom: Wisdom): WisdomEntity = WisdomEntity(
            id = if (wisdom.id == 0L && wisdom.text.isNotEmpty()) 0L else wisdom.id,
            text = wisdom.text,
            source = wisdom.source,
            category = wisdom.category,
            dateCreated = wisdom.dateCreated,
            dateCompleted = wisdom.dateCompleted,
            isActive = wisdom.isActive,
            startDate = wisdom.startDate,
            currentDay = wisdom.currentDay,
            exposuresTotal = wisdom.exposuresTotal,
            exposuresToday = wisdom.exposuresToday,
            lastExposureTime = wisdom.lastExposureTime,
            isFavorite = wisdom.isFavorite,
            orderIndex = wisdom.orderIndex, // Include new field
            backgroundColor = wisdom.backgroundColor,
            fontStyle = wisdom.fontStyle,
            imageBackground = wisdom.imageBackground
        )
    }
}