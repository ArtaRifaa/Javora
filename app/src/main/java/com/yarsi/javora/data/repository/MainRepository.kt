package com.yarsi.javora.data.repository

import android.content.Context
import io.appwrite.services.Databases
import io.appwrite.services.Realtime
import io.appwrite.Query
import com.yarsi.javora.data.remote.AppwriteClient

class MainRepository(context: Context) {
    private val client = AppwriteClient.getClient(context)
    private val databases = Databases(client)
    private val realtime = Realtime(client)

    companion object {
        private const val DATABASE_ID = "javora"
        private const val COLLECTION_USERS = "users_profile"
        private const val COLLECTION_TOPICS = "topics"
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

    suspend fun getLeaderboard(): List<Map<String, Any>> {
        return try {
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_USERS,
                queries = listOf(
                    Query.orderDesc("total_xp"), // Gunakan total_xp untuk sorting
                    Query.limit(25) 
                ) 
            )
            
            response.documents.map { doc ->
                // Gunakan Map asli dari Appwrite dan tambahkan ID dokumen
                val data = doc.data.toMutableMap()
                data["\$id"] = doc.id
                data
            }
        } catch (e: Exception) {
            android.util.Log.e("MainRepo", "Gagal ambil leaderboard: ${e.message}")
            // Fallback jika total_xp tidak terindex
            try {
                val fallback = databases.listDocuments(
                    DATABASE_ID, COLLECTION_USERS,
                    listOf(Query.orderDesc("score"), Query.limit(25))
                )
                fallback.documents.map { doc ->
                    val data = doc.data.toMutableMap()
                    data["\$id"] = doc.id
                    data
                }
            } catch (e2: Exception) { emptyList() }
        }
    }

    suspend fun getUserRank(myScore: Int): Int {
        return try {
            val response = databases.listDocuments(
                DATABASE_ID, COLLECTION_USERS,
                listOf(Query.greaterThan("score", myScore))
            )
            (response.total + 1).toInt()
        } catch (e: Exception) { 1 }
    }

    fun subscribeToLeaderboard(onUpdate: () -> Unit) = realtime.subscribe(
        "databases.$DATABASE_ID.collections.$COLLECTION_USERS.documents"
    ) { 
        android.util.Log.d("MainRepo", "Realtime update received!")
        onUpdate() 
    }
}
