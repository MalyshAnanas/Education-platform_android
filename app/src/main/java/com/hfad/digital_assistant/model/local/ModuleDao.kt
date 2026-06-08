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

    // modules

    @Query("SELECT * FROM modules ORDER BY `order`")
    suspend fun getModules(): List<ModuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleEntity>)

    @Query("DELETE FROM modules")
    suspend fun clearModules()


// module items

    @Query("SELECT * FROM module_items")
    suspend fun getModuleItems(): List<ModuleItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ModuleItemEntity>)

    @Query("DELETE FROM module_items")
    suspend fun clearItems()
}