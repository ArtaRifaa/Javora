package com.yarsi.javora.data.repository

import android.content.Context
import io.appwrite.services.Account
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import com.yarsi.javora.data.remote.AppwriteClient

class AuthRepository(context: Context) {
    private val client = AppwriteClient.getClient(context)
    private val account = Account(client)

    suspend fun getCurrentUserId(): String? {
        return try {
            val user = account.get()
            user.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCurrentUserName(): String? {
        return try {
            val user = account.get()
            user.name
        } catch (e: Exception) {
            null
        }
    }

    suspend fun isLoggedIn(): Boolean = getCurrentUserId() != null

    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            // Hapus session lama jika ada
            try { account.deleteSession("current") } catch (e: Exception) { }
            
            account.createEmailPasswordSession(email, password)
            Result.success(true)
        } catch (e: AppwriteException) {
            android.util.Log.e("AuthRepository", "Login error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, name: String): Result<Boolean> {
        return try {
            account.create(ID.unique(), email, password, name)
            Result.success(true)
        } catch (e: AppwriteException) {
            android.util.Log.e("AuthRepository", "Signup error: ${e.message}")
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
}
