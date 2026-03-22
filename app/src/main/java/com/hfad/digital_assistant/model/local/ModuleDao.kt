package com.hfad.digital_assistant.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ModuleDao {

    @Query("SELECT moduleId FROM completed_modules")
    suspend fun getCompletedModules(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ModuleCompletionEntity)

    @Query("DELETE FROM completed_modules WHERE moduleId = :id")
    suspend fun delete(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM completed_modules WHERE moduleId = :id)")
    suspend fun isCompleted(id: Int): Boolean
}