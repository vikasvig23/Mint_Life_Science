package com.example.mintlifesciences.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DoctorEntity::class], version = 1, exportSchema = false)
@TypeConverters(GroupConverter::class, FeedbackConverter::class)
abstract class DoctorDatabase : RoomDatabase() {
    abstract fun doctorDao(): DoctorDao

    companion object {
        @Volatile
        private var INSTANCE: DoctorDatabase? = null

        fun getDatabase(context: Context): DoctorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DoctorDatabase::class.java,
                    "doctor_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
