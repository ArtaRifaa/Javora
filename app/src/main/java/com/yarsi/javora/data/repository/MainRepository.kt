package com.yarsi.javora.data.repository

import android.content.Context
import io.appwrite.services.Databases
import io.appwrite.Query
import com.yarsi.javora.data.remote.AppwriteClient

class MainRepository(context: Context) {
    private val client = AppwriteClient.getClient(context)
    private val databases = Databases(client)

    companion object {
        private const val DATABASE_ID = "javora"
        private const val COLLECTION_TOPICS = "topics"
        private const val COLLECTION_USERS = "users_profile"
        private const val BUCKET_AVATARS = "avatars"
    }

    fun getAvatarUrl(fileId: String): String {
        val projectId = "6a106f5b0004e155b70f"
        val endpoint = "https://sgp.cloud.appwrite.io/v1"
        return "$endpoint/storage/buckets/$BUCKET_AVATARS/files/$fileId/view?project=$projectId"
    }

    suspend fun getTopics() = try {
        databases.listDocuments(DATABASE_ID, COLLECTION_TOPICS).documents.map { it.data }
    } catch (e: Exception) { emptyList() }

    suspend fun getLeaderboard() = try {
        databases.listDocuments(
            DATABASE_ID, COLLECTION_USERS,
            listOf(Query.orderDesc("score"), Query.limit(10))
        ).documents.map { it.data }
    } catch (e: Exception) { emptyList() }
}
