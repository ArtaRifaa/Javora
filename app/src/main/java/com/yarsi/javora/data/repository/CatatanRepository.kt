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
    private val prefs = context.getSharedPreferences("javora_data", Context.MODE_PRIVATE)

    companion object {
        private const val DATABASE_ID = "javora"
        private const val COLLECTION_USERS = "users_profile"
    }

    fun saveLocalData(xp: Int, level: Int, name: String, progressJson: String) {
        prefs.edit().apply {
            putInt("xp", xp)
            putInt("level", level)
            putString("name", name)
            putString("progress", progressJson)
            apply()
        }
    }

    fun getLocalXp(): Int = prefs.getInt("xp", 0)
    fun getLocalLevel(): Int = prefs.getInt("level", 1)
    fun getLocalName(): String = prefs.getString("name", "Pemain") ?: "Pemain"
    fun getLocalProgress(): String = prefs.getString("progress", "{}") ?: "{}"

    fun clearLocalData() { prefs.edit().clear().apply() }

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        return try {
            val response = databases.listDocuments(DATABASE_ID, COLLECTION_USERS, listOf(Query.equal("user_id", userId)))
            if (response.documents.isNotEmpty()) {
                val data = response.documents[0].data
                if (data.isNotEmpty()) return data
            }
            null
        } catch (e: Exception) { null }
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

        saveLocalData(totalXp, level, fullName, progressStr)

        val data = mapOf(
            "user_id" to userId,
            "full_name" to fullName,
            "total_xp" to totalXp,
            "level" to level,
            "title" to title,
            "score" to score,
            "progress_data" to progressStr
        )

        // PAKSA IZIN: Semua orang bisa baca, User ini bisa update
        val perms = listOf(
            Permission.read(Role.any()),
            Permission.update(Role.user(userId)),
            Permission.delete(Role.user(userId))
        )

        return try {
            // Coba update dulu
            try {
                databases.updateDocument(DATABASE_ID, COLLECTION_USERS, userId, data, perms)
            } catch (e: Exception) {
                // Jika gagal (belum ada), buat baru
                databases.createDocument(DATABASE_ID, COLLECTION_USERS, userId, data, perms)
            }
            true
        } catch (e: Exception) { 
            android.util.Log.e("CatatanRepo", "Gagal simpan: ${e.message}")
            false 
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
        } catch (e: Exception) { emptyMap() }
    }
}
