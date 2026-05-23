package com.yarsi.javora.ui.screens

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarsi.javora.data.QuizQuestion
import com.yarsi.javora.ui.components.JavoraBottomNavigation
import com.yarsi.javora.ui.components.JavoraStandardHeader
import com.yarsi.javora.ui.theme.*

@Composable
fun QuizScreen(
    questions: List<QuizQuestion>,
    onTabSelected: (String) -> Unit = {},
    onQuizComplete: (Int) -> Unit = {},
    onQuizFailed: () -> Unit = {}
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) } // Mulai dengan 3 nyawa

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = JavoraOrange)
        }
        return
    }

    val currentQuestion = questions[currentQuestionIndex]
    val totalQuestions = questions.size
    val progress = (currentQuestionIndex + 1).toFloat() / totalQuestions

    Scaffold(
        containerColor = JavoraDarkBg,
        topBar = { 
            Column {
                JavoraStandardHeader(showScore = true, score = score.toString())
                // Baris Nyawa
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    repeat(3) { index ->
                        Text(
                            text = if (index < lives) "❤️" else "🖤",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        bottomBar = { JavoraBottomNavigation(selectedTab = "Quiz", onTabSelected = onTabSelected) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUESTION ${currentQuestionIndex + 1} OF $totalQuestions",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(progress * 100).toInt()}% COMPLETE",
                    color = JavoraOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = JavoraPink,
                trackColor = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Question Card
            Card(
                colors = CardDefaults.cardColors(containerColor = JavoraCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = currentQuestion.question,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    if (currentQuestion.codeSnippet != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        JavaCodeBlock(currentQuestion.codeSnippet)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options
            currentQuestion.options.forEachIndexed { index, option ->
                OptionItem(
                    text = option,
                    isSelected = selectedOption == index,
                    onClick = { selectedOption = index }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            val isLastQuestion = currentQuestionIndex == totalQuestions - 1

            Button(
                onClick = {
                    if (selectedOption == currentQuestion.correctAnswerIndex) {
                        score += 100
                    } else {
                        lives-- // Kurangi nyawa jika salah
                    }
                    
                    if (lives <= 0) {
                        onQuizFailed() // Kuis gagal jika nyawa habis
                    } else if (isLastQuestion) {
                        onQuizComplete(score)
                    } else {
                        currentQuestionIndex++
                        selectedOption = null
                    }
                },
                enabled = selectedOption != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedOption != null) JavoraOrange else Color.DarkGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isLastQuestion) "Finish Quiz" else "Next Question",
                    color = if (selectedOption != null) Color.Black else Color.Gray,
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun JavaCodeBlock(code: String) {
    Surface(
        color = Color(0xFF121212),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = code,
            color = Color(0xFF98C379), // Greenish for code
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.padding(16.dp),
            lineHeight = 18.sp
        )
    }
}

@Composable
fun OptionItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = JavoraCardBg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, JavoraOrange) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Gray,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    JavoraTheme {
        QuizScreen(questions = emptyList(), onTabSelected = {})
    }
}
