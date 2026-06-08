package com.yarsi.javora.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yarsi.javora.data.remote.AppwriteService
import kotlinx.coroutines.launch

class UserViewModel(private val appwriteService: AppwriteService) : ViewModel() {
    
    // State yang bertindak seperti LiveData di Compose
    private val _userName = mutableStateOf("Coders")
    val userName: State<String> = _userName

    private val _userTotalXp = mutableIntStateOf(0)
    val userTotalXp: State<Int> = _userTotalXp

    private val _userLevel = mutableIntStateOf(1)
    val userLevel: State<Int> = _userLevel

    private val _progressMap = mutableStateOf(mapOf<String, Float>())
    val progressMap: State<Map<String, Float>> = _progressMap

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = appwriteService.getCurrentUserId() ?: return@launch
            
            val profile = appwriteService.getUserProfile(userId)
            val nameFromAccount = appwriteService.getCurrentUserName()
            
            if (profile != null) {
                _userName.value = profile["full_name"]?.toString() ?: nameFromAccount ?: "Pemain"
                
                _userTotalXp.intValue = when (val xp = profile["total_xp"]) {
                    is Number -> xp.toInt()
                    is String -> xp.toDoubleOrNull()?.toInt() ?: 0
                    else -> 0
                }
                
                _userLevel.intValue = when (val lvl = profile["level"]) {
                    is Number -> lvl.toInt()
                    is String -> lvl.toDoubleOrNull()?.toInt() ?: 1
                    else -> 1
                }
                
                _progressMap.value = appwriteService.parseProgressData(profile["progress_data"] as? String)
            } else {
                if (nameFromAccount != null) _userName.value = nameFromAccount
            }
            _isLoading.value = false
        }
    }

    fun updateQuizResult(topic: String, finalScore: Int, totalQuestions: Int) {
        viewModelScope.launch {
            val correctCount = finalScore / 100
            val newProgress = correctCount.toFloat() / totalQuestions
            
            val currentProgress = _progressMap.value[topic] ?: 0f
            if (newProgress > currentProgress) {
                val newMap = _progressMap.value.toMutableMap().apply {
                    put(topic, newProgress)
                }
                
                // Update local UI state immediately (seperti LiveData)
                _progressMap.value = newMap
                val newTotalXp = newMap.values.sumOf { (it * 1000).toInt() }
                val newLevel = (newTotalXp / 500) + 1
                
                _userTotalXp.intValue = newTotalXp
                _userLevel.intValue = newLevel

                // Simpan ke DB
                val userId = appwriteService.getCurrentUserId()
                if (userId != null) {
                    appwriteService.saveUserProfile(
                        userId = userId,
                        fullName = _userName.value,
                        totalXp = newTotalXp,
                        level = newLevel,
                        title = "ANTUSIAS JAVA",
                        score = newTotalXp,
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
            
            val userId = appwriteService.getCurrentUserId()
            if (userId != null) {
                appwriteService.saveUserProfile(
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

    fun updatePartialProgress(topic: String, partialProgress: Float) {
        viewModelScope.launch {
            if (partialProgress <= 0f || topic.isEmpty()) return@launch
            
            val currentStored = _progressMap.value[topic] ?: 0f
            if (partialProgress > currentStored) {
                val newMap = _progressMap.value.toMutableMap().apply {
                    put(topic, partialProgress)
                }
                _progressMap.value = newMap
                
                val userId = appwriteService.getCurrentUserId()
                if (userId != null) {
                    appwriteService.saveUserProfile(
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

    fun logout() {
        _progressMap.value = emptyMap()
        _userTotalXp.intValue = 0
        _userLevel.intValue = 1
        _userName.value = "Coders"
    }
}
