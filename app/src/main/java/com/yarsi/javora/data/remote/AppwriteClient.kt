package com.yarsi.javora.data.remote

import android.content.Context
import io.appwrite.Client

object AppwriteClient {
    private var client: Client? = null

    // Ganti dengan detail proyek Appwrite Anda
    private const val ENDPOINT = "https://sgp.cloud.appwrite.io/v1"
    private const val PROJECT_ID = "6a106f5b0004e155b70f"

    fun getClient(context: Context): Client {
        return client ?: Client(context)
            .setEndpoint(ENDPOINT)
            .setProject(PROJECT_ID)
            .setSelfSigned(true) // Hapus jika sudah production
            .also { client = it }
    }
}
