package com.yarsi.javora.data.repository

import android.content.Context
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.appwrite.Query
import io.appwrite.Permission
import io.appwrite.Role
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.exceptions.AppwriteException
import com.yarsi.javora.data.remote.AppwriteClient
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class CatatanRepository(private val context: Context) {
    private val client = AppwriteClient.getClient(context)
    private val databases = Databases(client)
    private val storage = Storage(client)
    private val prefs = context.getSharedPreferences("javora_data", Context.MODE_PRIVATE)

    companion object {
        private const val DATABASE_ID = "javora"
        private const val COLLECTION_USERS = "users_profile"
        private const val BUCKET_AVATARS = "avatars"
    }

    suspend fun uploadAvatar(uri: android.net.Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "temp_avatar.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.use { input -> outputStream.use { output -> input.copyTo(output) } }

            val response = storage.createFile(
                bucketId = BUCKET_AVATARS,
                fileId = ID.unique(),
                file = InputFile.fromFile(file),
                permissions = listOf(Permission.read(Role.any()))
            )
            file.delete()
            response.id
        } catch (e: Exception) {
            android.util.Log.e("CatatanRepo", "Upload Gagal: ${e.message}")
            null
        }
    }

    fun saveLocalDataWithId(userId: String, xp: Int, level: Int, name: String, progressJson: String) {
        prefs.edit().apply {
            putInt("${userId}_xp", xp)
            putInt("${userId}_level", level)
            putString("${userId}_name", name)
            putString("${userId}_progress", progressJson)
            apply()
        }
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

    fun getLocalXp(userId: String? = null): Int = if (userId != null) prefs.getInt("${userId}_xp", 0) else prefs.getInt("xp", 0)
    fun getLocalLevel(userId: String? = null): Int = if (userId != null) prefs.getInt("${userId}_level", 1) else prefs.getInt("level", 1)
    fun getLocalName(userId: String? = null): String = if (userId != null) prefs.getString("${userId}_name", "Pemain") ?: "Pemain" else prefs.getString("name", "Pemain") ?: "Pemain"
    fun getLocalProgress(userId: String? = null): String = if (userId != null) prefs.getString("${userId}_progress", "{}") ?: "{}" else prefs.getString("progress", "{}") ?: "{}"

    fun clearLocalData() { prefs.edit().clear().apply() }

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        return try {
            try {
                val doc = databases.getDocument(DATABASE_ID, COLLECTION_USERS, userId)
                if (doc.data.isNotEmpty()) return doc.data
            } catch (e: Exception) { }

            val response = databases.listDocuments(
                DATABASE_ID, COLLECTION_USERS, 
                listOf(Query.equal("user_id", userId), Query.limit(1))
            )
            if (response.documents.isNotEmpty()) response.documents[0].data else null
        } catch (e: Exception) { null }
    }

    suspend fun saveUserProfile(
        userId: String,
        fullName: String,
        totalXp: Int,
        level: Int,
        title: String,
        score: Int,
        progressMap: Map<String, Float>,
        avatarId: String? = null
    ): Boolean {
        val json = JSONObject()
        progressMap.forEach { (key, value) -> json.put(key, value.toDouble()) }
        val progressStr = json.toString()

        saveLocalData(totalXp, level, fullName, progressStr)

        val data = mutableMapOf(
            "user_id" to userId,
            "full_name" to fullName,
            "total_xp" to totalXp,
            "level" to level,
            "title" to title,
            "score" to score,
            "progress_data" to progressStr
        )
        if (avatarId != null) data["avatar_id"] = avatarId

        // Izinkan SEMUA ORANG untuk membaca data ini agar muncul di Scoreboard
        // Dan izinkan HANYA PEMILIKNYA untuk mengupdate data ini
        val perms = listOf(
            Permission.read(Role.any()), 
            Permission.update(Role.user(userId))
        )

        android.util.Log.d("CatatanRepo", "Kirim ke DB: $DATABASE_ID, Kolom: total_xp=$totalXp, score=$score")

        return try {
            try {
                databases.updateDocument(DATABASE_ID, COLLECTION_USERS, userId, data, perms)
                android.util.Log.d("CatatanRepo", "Update Appwrite BERHASIL")
            } catch (e: Exception) {
                android.util.Log.d("CatatanRepo", "Update gagal, mencoba Create... Pesan: ${e.message}")
                databases.createDocument(DATABASE_ID, COLLECTION_USERS, userId, data, perms)
                android.util.Log.d("CatatanRepo", "Create Appwrite BERHASIL")
            }
            true
        } catch (e: Exception) { 
            android.util.Log.e("CatatanRepo", "EROR KRITIKAL: ${e.message}")
            // Jika muncul 'Collection not found', berarti ID COLLECTION_USERS salah.
            // Jika muncul 'Permission denied', berarti tombol Update di Dashboard belum diklik.
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
