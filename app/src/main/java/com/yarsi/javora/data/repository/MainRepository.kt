package com.yarsi.javora.data.repository

import android.content.Context
import io.appwrite.services.Databases
import io.appwrite.services.TablesDB
import io.appwrite.services.Realtime
import io.appwrite.Query
import com.yarsi.javora.data.remote.AppwriteClient

class MainRepository(context: Context) {
    private val client = AppwriteClient.getClient(context)
    // Appwrite SDK 24+ pakai TablesDB (bukan legacy Databases)
    private val tablesDB = TablesDB(client)
    private val databases = Databases(client) // fallback kalau DB masih pakai Collections
    private val realtime = Realtime(client)

    companion object {
        private const val DATABASE_ID = "javora"
        private const val TABLE_USERS = "users_profile"
        private const val TABLE_TOPICS = "topics"
        private const val BUCKET_AVATARS = "avatars"
    }

    fun getAvatarUrl(fileId: String): String {
        val projectId = "6a106f5b0004e155b70f"
        val endpoint = "https://sgp.cloud.appwrite.io/v1"
        return "$endpoint/storage/buckets/$BUCKET_AVATARS/files/$fileId/view?project=$projectId"
    }

    suspend fun getTopics(): List<Map<String, Any>> = try {
        // TablesDB.listRows → list rows dari TABLE
        val response = tablesDB.listRows(
            databaseId = DATABASE_ID,
            tableId = TABLE_TOPICS
        )
        response.rows.map { it.data }
    } catch (e: Exception) {
        android.util.Log.e("MainRepo", "getTopics (TablesDB) gagal: ${e.message}, coba fallback Databases")
        // Fallback ke legacy Collections API
        try {
            databases.listDocuments(DATABASE_ID, "topics").documents.map { it.data }
        } catch (e2: Exception) { emptyList() }
    }

    private fun parseScore(raw: Any?): Int = when (raw) {
        is Number -> raw.toInt()
        is String -> raw.filter { it.isDigit() }.toIntOrNull() ?: 0
        else -> 0
    }

    suspend fun getLeaderboard(): List<Map<String, Any>> {
        val row = tablesDB.getRow(
            databaseId = DATABASE_ID,
            tableId = TABLE_USERS,
            rowId = "6a3644120015e31868b4"
        )

        android.util.Log.e("TEST", "ID=${row.id}")
        android.util.Log.e("TEST", "DATA=${row.data}")
        android.util.Log.e("TEST", "FULL=${row}")

        android.util.Log.e("GETROW", row.toString())
        android.util.Log.e("GETROW", row.data.toString())
        android.util.Log.e("TEST123", "GET LEADERBOARD DIPANGGIL")

        return try {
            val response = tablesDB.listRows(
                databaseId = DATABASE_ID,
                tableId = TABLE_USERS,
                queries = listOf(Query.limit(100))
            )

            android.util.Log.e("TEST123", "Jumlah Row = ${response.rows.size}")

            response.rows.forEachIndexed { index, row ->
                android.util.Log.e("ROWDATA", row.toString())
            }

            response.rows.map { row ->
                val data = row.data.toMutableMap()
                data["\$id"] = row.id
                data
            }.sortedByDescending { map ->
                val raw = map["total_xp"] ?: map["score"]
                parseScore(raw)
            }

        } catch (e: Exception) {
            android.util.Log.e("TEST123", "ERROR = ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getUserRank(myScore: Int): Int {
        return try {
            val response = tablesDB.listRows(
                DATABASE_ID, TABLE_USERS,
                listOf(Query.greaterThan("score", myScore))
            )
            (response.total + 1).toInt()
        } catch (e: Exception) {
            // Fallback ke legacy Collections
            try {
                val response = databases.listDocuments(
                    DATABASE_ID, TABLE_USERS,
                    listOf(Query.greaterThan("score", myScore))
                )
                (response.total + 1).toInt()
            } catch (e2: Exception) { 1 }
        }
    }

    fun subscribeToLeaderboard(onUpdate: () -> Unit) = realtime.subscribe(
        // Appwrite 1.6+ pakai endpoint "tables", bukan "collections"
        "databases.$DATABASE_ID.tables.$TABLE_USERS.rows"
    ) {
        android.util.Log.d("MainRepo", "Realtime update received!")
        onUpdate()
    }
}
