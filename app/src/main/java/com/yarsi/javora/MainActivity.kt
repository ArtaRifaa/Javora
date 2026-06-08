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
import com.yarsi.javora.data.QuestionRepository
import com.yarsi.javora.data.QuizQuestion
import com.yarsi.javora.data.repository.AuthRepository
import com.yarsi.javora.data.repository.CatatanRepository
import com.yarsi.javora.ui.components.JavoraSplashScreen
import com.yarsi.javora.ui.screens.*
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
//
@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val catatanRepository = remember { CatatanRepository(context) }
    val mainRepository = remember { com.yarsi.javora.data.repository.MainRepository(context) }
    
    // Inisialisasi ViewModel (Pola MVVM)
    val userViewModel = remember { UserViewModel(authRepository, catatanRepository, context) }
    
    var currentScreen by remember { mutableStateOf("splash") }
    var quizQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentTopic by remember { mutableStateOf("") }
    
    // Data sekarang diambil dari ViewModel (Mirip LiveData)
    val userName by userViewModel.userName
    val userTotalXp by userViewModel.userTotalXp
    val userLevel by userViewModel.userLevel
    val progressMap by userViewModel.progressMap

    var leaderboardUsers by remember { mutableStateOf<List<com.yarsi.javora.data.RankUser>>(emptyList()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (currentScreen == "splash") {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                // 1. Ambil data dari server
                userViewModel.loadUserData()
                
                // 2. Beri waktu sebentar agar State di ViewModel benar-benar stabil
                delay(1500) 
                
                // 3. Baru pindah layar
                currentScreen = "geranda"
            } else {
                delay(2000)
                currentScreen = "login"
            }
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
                    "login" -> LoginScreen(
                        authRepository = authRepository,
                        onLoginSuccess = { 
                            scope.launch {
                                // Ambil data dulu sebelum pindah layar
                                userViewModel.loadUserData()
                                delay(500)
                                currentScreen = "geranda" 
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
                                    text = "Menyiapkan tantangan kuis untukmu...",
                                    color = androidx.compose.ui.graphics.Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
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
                                try {
                                    delay(1500)
                                    val questions = QuestionRepository.getQuestionsForTopic(topic)
                                    
                                    if (questions.isNotEmpty()) {
                                        quizQuestions = questions
                                        currentScreen = "quiz"
                                    } else {
                                        currentScreen = "quiz" 
                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            android.widget.Toast.makeText(context, "Materi kuis belum tersedia.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("JavoraDebug", "Gagal memuat kuis", e)
                                    currentScreen = "quiz"
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
                    "rank" -> {
                        LaunchedEffect(Unit) {
                            scope.launch {
                                try {
                                    val rawData = mainRepository.getLeaderboard()
                                    leaderboardUsers = rawData.mapIndexed { index, map ->
                                        com.yarsi.javora.data.RankUser(
                                            rank = index + 1,
                                            name = map["full_name"] as? String ?: "Anonymous",
                                            score = ((map["score"] as? Number)?.toInt() ?: 0).toString(),
                                            subtitle = map["title"] as? String ?: "Coders"
                                        )
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("JavoraDebug", "Failed to fetch leaderboard", e)
                                }
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
                        authRepository = authRepository,
                        onTabSelected = { currentScreen = it.lowercase() },
                        onLogout = { 
                            userViewModel.logout()
                            currentScreen = "login" 
                        }
                    )
                    // Gabungkan geranda dan fallback ke satu handling
                    else -> {
                        val handleStartQuiz: (String) -> Unit = { topic ->
                            currentTopic = topic
                            currentScreen = "quiz_loading"
                            scope.launch {
                                try {
                                    // Menggunakan Repository Manual
                                    delay(1500)
                                    val questions = QuestionRepository.getQuestionsForTopic(topic)
                                    
                                    if (questions.isNotEmpty()) {
                                        quizQuestions = questions
                                        currentScreen = "quiz"
                                    } else {
                                        currentScreen = "geranda"
                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            android.widget.Toast.makeText(context, "Materi kuis belum tersedia.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("JavoraDebug", "Gagal memuat kuis", e)
                                    currentScreen = "geranda"
                                }
                            }
                        }
                        
                        HomeScreen(
                            userName = userName,
                            totalXp = userTotalXp,
                            level = userLevel,
                            progressMap = progressMap,
                            onTabSelected = { currentScreen = it.lowercase() },
                            onStartQuiz = handleStartQuiz
                        )
                    }
                }
            }
        }
    }
}
