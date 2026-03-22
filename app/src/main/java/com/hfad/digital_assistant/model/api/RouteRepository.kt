package com.hfad.digital_assistant.model.api

import com.hfad.digital_assistant.model.local.ModuleCompletionEntity
import com.hfad.digital_assistant.model.local.ModuleDao

class RouteRepository(
    private val api: RouteApi,
    private val dao: ModuleDao
) {

    suspend fun getModules(): List<Module> {
        return api.getModules()
    }

    suspend fun getCompletedModules(): Set<Int> {
        return dao.getCompletedModules().toSet()
    }

    suspend fun toggleCompleted(moduleId: Int) {
        val isCompleted = dao.isCompleted(moduleId)

        if (isCompleted) {
            dao.delete(moduleId)

            try {
                api.uncompleteModule(moduleId) // DELETE
            } catch (e: Exception) {
                // офлайн — просто оставляем локально
            }

        } else {
            dao.insert(ModuleCompletionEntity(moduleId))

            try {
                api.completeModule(moduleId) // POST
            } catch (e: Exception) {
                // офлайн
            }
        }
    }

    suspend fun syncWithServer() {

        val local = dao.getCompletedModules()
        val remote = api.getCompletedModules() // GET /completed/

        val remoteIds = remote.map { it.module }.toSet()

        // что есть локально, но нет на сервере → отправляем
        val toUpload = local.filter { it !in remoteIds }

        toUpload.forEach {
            try {
                api.completeModule(it)
            } catch (e: Exception) {}
        }

        // что есть на сервере, но нет локально → удаляем на сервере
        val toDelete = remoteIds.filter { it !in local }

        toDelete.forEach {
            try {
                api.uncompleteModule(it)
            } catch (e: Exception) {}
        }
    }
}