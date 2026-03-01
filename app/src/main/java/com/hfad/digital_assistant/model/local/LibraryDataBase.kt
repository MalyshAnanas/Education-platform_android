package com.hfad.digital_assistant.model.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hfad.digital_assistant.model.local.LibraryFile
import com.hfad.digital_assistant.model.local.CategoryConverter

@Database(entities = [LibraryFile::class], version = 1, exportSchema = false)
@TypeConverters(CategoryConverter::class)
abstract class LibraryDatabase : RoomDatabase() {
    abstract val libraryDao: LibraryDao

    companion object {
        @Volatile
        private var INSTANCE: LibraryDatabase? = null

        fun getInstance(context: Context): LibraryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "library_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}