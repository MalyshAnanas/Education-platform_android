package com.hfad.digital_assistant.model.api

import com.hfad.digital_assistant.model.local.ModuleCompletionEntity
import com.hfad.digital_assistant.model.local.ModuleDao
import com.hfad.digital_assistant.model.local.ModuleEntity
import com.hfad.digital_assistant.model.local.ModuleItemEntity

class RouteRepository(
    private val api: RouteApi,
    private val dao: ModuleDao
) {

    suspend fun refreshModules(): List<Module> {

        val modules = api.getModules()

        saveModulesToDb(modules)

        return modules
    }

    public suspend fun saveModulesToDb(modules: List<Module>) {

        dao.clearItems()
        dao.clearModules()

        dao.insertModules(
            modules.map {
                ModuleEntity(
                    id = it.id,
                    title = it.title,
                    type = it.type,
                    order = it.order
                )
            }
        )

        dao.insertItems(
            modules.flatMap { module ->

                module.items.map { item ->

                    ModuleItemEntity(
                        id = item.id,
                        moduleId = module.id,
                        type = item.type,
                        text = item.text,
                        libraryFileSlug = item.library_file?.slug,
                        order = item.order
                    )
                }
            }
        )
    }

    suspend fun getModulesFromDb(): List<Module> {

        val modules = dao.getModules()
        val items = dao.getModuleItems()

        return modules.map { module ->

            Module(
                id = module.id,
                title = module.title,
                type = module.type,
                library_file = null,
                order = module.order,

                items = items
                    .filter { it.moduleId == module.id }
                    .sortedBy { it.order }
                    .map { item ->

                        ModuleItem(
                            id = item.id,
                            type = item.type,
                            text = item.text,
                            library_file = null,
                            order = item.order
                        )
                    }
            )
        }
    }

    suspend fun getModules(): List<Module> {

        return try {

            val modules = api.getModules()

            saveModulesToDb(modules)

            modules

        } catch (e: Exception) {

            getModulesFromDb()
        }
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
            } catch (e: Exception) {
            }
        }

        // что есть на сервере, но нет локально → удаляем на сервере
        val toDelete = remoteIds.filter { it !in local }

        toDelete.forEach {
            try {
                api.uncompleteModule(it)
            } catch (e: Exception) {
            }
        }
    }
}