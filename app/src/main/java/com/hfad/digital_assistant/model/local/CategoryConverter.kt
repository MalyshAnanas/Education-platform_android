package com.hfad.digital_assistant.model.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hfad.digital_assistant.model.local.Category

class CategoryConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromCategoryList(value: List<Category>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCategoryList(value: String): List<Category> {
        val listType = object : TypeToken<List<Category>>() {}.type
        return gson.fromJson(value, listType)
    }
}