package com.yarsi.javora.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yarsi.javora.data.repository.AuthRepository
import com.yarsi.javora.data.repository.CatatanRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

class UserViewModel(
    private val authRepository: AuthRepository,
    private val catatanRepository: CatatanRepository,
    private val context: Context
) : ViewModel() {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("javora_user_data", Context.MODE_PRIVATE)
    
    private val _userName = mutableStateOf("...")
    val userName: State<String> = _userName

    private val _userTotalXp = mutableIntStateOf(0)
    val userTotalXp: State<Int> = _userTotalXp

    private val _userLevel = mutableIntStateOf(1)
    val userLevel: State<Int> = _userLevel

    private val _progressMap = mutableStateOf(mapOf<String, Float>())
    val progressMap: State<Map<String, Float>> = _progressMap

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // ==================== LOCAL CACHE DENGAN USER ID ====================
    
    private fun saveToLocal(userId: String, name: String, xp: Int, level: Int, progressMap: Map<String, Float>) {
        val progressJson = JSONObject()
        progressMap.forEach { (key, value) -> progressJson.put(key, value.toDouble()) }
        
        // Simpan dengan prefix userId agar tidak tertukar antar akun
        prefs.edit()
            .putString("${userId}_name", name)
            .putInt("${userId}_xp", xp)
            .putInt("${userId}_level", level)
            .putString("${userId}_progress", progressJson.toString())
            .putString("last_logged_in_uid", userId) // Catat siapa yang terakhir login
            .apply()
    }
    
    private fun loadFromLocal(userId: String): Boolean {
        val name = prefs.getString("${userId}_name", null) ?: return false
        val xp = prefs.getInt("${userId}_xp", 0)
        val level = prefs.getInt("${userId}_level", 1)
        val progressJson = prefs.getString("${userId}_progress", "{}")
        
        _userName.value = name
        _userTotalXp.intValue = xp
        _userLevel.intValue = level
        
        if (!progressJson.isNullOrEmpty()) {
            try {
                val json = JSONObject(progressJson)
                val map = mutableMapOf<String, Float>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = json.getDouble(key).toFloat()
                }
                _progressMap.value = map
            } catch (e: Exception) { }
        }
        return true
    }

    // ==================== LOAD DATA ====================
    
    suspend fun loadUserData() {
        _isLoading.value = true
        
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _isLoading.value = false
            return
        }

        // 1. Muat dari lokal khusus untuk user ini (Instan!)
        loadFromLocal(userId)
        
        // 2. Sinkronkan dengan Appwrite
        try {
            val nameFromAccount = authRepository.getCurrentUserName()
            val profile = catatanRepository.getUserProfile(userId)
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                // Update nama dari akun dulu agar tidak "Coders"
                if (nameFromAccount != null) {
                    _userName.value = nameFromAccount
                }

                if (profile != null && profile.isNotEmpty()) {
                    val rawXp = profile["total_xp"] ?: profile["score"] ?: 0
                    val remoteXp = when(rawXp) {
                        is Number -> rawXp.toInt()
                        is String -> rawXp.toDoubleOrNull()?.toInt() ?: 0
                        else -> 0
                    }
                    
                    val remoteName = profile["full_name"]?.toString() ?: nameFromAccount ?: _userName.value
                    val remoteLevel = ((remoteXp / 500) + 1).coerceAtLeast(1)
                    
                    val progressDataRaw = profile["progress_data"]?.toString()
                    val remoteProgress = catatanRepository.parseProgressData(progressDataRaw)
                    
                    _userName.value = remoteName
                    _userTotalXp.intValue = remoteXp
                    _userLevel.intValue = remoteLevel
                    _progressMap.value = remoteProgress
                    
                    // Update Cache Lokal khusus user ini
                    saveToLocal(userId, remoteName, remoteXp, remoteLevel, remoteProgress)
                } else if (nameFromAccount != null) {
                    // Jika profil belum ada, simpan nama akun ke lokal
                    saveToLocal(userId, nameFromAccount, _userTotalXp.intValue, _userLevel.intValue, _progressMap.value)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UserVM", "Sync Gagal: ${e.message}")
        }
        
        _isLoading.value = false
    }

    fun updateQuizResult(topic: String, finalScore: Int, totalQuestions: Int) {
        viewModelScope.launch {
            val correctCount = finalScore / 100
            val newProgress = correctCount.toFloat() / totalQuestions
            
            val currentProgress = _progressMap.value[topic] ?: 0f
            val newMap = _progressMap.value.toMutableMap().apply {
                put(topic, if (newProgress > currentProgress) newProgress else currentProgress)
            }
            
            val newTotalXp = _userTotalXp.intValue + finalScore
            val newLevel = (newTotalXp / 500) + 1
            
            _userTotalXp.intValue = newTotalXp
            _userLevel.intValue = newLevel
            _progressMap.value = newMap
            
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                // Simpan Lokal
                saveToLocal(userId, _userName.value, newTotalXp, newLevel, newMap)
                
                // Simpan Appwrite
                catatanRepository.saveUserProfile(userId, _userName.value, newTotalXp, newLevel, "ANTUSIAS JAVA", newTotalXp, newMap)
            }
        }
    }

    fun updatePartialProgress(topic: String, partialProgress: Float) {
        viewModelScope.launch {
            if (partialProgress <= 0f || topic.isEmpty()) return@launch
            
            val currentStored = _progressMap.value[topic] ?: 0f
            if (partialProgress > currentStored) {
                val newMap = _progressMap.value.toMutableMap().apply {
                    put(topic, partialProgress)
                }
                _progressMap.value = newMap
                
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    saveToLocal(userId, _userName.value, _userTotalXp.intValue, _userLevel.intValue, newMap)
                    catatanRepository.saveUserProfile(
                        userId = userId,
                        fullName = _userName.value,
                        totalXp = _userTotalXp.intValue,
                        level = _userLevel.intValue,
                        title = "ANTUSIAS JAVA",
                        score = _userTotalXp.intValue,
                        progressMap = newMap
                    )
                }
            }
        }
    }

    fun resetProgress(topic: String) {
        viewModelScope.launch {
            val newMap = _progressMap.value.toMutableMap().apply {
                remove(topic)
            }
            _progressMap.value = newMap
            
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                saveToLocal(userId, _userName.value, _userTotalXp.intValue, _userLevel.intValue, newMap)
                catatanRepository.saveUserProfile(
                    userId = userId,
                    fullName = _userName.value,
                    totalXp = _userTotalXp.intValue,
                    level = _userLevel.intValue,
                    title = "ANTUSIAS JAVA",
                    score = _userTotalXp.intValue,
                    progressMap = newMap
                )
            }
        }
    }

    fun logout() {
        // Jangan hapus semua cache, cukup reset state UI
        // Cache akun lain tetap aman di SharedPreferences
        _progressMap.value = emptyMap()
        _userTotalXp.intValue = 0
        _userLevel.intValue = 1
        _userName.value = "Coders"
    }
}
