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
        private const val COLLECTION_USERS = "users_profile"
        private const val COLLECTION_TOPICS = "topics"
    }

    suspend fun getTopics() = try {
        databases.listDocuments(DATABASE_ID, COLLECTION_TOPICS).documents.map { it.data }
    } catch (e: Exception) { emptyList() }

    suspend fun getLeaderboard(): List<Map<String, Any>> {
        return try {
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_USERS,
                queries = listOf(Query.orderDesc("score"), Query.limit(20)) 
            )
            
            android.util.Log.d("MainRepo", "--- LEADERBOARD FETCH SUCCESS ---")
            android.util.Log.d("MainRepo", "Total Docs: ${response.documents.size}")
            
            response.documents.forEachIndexed { i, doc ->
                android.util.Log.d("MainRepo", "Doc #$i ID: ${doc.id}")
                android.util.Log.d("MainRepo", "Doc #$i Data: ${doc.data}")
                if (doc.data.isEmpty()) {
                    android.util.Log.e("MainRepo", "DOKUMEN ${doc.id} KOSONG! Periksa Settings -> Permissions -> Any (Read)!")
                }
            }
            
            response.documents.map { it.data }
        } catch (e: Exception) {
            android.util.Log.e("MainRepo", "ERROR APPWRITE: ${e.message}")
            emptyList()
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
}
