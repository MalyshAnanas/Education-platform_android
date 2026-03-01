package com.hfad.digital_assistant.model.api

import com.hfad.digital_assistant.model.local.LibraryFile

class RemoteLibraryRepository(private val api: LibraryApi) {
    suspend fun getAllFiles(): List<LibraryFile> = api.getFiles()
    suspend fun getFile(slug: String): LibraryFile = api.getFile(slug)
}