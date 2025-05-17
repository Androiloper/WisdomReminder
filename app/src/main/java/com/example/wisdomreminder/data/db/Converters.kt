package com.example.wisdomreminder.data.db

import android.util.Log
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Type converters for Room database
 */
class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let {
            try {
                LocalDateTime.parse(it, formatter)
            } catch (e: Exception) {
                Log.e("Converters", "Failed to parse LocalDateTime: $value", e)
                null // Handle parsing errors gracefully
            }
        }
    }
    // Add converters for new types if needed
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }
}