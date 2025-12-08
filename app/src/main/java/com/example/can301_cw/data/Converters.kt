package com.example.can301_cw.data

import androidx.room.TypeConverter
import com.example.can301_cw.model.ApiResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: String?): MutableList<String>? {
        if (value == null) return null
        val listType = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toStringList(list: MutableList<String>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromApiResponse(value: String?): ApiResponse? {
        if (value == null) return null
        val type = object : TypeToken<ApiResponse>() {}.type
        val response: ApiResponse? = gson.fromJson(value, type)
        
        // Fix for existing data where taskStatus might be missing (null due to Gson unsafe allocation)
        response?.schedule?.tasks?.forEach { task ->
            // Use reflection or unsafe check to see if it's null, or just reassignment if it appears null
            // In Kotlin, accessing a null field typed as non-null might not throw immediately on Android/JVM
            // but effectively we want to ensure it's PENDING.
            @Suppress("SENSELESS_COMPARISON")
            if (task.taskStatus == null) {
                task.taskStatus = com.example.can301_cw.model.TaskStatus.PENDING
            }

            @Suppress("SENSELESS_COMPARISON")
            if (task.id == null) {
                // Generate deterministic ID based on content to ensure consistency across reads
                // until the object is saved back to DB with the ID.
                // Using startTime, theme and category as seed.
                val uniqueString = "${task.startTime}-${task.theme}-${task.category}"
                task.id = java.util.UUID.nameUUIDFromBytes(uniqueString.toByteArray()).toString()
            }
        }
        return response
    }

    @TypeConverter
    fun toApiResponse(apiResponse: ApiResponse?): String? {
        return gson.toJson(apiResponse)
    }
}
