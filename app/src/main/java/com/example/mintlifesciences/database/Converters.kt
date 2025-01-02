package com.example.mintlifesciences.database

import androidx.room.TypeConverter
import com.example.mintlifesciences.addDoctor.Group
import com.example.mintlifesciences.model.FeedbackData
import com.example.mintlifesciences.model.Medicine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GroupConverter {
    @TypeConverter
    fun fromGroupList(groups: List<Group>): String = Gson().toJson(groups)

    @TypeConverter
    fun toGroupList(data: String): List<Group> {
        val type = object : TypeToken<List<Group>>() {}.type
        return Gson().fromJson(data, type)
    }
}
//
//class MedicineConverter {
//    private val gson = Gson()
//
//    @TypeConverter
//    fun fromMedicineList(medicines: List<Medicine>?): String {
//        return gson.toJson(medicines ?: emptyList())
//    }
//
//    @TypeConverter
//    fun toMedicineList(data: String?): List<Medicine> {
//        val type = object : TypeToken<List<Medicine>>() {}.type
//        return gson.fromJson(data ?: "[]", type)
//    }
//}

class FeedbackConverter {
    @TypeConverter
    fun fromFeedbackList(feedback: List<FeedbackData>): String = Gson().toJson(feedback)

    @TypeConverter
    fun toFeedbackList(data: String): List<FeedbackData> {
        val type = object : TypeToken<List<FeedbackData>>() {}.type
        return Gson().fromJson(data, type)
    }
}
