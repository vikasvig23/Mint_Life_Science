package com.mintlifescience.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DoctorEntity::class, MedicineEntity::class], version = 3, exportSchema = false)
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
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_medicine_table_doctorName` ON `medicine_table` (`doctorName`)"
        )
    }
}

// Migration from version 1 to 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `medicine_table` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `image` TEXT, `name` TEXT, `salt` TEXT, `description` TEXT,
                `uses` TEXT, `mrp` TEXT, `videoUrl` TEXT, `pdfUrl` TEXT, `doctorName` TEXT NOT NULL
            )
        """)
    }
}
