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
    }

    suspend fun getTopics() = try {
        databases.listDocuments(DATABASE_ID, COLLECTION_TOPICS).documents.map { it.data }
    } catch (e: Exception) { emptyList() }

    suspend fun getLeaderboard(): List<Map<String, Any>> {
        return try {
            val response = databases.listDocuments(
                DATABASE_ID, COLLECTION_USERS,
                listOf(Query.orderDesc("score"), Query.limit(10))
            )
            
            // Log detail untuk debugging
            android.util.Log.d("MainRepo", "Leaderboard: Dapat ${response.documents.size} data")
            response.documents.forEach { doc ->
                android.util.Log.d("MainRepo", "Item: ${doc.data["full_name"]} - Score: ${doc.data["score"]}")
            }
            
            response.documents.map { it.data }
        } catch (e: Exception) {
            android.util.Log.e("MainRepo", "Gagal leaderboard: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserRank(userId: String): Int {
        return try {
            // 1. Ambil skor user ini dari server
            val myDoc = try {
                databases.getDocument(DATABASE_ID, COLLECTION_USERS, userId).data
            } catch (e: Exception) {
                // Jika tidak ketemu via ID, cari via query user_id
                val list = databases.listDocuments(DATABASE_ID, COLLECTION_USERS, listOf(Query.equal("user_id", userId), Query.limit(1)))
                if (list.documents.isNotEmpty()) list.documents[0].data else null
            }

            val myScore = (myDoc?.get("score") as? Number)?.toInt() ?: 0
            
            // 2. Hitung berapa orang yang skornya LEBIH TINGGI dari user ini
            val higher = databases.listDocuments(
                DATABASE_ID, COLLECTION_USERS,
                listOf(Query.greaterThan("score", myScore))
            )
            
            // Jika user belum punya data di DB tapi ada orang lain, 
            // rank akan otomatis jadi jumlah orang + 1
            (higher.total + 1).toInt()
        } catch (e: Exception) {
            android.util.Log.e("MainRepo", "Gagal hitung rank: ${e.message}")
            1
        }
    }
}
