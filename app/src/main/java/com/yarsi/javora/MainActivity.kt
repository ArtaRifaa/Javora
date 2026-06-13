package com.yarsi.javora

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarsi.javora.data.QuestionRepository
import com.yarsi.javora.data.QuizQuestion
import com.yarsi.javora.data.repository.AuthRepository
import com.yarsi.javora.data.repository.CatatanRepository
import com.yarsi.javora.data.repository.MainRepository
import com.yarsi.javora.ui.components.JavoraSplashScreen
import com.yarsi.javora.ui.screens.*
import com.yarsi.javora.ui.theme.JavoraDarkBg
import com.yarsi.javora.ui.theme.JavoraOrange
import com.yarsi.javora.ui.theme.JavoraTheme
import com.yarsi.javora.ui.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val catatanRepository = remember { CatatanRepository(context) }
    val mainRepository = remember { MainRepository(context) }

    val userViewModel = remember {
        UserViewModel(authRepository, catatanRepository, mainRepository, context)
    }

    var currentScreen by remember { mutableStateOf("splash") }
    var quizQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentTopic by remember { mutableStateOf("") }

    val userName by userViewModel.userName
    val userTotalXp by userViewModel.userTotalXp
    val userLevel by userViewModel.userLevel
    val progressMap by userViewModel.progressMap
    val userRank by userViewModel.userRank

    var leaderboardUsers by remember { mutableStateOf<List<com.yarsi.javora.data.RankUser>>(emptyList()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (currentScreen == "splash") {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                userViewModel.loadUserData()
                userViewModel.loadUserRank()
                currentScreen = "geranda"
            } else {
                delay(2000)
                currentScreen = "login"
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = JavoraDarkBg
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(durationMillis = 800),
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "splash" -> JavoraSplashScreen()
                    "login" -> LoginScreen(
                        authRepository = authRepository,
                        onLoginSuccess = { 
                            scope.launch {
                                userViewModel.loadUserData()
                                delay(500)
                                currentScreen = "geranda" 
                            }
                        }
                    )
                    "quiz_loading" -> QuizLoadingScreen()
                    "quiz" -> QuizScreen(
                        topicName = currentTopic,
                        questions = quizQuestions,
                        progressMap = progressMap,
                        onExit = { partialProgress ->
                            userViewModel.updatePartialProgress(currentTopic, partialProgress)
                            quizQuestions = emptyList()
                            currentTopic = ""
                            currentScreen = "geranda" 
                        },
                        onContinueQuiz = { topic ->
                            currentTopic = topic
                            currentScreen = "quiz_loading"
                            scope.launch {
                                val questions = QuestionRepository.getQuestionsForTopic(topic)
                                if (questions.isNotEmpty()) {
                                    quizQuestions = questions
                                    currentScreen = "quiz"
                                } else {
                                    currentScreen = "quiz" 
                                    Toast.makeText(context, "Materi kuis belum tersedia.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onResetProgress = { topic ->
                            userViewModel.resetProgress(topic)
                        },
                        onQuizComplete = { finalScore ->
                            userViewModel.updateQuizResult(currentTopic, finalScore, quizQuestions.size)
                            quizQuestions = emptyList()
                            currentTopic = ""
                            currentScreen = "geranda"
                        },
                        onQuizFailed = {
                            quizQuestions = emptyList()
                            currentTopic = ""
                            currentScreen = "geranda"
                        }
                    )
                    "rank" -> RankNavigationWrapper(
                        authRepository = authRepository,
                        mainRepository = mainRepository,
                        userViewModel = userViewModel,
                        userTotalXp = userTotalXp,
                        userRank = userRank,
                        onNavigate = { currentScreen = it }
                    )
                    "profile" -> ProfileScreen(
                        userName = userName,
                        totalXp = userTotalXp,
                        level = userLevel,
                        completedQuizzes = progressMap.values.count { it > 0f },
                        userRank = if (userRank > 0) "#$userRank" else "-",
                        authRepository = authRepository,
                        onTabSelected = { currentScreen = it.lowercase() },
                        onLogout = {
                            userViewModel.logout()
                            currentScreen = "login"
                        }
                    )
                    else -> HomeScreen(
                        userName = userName,
                        totalXp = userTotalXp,
                        level = userLevel,
                        userRank = if (userRank > 0) "#$userRank" else "-",
                        progressMap = progressMap,
                        onTabSelected = { currentScreen = it.lowercase() },
                        onStartQuiz = { topic ->
                            currentTopic = topic
                            currentScreen = "quiz_loading"
                            scope.launch {
                                delay(1500)
                                val questions = QuestionRepository.getQuestionsForTopic(topic)
                                if (questions.isNotEmpty()) {
                                    quizQuestions = questions
                                    currentScreen = "quiz"
                                } else {
                                    currentScreen = "geranda"
                                    Toast.makeText(context, "Materi kuis belum tersedia.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuizLoadingScreen() {
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

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = JavoraOrange)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = currentTip,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Menyiapkan tantangan kuis untukmu...",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun RankNavigationWrapper(
    authRepository: AuthRepository,
    mainRepository: MainRepository,
    userViewModel: UserViewModel,
    userTotalXp: Int,
    userRank: Int,
    onNavigate: (String) -> Unit
) {
    var leaderboardUsers by remember { mutableStateOf<List<com.yarsi.javora.data.RankUser>>(emptyList()) }
    var rankLoadDone by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun loadData(isRefresh: Boolean = false) {
        if (isRefresh) isRefreshing = true
        loadError = null
        try {
            val userId = authRepository.getCurrentUserId()
            val rawData = mainRepository.getLeaderboard()
            
            leaderboardUsers = rawData.mapIndexed { index, map ->
                val rawName = map["full_name"]?.toString()
                val rawScore = map["score"] ?: map["total_xp"] ?: 0
                
                com.yarsi.javora.data.RankUser(
                    rank = index + 1,
                    name = when {
                        !rawName.isNullOrEmpty() -> rawName
                        map["user_id"] == userId -> userViewModel.userName.value
                        else -> "Pemain #${index+1}"
                    },
                    score = when(rawScore) {
                        is Number -> rawScore.toInt().toString()
                        is String -> rawScore.toDoubleOrNull()?.toInt()?.toString() ?: "0"
                        else -> "0"
                    },
                    subtitle = if (map.isEmpty()) "Gagal membaca profil" else "Java Coder",
                    isCurrentUser = map["user_id"] == userId
                )
            }
            userViewModel.loadUserRank()
        } catch (e: Exception) {
            loadError = "Gagal memuat peringkat."
        } finally {
            rankLoadDone = true
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    when {
        !rankLoadDone -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = JavoraOrange)
            }
        }
        loadError != null -> {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, null, tint = JavoraOrange, modifier = Modifier.size(56.dp))
                    Text("Error", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(loadError!!, color = Color.Gray, textAlign = TextAlign.Center)
                    Button(onClick = { scope.launch { loadData(true) } }, colors = ButtonDefaults.buttonColors(JavoraOrange)) {
                        Text("Coba Lagi")
                    }
                }
            }
        }
        else -> RankScreen(
            users = leaderboardUsers,
            currentUserRank = if (userRank > 0) userRank else null,
            currentUserScore = userTotalXp.toString(),
            isRefreshing = isRefreshing,
            onRefresh = { scope.launch { loadData(true) } },
            onTabSelected = { onNavigate(it.lowercase()) }
        )
    }
}
