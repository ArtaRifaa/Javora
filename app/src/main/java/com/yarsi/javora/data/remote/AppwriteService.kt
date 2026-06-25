package com.yarsi.javora.data.remote

import android.content.Context
import android.util.Log
import io.appwrite.services.Account
import io.appwrite.services.TablesDB
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import org.json.JSONObject

class AppwriteService(context: Context) {
    private val client = AppwriteClient.getClient(context)
    private val account = Account(client)
    private val tablesDB = TablesDB(client)

    companion object {
        private const val DATABASE_ID = "javora"
        private const val COLLECTION_USERS = "users_profile"
    }

    suspend fun getCurrentUserName(): String? = try { account.get().name } catch (e: Exception) { null }
    suspend fun getCurrentUserId(): String? = try { account.get().id } catch (e: Exception) { null }
    suspend fun isLoggedIn(): Boolean = getCurrentUserId() != null

    suspend fun login(email: String, password: String): Result<Boolean> = try {
        try { account.deleteSession("current") } catch (e: Exception) { }
        account.createEmailPasswordSession(email, password)
        Result.success(true)
    } catch (e: AppwriteException) { Result.failure(e) }

    suspend fun signUp(email: String, password: String, name: String): Result<Boolean> = try {
        account.create(ID.unique(), email, password, name)
        account.createEmailPasswordSession(email, password)
        Result.success(true)
    } catch (e: AppwriteException) { Result.failure(e) }

    suspend fun logout(): Boolean = try { account.deleteSession("current"); true } catch (e: Exception) { false }

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
            val progressJson = JSONObject()
            progressMap.forEach { (k, v) -> progressJson.put(k, v.toDouble()) }
            
            val data = mapOf(
                "user_id" to userId,
                "full_name" to fullName,
                "total_xp" to totalXp,
                "level" to level,
                "title" to title,
                "score" to score,
                "progress_data" to progressJson.toString()
            )
            
            val existing = tablesDB.listRows(
                databaseId = DATABASE_ID,
                tableId = COLLECTION_USERS,
                queries = listOf(Query.equal("user_id", userId))
            )

            if (existing.rows.isNotEmpty()) {
                tablesDB.updateRow(DATABASE_ID, COLLECTION_USERS, existing.rows[0].id, data)
            } else {
                tablesDB.createRow(DATABASE_ID, COLLECTION_USERS, ID.unique(), data)
            }
            true
        } catch (e: Exception) { 
            Log.e("JavoraDB", "Save error: ${e.message}")
            false 
        }
    }

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        return try {
            val response = tablesDB.listRows(
                databaseId = DATABASE_ID,
                tableId = COLLECTION_USERS,
                queries = listOf(Query.equal("user_id", userId))
            )
            if (response.rows.isNotEmpty()) response.rows[0].data else null
        } catch (e: Exception) { null }
    }

    fun parseProgressData(data: Any?): Map<String, Float> {
        if (data == null) return emptyMap()
        val map = mutableMapOf<String, Float>()
        try {
            when (data) {
                is Map<*, *> -> {
                    data.forEach { (k, v) -> map[k.toString()] = (v as? Number)?.toFloat() ?: 0f }
                }
                is String -> {
                    if (data.isEmpty() || data == "null") return emptyMap()
                    val json = JSONObject(data)
                    json.keys().forEach { k -> map[k] = json.optDouble(k, 0.0).toFloat() }
                }
            }
        } catch (e: Exception) { }
        return map
    }

    suspend fun getLeaderboard(): List<Map<String, Any>> = try {
        tablesDB.listRows(
            databaseId = DATABASE_ID,
            tableId = COLLECTION_USERS,
            queries = listOf(Query.orderDesc("score"), Query.limit(10))
        ).rows.map { it.data }
    } catch (e: Exception) { emptyList() }
}
