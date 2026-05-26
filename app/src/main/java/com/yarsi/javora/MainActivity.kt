package com.yarsi.javora
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarsi.javora.data.QuizQuestion
import com.yarsi.javora.data.remote.AiService
import com.yarsi.javora.ui.components.JavoraSplashScreen
import com.yarsi.javora.ui.screens.*
import com.yarsi.javora.ui.theme.JavoraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JavoraTheme {
                MainNavigation()
            }
        }
    }
}
//asasas
@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val appwriteService = remember { com.yarsi.javora.data.remote.AppwriteService(context) }
    var currentScreen by remember { mutableStateOf("splash") }
    var quizQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentTopic by remember { mutableStateOf("") }
    var progressMap by remember { mutableStateOf(mapOf<String, Float>()) }
    var userName by remember { mutableStateOf("Coders") }
    var userTotalXp by remember { mutableStateOf(0) }
    var userLevel by remember { mutableStateOf(1) }
    var leaderboardUsers by remember { mutableStateOf<List<com.yarsi.javora.data.RankUser>>(emptyList()) }

    val scope = rememberCoroutineScope()
    val aiService = remember { AiService() }

    suspend fun refreshUserData() {
        val userId = appwriteService.getCurrentUserId() ?: return
        val profile = appwriteService.getUserProfile(userId)
        val nameFromAccount = appwriteService.getCurrentUserName()
        
        if (profile != null && profile.containsKey("total_xp")) {
            android.util.Log.d("JavoraDebug", "Data dari Appwrite: $profile")
            userName = profile["full_name"]?.toString() ?: nameFromAccount ?: "Pemain"
            userTotalXp = profile["total_xp"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0
            userLevel = profile["level"]?.toString()?.toDoubleOrNull()?.toInt() ?: 1
            progressMap = appwriteService.parseProgressData(profile["progress_data"] as? String)
        } else if (profile == null) {
            userName = nameFromAccount ?: "Coders"
            userTotalXp = 0
            userLevel = 1
            progressMap = emptyMap()
        }
    }

    LaunchedEffect(Unit) {
        if (currentScreen == "splash") {
            delay(2500)
            currentScreen = "login"
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = com.yarsi.javora.ui.theme.JavoraDarkBg
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(durationMillis = 800),
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "splash" -> JavoraSplashScreen()
                    "login" -> LoginScreen(onLoginSuccess = { 
                        scope.launch {
                            delay(1500)
                            val userId = appwriteService.getCurrentUserId()
                            if (userId != null) {
                                val profile = appwriteService.getUserProfile(userId)
                                if (profile == null) {
                                    appwriteService.saveUserProfile(
                                        userId = userId,
                                        fullName = appwriteService.getCurrentUserName() ?: "aku",
                                        totalXp = 0,
                                        level = 1,
                                        title = "ANTUSIAS JAVA",
                                        score = 0,
                                        progressMap = emptyMap()
                                    )
                                }
                                refreshUserData()
                            }
                            currentScreen = "geranda" 
                        }
                    })
                    "geranda" -> HomeScreen(
                        userName = userName,
                        totalXp = userTotalXp,
                        level = userLevel,
                        progressMap = progressMap,
                        onTabSelected = { currentScreen = it.lowercase() },
                        onStartQuiz = { topic ->
                            if (currentScreen == "geranda") {
                                currentTopic = topic
                                currentScreen = "quiz_loading"
                                scope.launch {
                                    try {
                                        val questions = aiService.generateQuestions(topic)
                                        if (questions.isNotEmpty()) {
                                            quizQuestions = questions
                                            currentScreen = "quiz"
                                        } else {
                                            currentScreen = "geranda"
                                        }
                                    } catch (e: Exception) {
                                        currentScreen = "geranda"
                                    }
                                }
                            }
                        }
                    )
                    "quiz_loading" -> {
                        val tips = listOf(
                            "Tahukah kamu? Java awalnya bernama Oak.",
                            "Menganalisis soal Java terbaik untukmu...",
                            "Menyiapkan tantangan kode yang seru...",
                            "Hampir siap! Jangan lupa cek titik koma (;) ya.",
                            "Java digunakan di lebih dari 3 miliar perangkat."
                        )
                        var currentTip by remember { mutableStateOf(tips.random()) }
                        
                        LaunchedEffect(Unit) {
                            while(true) {
                                delay(3000)
                                currentTip = tips.random()
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                androidx.compose.material3.CircularProgressIndicator(color = com.yarsi.javora.ui.theme.JavoraOrange)
                                Spacer(modifier = Modifier.height(24.dp))
                                androidx.compose.material3.Text(
                                    text = currentTip,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontSize = 16.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                androidx.compose.material3.Text(
                                    text = "Menyusun 10 soal oleh AI...",
                                    color = androidx.compose.ui.graphics.Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    "quiz" -> QuizScreen(
                        questions = quizQuestions,
                        onTabSelected = { currentScreen = it.lowercase() },
                        onQuizComplete = { finalScore ->
                            val correctCount = finalScore / 100
                            val total = quizQuestions.size
                            val newProgress = correctCount.toFloat() / total
                            
                            val currentProgress = progressMap[currentTopic] ?: 0f
                            if (newProgress > currentProgress) {
                                val newMap = progressMap.toMutableMap().apply {
                                    put(currentTopic, newProgress)
                                }
                                progressMap = newMap
                                
                                scope.launch {
                                    val userId = appwriteService.getCurrentUserId()
                                    if (userId != null) {
                                        val totalXp = newMap.values.sumOf { (it * 1000).toInt() }
                                        val level = (totalXp / 500) + 1
                                        appwriteService.saveUserProfile(
                                            userId = userId,
                                            fullName = userName,
                                            totalXp = totalXp,
                                            level = level,
                                            title = "ANTUSIAS JAVA",
                                            score = totalXp, // Skor di papan peringkat mengikuti total XP
                                            progressMap = newMap
                                        )
                                        userTotalXp = totalXp
                                        userLevel = level
                                        refreshUserData()
                                    }
                                }
                            }

                            currentScreen = "geranda"
                        },
                        onQuizFailed = {
                            currentScreen = "geranda"
                        }
                    )
                    "rank" -> {
                        LaunchedEffect(Unit) {
                            scope.launch {
                                try {
                                    val rawData = appwriteService.getLeaderboard()
                                    leaderboardUsers = rawData.mapIndexed { index, map ->
                                        com.yarsi.javora.data.RankUser(
                                            rank = index + 1,
                                            name = map["full_name"] as? String ?: "Anonymous",
                                            score = ((map["score"] as? Number)?.toInt() ?: 0).toString(),
                                            subtitle = map["title"] as? String ?: "Coders"
                                        )
                                    }
                                } catch (e: Exception) { }
                            }
                        }
                        
                        if (leaderboardUsers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                androidx.compose.material3.CircularProgressIndicator(color = com.yarsi.javora.ui.theme.JavoraOrange)
                            }
                        } else {
                            RankScreen(
                                users = leaderboardUsers,
                                onTabSelected = { currentScreen = it.lowercase() }
                            )
                        }
                    }
                    "profile" -> ProfileScreen(
                        userName = userName,
                        totalXp = userTotalXp,
                        level = userLevel,
                        completedQuizzes = progressMap.values.count { it > 0f },
                        onTabSelected = { currentScreen = it.lowercase() },
                        onLogout = { 
                            progressMap = emptyMap()
                            userTotalXp = 0
                            userLevel = 1
                            currentScreen = "login" 
                        }
                    )
                    else -> HomeScreen(
                        userName = userName,
                        totalXp = userTotalXp,
                        level = userLevel,
                        progressMap = progressMap,
                        onTabSelected = { currentScreen = it.lowercase() }
                    )
                }
            }
        }
    }
}
