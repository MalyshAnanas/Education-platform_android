package com.hfad.digital_assistant.model.api

import com.hfad.digital_assistant.model.local.LibraryFile
import okhttp3.MultipartBody
import okhttp3.RequestBody

class RemoteLibraryRepository(private val api: LibraryApi) {
    suspend fun getAllFiles(): List<LibraryFile> = api.getFiles()
    suspend fun getFile(slug: String): LibraryFile = api.getFile(slug)
    suspend fun uploadFile(
        title: RequestBody,
        description: RequestBody,
        file: MultipartBody.Part
    ): LibraryFile {
        return api.uploadFile(title, description, file)
    }
}