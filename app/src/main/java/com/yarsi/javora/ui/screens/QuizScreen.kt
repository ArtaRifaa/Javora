package com.yarsi.javora.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarsi.javora.data.QuizQuestion
import com.yarsi.javora.ui.components.JavoraStandardHeader
import com.yarsi.javora.ui.theme.*

@Composable
fun QuizScreen(
    topicName: String = "",
    questions: List<QuizQuestion>,
    progressMap: Map<String, Float> = emptyMap(),
    onQuizComplete: (Int) -> Unit = {},
    onQuizFailed: () -> Unit = {},
    onExit: (Float) -> Unit = {},
    onContinueQuiz: (String) -> Unit = {},
    onResetProgress: (String) -> Unit = {}
) {
    val totalQuestions = questions.size
    val savedProgress = progressMap[topicName] ?: 0f
    
    val initialIndex = if (savedProgress > 0f && savedProgress < 1f) (savedProgress * totalQuestions).toInt() else 0
    val initialScore = initialIndex * 100

    var currentQuestionIndex by remember { mutableStateOf(initialIndex) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(initialScore) }
    var lives by remember { mutableStateOf(3) }
    var showExitDialog by remember { mutableStateOf(false) }
    var quizState by remember { mutableStateOf("running") }

    // Sync state saat ganti soal/topik
    LaunchedEffect(topicName, questions) {
        currentQuestionIndex = initialIndex
        score = initialScore
        selectedOption = null
        quizState = "running"
        lives = 3
    }

    val allAvailableTopics = listOf(
        "Pewarisan (Inheritance)",
        "Perulangan (Looping)",
        "Koleksi (Collections)",
        "Pemrograman Berorientasi Objek",
        "Polymorphism",
        "Exception Handling"
    )

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = JavoraCardBg,
            title = { Text("Keluar Kuis?", color = Color.White) },
            text = { Text("Progress kuis ini akan disimpan sebagai draft. Kamu yakin?", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = {
                    val progress = if (totalQuestions > 0) currentQuestionIndex.toFloat() / totalQuestions else 0f
                    onExit(progress)
                }) {
                    Text("Keluar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Lanjut", color = JavoraOrange)
                }
            }
        )
    }

    if (questions.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            JavoraStandardHeader()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Pilih Tantangan", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Lanjutkan progresmu atau mulai topik baru", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                val inProgress = progressMap.filter { it.value > 0f && it.value < 1f }
                if (inProgress.isNotEmpty()) {
                    item {
                        Text("SEDANG DIKERJAKAN (DRAFT)", color = JavoraOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    items(inProgress.toList()) { (topic, progress) ->
                        QuizDraftCard(topic, progress, onContinueQuiz, onResetProgress)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                item {
                    Text("SEMUA TOPIK", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(allAvailableTopics) { topic ->
                    val progress = progressMap[topic] ?: 0f
                    if (progress == 0f || progress >= 1f) {
                        QuizTopicCard(topic = topic, isCompleted = progress >= 1f, onClick = { onContinueQuiz(topic) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            Button(onClick = { onExit(0f) }, colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(50.dp)) {
                Text("KEMBALI KE BERANDA", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    if (quizState != "running") {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = if (quizState == "completed") Icons.Default.EmojiEvents else Icons.Default.SentimentVeryDissatisfied, contentDescription = null, tint = if (quizState == "completed") JavoraOrange else Color.Red, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = if (quizState == "completed") "Kuis Selesai!" else "Yah, Nyawa Habis!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = if (quizState == "completed") "Skor Akhir: $score" else "Jangan menyerah, coba lagi yuk!", color = Color.Gray, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(40.dp))
                Button(onClick = { if (quizState == "completed") onQuizComplete(score) else onQuizFailed() }, colors = ButtonDefaults.buttonColors(containerColor = JavoraOrange), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("KEMBALI KE MENU", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val currentQuestion = questions[currentQuestionIndex]
    val progress = (currentQuestionIndex + 1).toFloat() / totalQuestions

    Scaffold(
        containerColor = JavoraDarkBg,
        topBar = { 
            Column {
                Row(modifier = Modifier.fillMaxWidth().padding(end = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) { JavoraStandardHeader(showScore = true, score = score.toString()) }
                    IconButton(onClick = { showExitDialog = true }) { Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.Gray) }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    if (topicName.isNotEmpty()) {
                        Surface(color = JavoraOrange.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text(text = topicName.uppercase(), color = JavoraOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    } else { Spacer(modifier = Modifier.width(1.dp)) }
                    Row { repeat(3) { index -> Text(text = if (index < lives) "❤️" else "🖤", fontSize = 18.sp, modifier = Modifier.padding(start = 4.dp)) } }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("QUESTION ${currentQuestionIndex + 1} OF $totalQuestions", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${(progress * 100).toInt()}% COMPLETE", color = JavoraOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = JavoraPink, trackColor = Color.DarkGray)
            Spacer(modifier = Modifier.height(32.dp))
            Card(colors = CardDefaults.cardColors(containerColor = JavoraCardBg), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = currentQuestion.question, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    if (currentQuestion.codeSnippet != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        JavaCodeBlock(currentQuestion.codeSnippet)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            currentQuestion.options.forEachIndexed { index, option ->
                OptionItem(text = option, isSelected = selectedOption == index, onClick = { selectedOption = index })
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (selectedOption == currentQuestion.correctAnswerIndex) { score += 100 } else { lives-- }
                    if (lives <= 0) { quizState = "failed" } 
                    else if (currentQuestionIndex == totalQuestions - 1) { quizState = "completed" } 
                    else { currentQuestionIndex++; selectedOption = null }
                },
                enabled = selectedOption != null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedOption != null) JavoraOrange else Color.DarkGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = if (currentQuestionIndex == totalQuestions - 1) "Finish Quiz" else "Next Question", color = if (selectedOption != null) Color.Black else Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuizDraftCard(topic: String, progress: Float, onContinue: (String) -> Unit, onReset: (String) -> Unit) {
    Card(onClick = { onContinue(topic) }, colors = CardDefaults.cardColors(containerColor = JavoraCardBg), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(JavoraOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.History, null, tint = JavoraOrange) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(topic, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${(progress * 100).toInt()}% Selesai", color = JavoraOrange, fontSize = 12.sp)
            }
            IconButton(onClick = { onReset(topic) }) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f)) }
        }
    }
}

@Composable
fun QuizTopicCard(topic: String, isCompleted: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = JavoraCardBg.copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow, null, tint = if (isCompleted) Color.Green else Color.Gray) }
            Spacer(modifier = Modifier.width(16.dp))
            Text(topic, color = if (isCompleted) Color.Gray else Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.DarkGray)
        }
    }
}

@Composable
fun JavaCodeBlock(code: String) {
    Surface(color = Color(0xFF121212), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text = code, color = Color(0xFF98C379), fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.padding(16.dp), lineHeight = 18.sp)
    }
}

@Composable
fun OptionItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, color = JavoraCardBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, JavoraOrange) else null) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = text, color = if (isSelected) Color.White else Color.Gray, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    JavoraTheme { QuizScreen(questions = emptyList()) }
}
