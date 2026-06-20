package com.yarsi.javora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarsi.javora.data.Topic
import com.yarsi.javora.ui.components.JavoraBottomNavigation
import com.yarsi.javora.ui.components.JavoraStandardHeader
import com.yarsi.javora.ui.theme.*

@Composable
fun HomeScreen(
    userName: String = "Pemain",
    avatarUrl: String? = null,
    totalXp: Int = 0,
    level: Int = 1,
    userRank: String = "-",
    progressMap: Map<String, Float> = emptyMap(),
    onTabSelected: (String) -> Unit = {},
    onStartQuiz: (String) -> Unit = {}
) {
    val topics = listOf(
        Topic("Pewarisan (Inheritance)", "Kuasai konsep superclass, subclass, dan penggunaan kembali kode.", 
            getStatus(progressMap["Pewarisan (Inheritance)"] ?: 0f), progressMap["Pewarisan (Inheritance)"] ?: 0f, Icons.Default.AccountTree, JavoraPink),
        Topic("Perulangan (Looping)", "Pahami while, do-while, dan for-loop untuk tugas berulang.", 
            getStatus(progressMap["Perulangan (Looping)"] ?: 0f), progressMap["Perulangan (Looping)"] ?: 0f, Icons.Default.Sync, JavoraPurple),
        Topic("Koleksi (Collections)", "Pengenalan List, Set, dan Map dalam Framework Java.", 
            getStatus(progressMap["Koleksi (Collections)"] ?: 0f), progressMap["Koleksi (Collections)"] ?: 0f, Icons.Default.Inventory, Color.Gray),
        Topic("Pemrograman Berorientasi Objek", "Dasar-dasar utama class, object, dan abstraksi.", 
            getStatus(progressMap["Pemrograman Berorientasi Objek"] ?: 0f), progressMap["Pemrograman Berorientasi Objek"] ?: 0f, Icons.Default.Dashboard, Color.Gray)
    )
    
    val completedQuizzes = progressMap.values.count { it > 0f }
    val accuracy = if (completedQuizzes > 0) (progressMap.values.sum() / completedQuizzes * 100).toInt() else 0

    Scaffold(
        bottomBar = { JavoraBottomNavigation(selectedTab = "Geranda", onTabSelected = onTabSelected) },
        containerColor = JavoraDarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            item {
                JavoraStandardHeader(
                    avatarUrl = avatarUrl,
                    showLevel = true,
                    level = level,
                    xp = totalXp.toString()
                )
                GreetingSection(userName)
                Spacer(modifier = Modifier.height(24.dp))
                StatsRow(accuracy, completedQuizzes, userRank)
                Spacer(modifier = Modifier.height(24.dp))
                ChallengeCard(onStartQuiz)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Topik Java",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(topics) { topic ->
                TopicCard(topic, onClick = { onStartQuiz(topic.title) })
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun GreetingSection(userName: String) {
    Column {
        Text(
            text = "Halo, $userName!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineLarge.copy(brush = JavoraGradient)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Siap menguasai Java hari ini? Rangkaian belajar Anda sudah mencapai 5 hari. Pertahankan momentumnya!",
            color = Color.LightGray,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun StatsRow(accuracy: Int, completedQuizzes: Int, userRank: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.FlashOn,
            value = "$accuracy%",
            label = "AKURASI"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.BarChart,
            value = userRank,
            label = "RANK"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Layers,
            value = completedQuizzes.toString(),
            label = "KUIS"
        )
    }
}

private fun getStatus(progress: Float): String {
    return when {
        progress >= 1f -> "SELESAI"
        progress > 0f -> "SEDANG BERJALAN"
        else -> "BELUM DIMULAI"
    }
}

@Composable
fun StatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = JavoraCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = JavoraPurple, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ChallengeCard(onStartQuiz: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B10)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tantangan Baru!", color = JavoraOrange, fontWeight = FontWeight.Bold)
                Text(
                    "Selesaikan 'Polymorphism' untuk mendapatkan bonus 500 XP",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = { onStartQuiz("Polymorphism") },
                colors = ButtonDefaults.buttonColors(containerColor = JavoraOrange),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("MULAI", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TopicCard(topic: Topic, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(JavoraCardBg, Color(0xFF2D1B3E))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(topic.icon, contentDescription = null, tint = JavoraOrange, modifier = Modifier.size(24.dp))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Status", color = Color.Gray, fontSize = 10.sp)
                        Text(topic.status, color = topic.statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(topic.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(topic.description, color = Color.Gray, fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Kemajuan", color = Color.White, fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text("${(topic.progress * 100).toInt()}%", color = Color.White, fontSize = 10.sp)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = { topic.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = if (topic.progress == 1f) Color.Green else JavoraPink,
                    trackColor = Color.DarkGray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    JavoraTheme {
        HomeScreen()
    }
}
