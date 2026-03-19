package com.hfad.digital_assistant.model.api

class RouteRepository(private val api: RouteApi) {

    suspend fun getModules(): List<Module> {
        return api.getModules()
    }
}