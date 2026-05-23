package com.yarsi.javora.data.remote

import android.content.Context
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import org.json.JSONObject

class AppwriteService(context: Context) {
    private val client = AppwriteClient.getClient(context)
    private val account = Account(client)
    private val databases = Databases(client)

    companion object {
        private const val DATABASE_ID = "javora"
        private const val COLLECTION_TOPICS = "topics"
        private const val COLLECTION_USERS = "users_profile"
        private const val COLLECTION_RANKINGS = "rankings"
        private const val BUCKET_AVATARS = "avatars"
    }

    fun getAvatarUrl(fileId: String): String {
        val projectId = "6a106f5b0004e155b70f"
        val endpoint = "https://sgp.cloud.appwrite.io/v1"
        return "$endpoint/storage/buckets/$BUCKET_AVATARS/files/$fileId/view?project=$projectId"
    }

    // --- AUTHENTICATION ---
    suspend fun getCurrentUserName(): String? {
        return try {
            val user = account.get()
            user.name
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCurrentUserId(): String? {
        return try {
            val user = account.get()
            user.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return getCurrentUserId() != null
    }

    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            try {
                account.deleteSession("current")
            } catch (e: Exception) { }

            account.createEmailPasswordSession(email, password)
            Result.success(true)
        } catch (e: AppwriteException) {
            android.util.Log.e("AppwriteService", "Login error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, name: String): Result<Boolean> {
        return try {
            account.create(ID.unique(), email, password, name)
            Result.success(true)
        } catch (e: AppwriteException) {
            android.util.Log.e("AppwriteService", "Signup error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun logout(): Boolean {
        return try {
            account.deleteSession("current")
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- DATA FETCHING ---

    suspend fun saveUserProfile(
        userId: String, 
        fullName: String, 
        totalXp: Int, 
        level: Int, 
        title: String,
        score: Int,
        progressMap: Map<String, Float>
    ): Boolean {
        return try {
            val json = JSONObject()
            progressMap.forEach { (key, value) -> json.put(key, value.toDouble()) }
            
            val data = mapOf(
                "user_id" to userId,
                "full_name" to fullName,
                "total_xp" to totalXp,
                "level" to level,
                "title" to title,
                "score" to score,
                "progress_data" to json.toString()
            )
            
            try {
                databases.updateDocument(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_USERS,
                    documentId = userId,
                    data = data
                )
            } catch (e: Exception) {
                databases.createDocument(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_USERS,
                    documentId = userId,
                    data = data
                )
            }
            true
        } catch (e: AppwriteException) {
            android.util.Log.e("AppwriteService", "Error saving profile: ${e.message}")
            false
        }
    }

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        android.util.Log.d("AppwriteService", "Mencari data untuk user_id: $userId")
        return try {
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_USERS,
                queries = listOf(Query.equal("user_id", userId))
            )
            
            if (response.documents.isNotEmpty()) {
                val data = response.documents[0].data
                android.util.Log.d("AppwriteService", "DATA BERHASIL DIAMBIL: $data")
                data
            } else {
                android.util.Log.d("AppwriteService", "DATA TIDAK DITEMUKAN untuk user_id: $userId")
                null
            }
        } catch (e: AppwriteException) {
            android.util.Log.e("AppwriteService", "GAGAL AKSES DATABASE: ${e.message}")
            null
        }
    }

    fun parseProgressData(jsonString: String?): Map<String, Float> {
        if (jsonString.isNullOrEmpty()) return emptyMap()
        return try {
            val json = JSONObject(jsonString)
            val map = mutableMapOf<String, Float>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.getDouble(key).toFloat()
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getTopics(): List<Map<String, Any>> {
        return try {
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TOPICS
            )
            response.documents.map { it.data }
        } catch (e: AppwriteException) {
            emptyList()
        }
    }

    suspend fun getLeaderboard(): List<Map<String, Any>> {
        return try {
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_USERS,
                queries = listOf(
                    Query.orderDesc("score"),
                    Query.limit(10)
                )
            )
            response.documents.map { it.data }
        } catch (e: AppwriteException) {
            emptyList()
        }
    }

    suspend fun getRankings(): List<Map<String, Any>> {
        return try {
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_RANKINGS,
                queries = listOf(Query.orderAsc("rank_number"))
            )
            response.documents.map { it.data }
        } catch (e: AppwriteException) {
            emptyList()
        }
    }
}
