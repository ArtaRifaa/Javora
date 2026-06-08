package com.yarsi.javora.data.repository

import android.content.Context
import io.appwrite.services.Databases
import io.appwrite.Query
import io.appwrite.Permission
import io.appwrite.Role
import io.appwrite.exceptions.AppwriteException
import com.yarsi.javora.data.remote.AppwriteClient
import org.json.JSONObject

class CatatanRepository(context: Context) {
    private val client = AppwriteClient.getClient(context)
    private val databases = Databases(client)
    
    // Inisialisasi SharedPreferences
    private val prefs = context.getSharedPreferences("javora_data", Context.MODE_PRIVATE)

    companion object {
        private const val DATABASE_ID = "javora"
        private const val COLLECTION_USERS = "users_profile"
    }

    // --- LOKAL STORAGE (SHARED PREFERENCES) ---
    
    fun saveLocalData(xp: Int, level: Int, name: String, progressJson: String) {
        prefs.edit().apply {
            putInt("xp", xp)
            putInt("level", level)
            putString("name", name)
            putString("progress", progressJson)
            apply()
        }
        android.util.Log.d("CatatanRepo", "Data disimpan ke LOKAL (SharedPrefs)")
    }

    fun getLocalXp(): Int = prefs.getInt("xp", 0)
    fun getLocalLevel(): Int = prefs.getInt("level", 1)
    fun getLocalName(): String = prefs.getString("name", "Coders") ?: "Coders"
    fun getLocalProgress(): String = prefs.getString("progress", "{}") ?: "{}"

    fun clearLocalData() {
        prefs.edit().clear().apply()
    }

    // --- REMOTE STORAGE (APPWRITE) ---

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        return try {
            val document = databases.getDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_USERS,
                documentId = userId
            )
            document.data
        } catch (e: Exception) {
            try {
                val queryResponse = databases.listDocuments(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_USERS,
                    queries = listOf(Query.equal("user_id", userId))
                )
                if (queryResponse.documents.isNotEmpty()) queryResponse.documents[0].data else null
            } catch (e2: Exception) { null }
        }
    }

    suspend fun saveUserProfile(
        userId: String,
        fullName: String,
        totalXp: Int,
        level: Int,
        title: String,
        score: Int,
        progressMap: Map<String, Float>
    ): Boolean {
        val json = JSONObject()
        progressMap.forEach { (key, value) -> json.put(key, value.toDouble()) }
        val progressStr = json.toString()

        // 1. Simpan ke LOKAL dulu (Instan!)
        saveLocalData(totalXp, level, fullName, progressStr)

        // 2. Simpan ke Appwrite
        return try {
            val data = mapOf(
                "user_id" to userId,
                "full_name" to fullName,
                "total_xp" to totalXp,
                "level" to level,
                "title" to title,
                "score" to score,
                "progress_data" to progressStr
            )
            val perms = listOf(Permission.read(Role.any()), Permission.update(Role.any()))
            try {
                databases.updateDocument(DATABASE_ID, COLLECTION_USERS, userId, data, perms)
            } catch (e: Exception) {
                databases.createDocument(DATABASE_ID, COLLECTION_USERS, userId, data, perms)
            }
            true
        } catch (e: Exception) { false }
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
        } catch (e: Exception) { emptyMap() }
    }
}
