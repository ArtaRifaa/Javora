package com.yarsi.javora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarsi.javora.data.RankUser
import com.yarsi.javora.ui.components.JavoraBottomNavigation
import com.yarsi.javora.ui.components.JavoraStandardHeader
import com.yarsi.javora.ui.theme.*

@Composable
fun RankScreen(
    users: List<RankUser> = emptyList(),
    currentUserRank: Int? = null,
    currentUserScore: String? = null,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onTabSelected: (String) -> Unit = {}
) {
    val topUsers = users.take(3)
    val otherUsers = users.drop(3)
    val isUserInTop10 = users.any { it.isCurrentUser }

    Scaffold(
        containerColor = JavoraDarkBg,
        topBar = { JavoraStandardHeader() },
        bottomBar = { JavoraBottomNavigation(selectedTab = "Rank", onTabSelected = onTabSelected) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("KOMPETISI GLOBAL", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Peringkat\nTeratas", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = JavoraOrange,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = JavoraOrange)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                TopThreeSection(topUsers)
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Banner Peringkatmu (Jika tidak masuk Top 10)
            if (currentUserRank != null && !isUserInTop10) {
                item {
                    Text("PERINGKATMU", color = JavoraOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    YourRankCard(currentUserRank, currentUserScore ?: "0")
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            items(otherUsers) { user ->
                RankListItem(user)
                Spacer(modifier = Modifier.height(12.dp))
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun TopThreeSection(users: List<RankUser>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        // Juara 2
        if (users.size >= 2) PodiumItem(users[1], Modifier.weight(1f), JavoraPurple, 70.dp)
        else Spacer(Modifier.weight(1f))

        // Juara 1
        if (users.isNotEmpty()) PodiumItem(users[0], Modifier.weight(1.2f), JavoraOrange, 90.dp, true, JavoraOrange)
        else Spacer(Modifier.weight(1.2f))

        // Juara 3
        if (users.size >= 3) PodiumItem(users[2], Modifier.weight(1f), JavoraPurple, 70.dp)
        else Spacer(Modifier.weight(1f))
    }
}

@Composable
fun PodiumItem(user: RankUser, modifier: Modifier, ringColor: Color, avatarSize: androidx.compose.ui.unit.Dp, showTrophy: Boolean = false, scoreColor: Color = Color.White) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (showTrophy) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = JavoraOrange, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
        }
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(modifier = Modifier.size(avatarSize).clip(CircleShape).border(2.dp, Brush.linearGradient(listOf(ringColor, JavoraPink)), CircleShape).padding(4.dp)) {
                Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.DarkGray)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(avatarSize / 2))
                }
            }
            Surface(color = JavoraPink, shape = CircleShape, modifier = Modifier.size(20.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(user.rank.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(user.name.split(" ").first(), color = if (user.isCurrentUser) JavoraOrange else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(user.score, color = scoreColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun RankListItem(user: RankUser) {
    Surface(
        color = if (user.isCurrentUser) Color(0xFF2D1B10) else JavoraCardBg,
        shape = RoundedCornerShape(16.dp),
        border = if (user.isCurrentUser) androidx.compose.foundation.BorderStroke(1.dp, JavoraOrange) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(user.rank.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.DarkGray)) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, color = if (user.isCurrentUser) JavoraOrange else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(user.subtitle ?: "Java Coder", color = Color.Gray, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(user.score, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("XP", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun YourRankCard(rank: Int, score: String) {
    Surface(color = Color(0xFF2D1B10), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, JavoraOrange), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = JavoraOrange.copy(alpha = 0.15f), shape = CircleShape, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("#$rank", color = JavoraOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Peringkat Kamu", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Ayo kerjakan lebih banyak kuis!", color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(score, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("XP", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}
