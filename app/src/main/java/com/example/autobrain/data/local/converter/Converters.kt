package com.example.autobrain.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room Type Converters for complex types
 */
class ListStringConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class MapStringConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromMap(value: Map<String, Any>?): String {
        return gson.toJson(value ?: emptyMap<String, Any>())
    }

    @TypeConverter
    fun toMap(value: String?): Map<String, Any> {
        if (value.isNullOrBlank()) return emptyMap()
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return try {
            gson.fromJson(value, mapType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
