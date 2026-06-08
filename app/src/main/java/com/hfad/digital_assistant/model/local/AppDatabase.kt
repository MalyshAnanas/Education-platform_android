package com.hfad.digital_assistant.model.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hfad.digital_assistant.model.local.practicum.PendingPracticumAnswerEntity
import com.hfad.digital_assistant.model.local.practicum.PracticumAnswerEntity
import com.hfad.digital_assistant.model.local.practicum.PracticumCaseEntity
import com.hfad.digital_assistant.model.local.practicum.PracticumDao

@Database(
    entities = [

        ModuleCompletionEntity::class,
        ModuleEntity::class,
        ModuleItemEntity::class,

        PracticumCaseEntity::class,
        PracticumAnswerEntity::class,
        PendingPracticumAnswerEntity::class

    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moduleDao(): ModuleDao

    abstract fun practicumDao(): PracticumDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}