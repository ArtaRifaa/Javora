package com.yarsi.javora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yarsi.javora.data.repository.AuthRepository
import com.yarsi.javora.ui.components.JavoraBottomNavigation
import com.yarsi.javora.ui.components.JavoraStandardHeader
import com.yarsi.javora.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userName: String = "Felix",
    avatarUrl: String? = null,
    totalXp: Int = 0,
    level: Int = 1,
    completedQuizzes: Int = 0,
    authRepository: AuthRepository? = null,
    onTabSelected: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = JavoraDarkBg,
        topBar = { JavoraStandardHeader(showLevel = true, level = level, xp = totalXp.toString()) },
        bottomBar = { JavoraBottomNavigation(selectedTab = "Profile", onTabSelected = onTabSelected) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Profile Header Section
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = JavoraGradient,
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                        ) {
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.Center).size(60.dp)
                                )
                            }
                        }
                    }
                    // Verified/Status Badge
                    Surface(
                        color = Color(0xFFCC8B3C),
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp).border(2.dp, JavoraDarkBg, CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(userName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        "ANTUSIAS JAVA",
                        color = Color(0xFFCC8B3C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // XP Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = JavoraCardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TOTAL XP", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            totalXp.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineLarge.copy(brush = JavoraGradient)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color(0xFFCC8B3C), modifier = Modifier.size(14.dp))
                            Text(" Total poin terkumpul", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfileStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.School, value = completedQuizzes.toString(), label = "Kuis Selesai")
                    ProfileStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.BarChart, value = "#-", label = "Peringkat Global")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Achievements Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pencapaian", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { }) {
                        Text("LIHAT SEMUA", color = Color(0xFFCC8B3C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AchievementItem(Icons.Default.MilitaryTech, "Pejuang Kode", Color(0xFFCC8B3C))
                    AchievementItem(Icons.Default.AccountTree, "Ahli Pewarisan", Color(0xFF9C27B0))
                    AchievementItem(Icons.Default.Whatshot, "7 Hari Beruntun", Color(0xFFE91E63))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Settings Section
                Text(
                    "Pengaturan Akun",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsItem(Icons.Default.Badge, "Ubah Profil")
                SettingsItem(Icons.Default.AutoStories, "Preferensi Belajar")
                SettingsItem(Icons.Default.Settings, "Pengaturan Aplikasi")
                SettingsItem(Icons.AutoMirrored.Filled.Logout, "Keluar", isLogout = true) {
                    scope.launch {
                        if (authRepository?.logout() == true) {
                            onLogout()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileStatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = JavoraCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = JavoraPurple, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun AchievementItem(icon: ImageVector, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = CircleShape,
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(30.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color.Gray, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsItem(icon: ImageVector, label: String, isLogout: Boolean = false, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        color = if (isLogout) Color.Transparent else JavoraCardBg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = if (isLogout) JavoraPink else Color(0xFFCC8B3C), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                label,
                color = if (isLogout) JavoraPink else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (!isLogout) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    JavoraTheme {
        ProfileScreen(avatarUrl = null, onTabSelected = {})
    }
}
