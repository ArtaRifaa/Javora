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
    val userAvatar by userViewModel.userAvatar
    val userTotalXp by userViewModel.userTotalXp
    val userLevel by userViewModel.userLevel
    val progressMap by userViewModel.progressMap
    val userRank by userViewModel.userRank

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

    LaunchedEffect(currentScreen) {
        if (currentScreen == "rank") {
            userViewModel.loadUserRank()
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
                        avatarUrl = userAvatar,
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
                            scope.launch {
                                userViewModel.updateQuizResult(currentTopic, finalScore, quizQuestions.size)
                                delay(500)
                                quizQuestions = emptyList()
                                currentTopic = ""
                                currentScreen = "geranda"
                            }
                        },
                        onQuizFailed = {
                            quizQuestions = emptyList()
                            currentTopic = ""
                            currentScreen = "geranda"
                        }
                    )
                    "rank" -> {
                        val userId = remember { mutableStateOf<String?>(null) }
                        LaunchedEffect(Unit) { userId.value = authRepository.getCurrentUserId() }
                        
                        RankNavigationWrapper(
                            authRepository = authRepository,
                            mainRepository = mainRepository,
                            userViewModel = userViewModel,
                            userTotalXp = userTotalXp,
                            userAvatar = userAvatar,
                            userRank = userRank,
                            currentUserId = userId.value ?: "",
                            onNavigate = { currentScreen = it }
                        )
                    }
                    "profile" -> ProfileScreen(
                        userName = userName,
                        avatarUrl = userAvatar,
                        totalXp = userTotalXp,
                        level = userLevel,
                        completedQuizzes = progressMap.values.count { it > 0f },
                        userRank = if (userRank > 0) "#$userRank" else "-",
                        authRepository = authRepository,
                        onUpdateName = { userViewModel.updateUserName(it) },
                        onUpdateAvatar = { userViewModel.updateUserAvatar(it) },
                        onTabSelected = { currentScreen = it.lowercase() },
                        onLogout = {
                            userViewModel.logout()
                            currentScreen = "login"
                        }
                    )
                    else -> HomeScreen(
                        userName = userName,
                        avatarUrl = userAvatar,
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
    userAvatar: String?,
    userRank: Int,
    currentUserId: String,
    onNavigate: (String) -> Unit
) {
    var leaderboardUsers by remember { mutableStateOf<List<com.yarsi.javora.data.RankUser>>(emptyList()) }
    var rankLoadDone by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun loadData(isRefresh: Boolean = false) {
        android.util.Log.e("MASUK", "LOADDATA DIPANGGIL")

        if (isRefresh) isRefreshing = true
        loadError = null

        try {
            val rawData = mainRepository.getLeaderboard()

            android.util.Log.e("TEST123", "JUMLAH = ${rawData.size}")


            val mapped = rawData.mapIndexed { index, map ->

                android.util.Log.d("LEADERBOARD", "Row[$index] = $map")

                val name = when {
                    map["full_name"] != null -> map["full_name"].toString()
                    map["fullName"] != null -> map["fullName"].toString()
                    map["name"] != null -> map["name"].toString()
                    map["username"] != null -> map["username"].toString()
                    else -> "Pemain"
                }

                val score = when (val s = map["score"] ?: map["total_xp"]) {
                    is Number -> s.toInt()
                    is String -> s.toIntOrNull() ?: 0
                    else -> 0
                }

                val uid = map["user_id"]?.toString() ?: ""

                com.yarsi.javora.data.RankUser(
                    rank = index + 1,
                    name = name,
                    score = score.toString(),
                    subtitle = "Java Coder",
                    isCurrentUser = uid == currentUserId
                )
            }

            leaderboardUsers = mapped.sortedByDescending {
                it.score.toIntOrNull() ?: 0
            }.mapIndexed { i, user ->
                user.copy(rank = i + 1)
            }

            userViewModel.loadUserRank()

        } catch (e: Exception) {
            android.util.Log.e("LEADERBOARD", "ERROR = ${e.message}", e)
            loadError = e.message
        } finally {
            rankLoadDone = true
            isRefreshing = false
        }
    }
    // REAL-TIME: Dengerin perubahan di database Appwrite
    DisposableEffect(Unit) {
        val subscription = mainRepository.subscribeToLeaderboard {
            scope.launch {
                loadData(false)
            }
        }

        onDispose {
            subscription.close()
        }
    }

    // UPDATE OTOMATIS: Jika skor saya berubah di HP, langsung update urutan di layar
    LaunchedEffect(userTotalXp, userViewModel.userName.value) {
        if (rankLoadDone) {
            loadData(isRefresh = false)
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
        else -> {
            val finalRank = leaderboardUsers.find { it.isCurrentUser }?.rank ?: userRank
            RankScreen(
                users = leaderboardUsers,
                currentUserRank = if (finalRank > 0) finalRank else null,
                currentUserScore = userTotalXp.toString(),
                avatarUrl = userAvatar,
                isRefreshing = isRefreshing,
                onRefresh = { scope.launch { loadData(true) } },
                onTabSelected = { onNavigate(it.lowercase()) }
            )
        }
    }
}
