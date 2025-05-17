// Create this in a utils package, e.g., com.example.wisdomreminder.util.FlowExtensions.kt

package com.example.wisdomreminder.util

import android.util.Log
import androidx.room.Transaction
import com.example.wisdomreminder.BuildConfig
import com.example.wisdomreminder.data.db.entities.WisdomEntity
import com.example.wisdomreminder.model.Wisdom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Extension function to map entity lists to domain model lists with standardized error handling
 */
fun Flow<List<WisdomEntity>>.mapToWisdomListAndCatch(
    errorMessage: String,
    tag: String
): Flow<List<Wisdom>> {
    return this
        .onStart { if (BuildConfig.DEBUG) Log.d(tag, "Starting flow: $errorMessage") }
        .map { entities ->
            if (BuildConfig.DEBUG) Log.d(tag, "Mapping ${entities.size} entities to wisdom")
            entities.map { it.toWisdom() }
        }
        .catch { e ->
            Log.e(tag, errorMessage, e)
            emit(emptyList())
        }
}

/**
 * Extension function for count flows with standardized error handling
 */
fun Flow<Int>.mapToCountAndCatch(
    errorMessage: String,
    tag: String
): Flow<Int> {
    return this
        .catch { e ->
            Log.e(tag, errorMessage, e)
            emit(0) // Default count on error
        }
}