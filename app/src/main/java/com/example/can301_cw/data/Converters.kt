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
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toApiResponse(apiResponse: ApiResponse?): String? {
        return gson.toJson(apiResponse)
    }
}
