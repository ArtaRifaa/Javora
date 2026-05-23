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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarsi.javora.data.RankUser
import com.yarsi.javora.ui.components.JavoraBottomNavigation
import com.yarsi.javora.ui.components.JavoraStandardHeader
import com.yarsi.javora.ui.theme.*

@Composable
fun RankScreen(
    users: List<RankUser> = emptyList(),
    currentUserId: String? = null,
    onTabSelected: (String) -> Unit = {}
) {
    // Tiga besar untuk podium
    val topUsers = users.take(3)
    // Sisanya untuk daftar di bawah
    val otherUsers = users.drop(3)

    Scaffold(
        containerColor = JavoraDarkBg,
        topBar = { JavoraStandardHeader() },
        bottomBar = { JavoraBottomNavigation(selectedTab = "Rank", onTabSelected = onTabSelected) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("KOMPETISI GLOBAL", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Peringkat\nTeratas", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(" Diperbarui 1 mnt yang lalu", color = Color.Gray, fontSize = 10.sp)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TopThreeSection(topUsers)
                
                Spacer(modifier = Modifier.height(32.dp))
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // Rank 2
        if (users.size >= 2) {
            PodiumItem(users[1], modifier = Modifier.weight(1f), ringColor = JavoraPurple, avatarSize = 70.dp)
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Rank 1
        if (users.isNotEmpty()) {
            PodiumItem(
                users[0], 
                modifier = Modifier.weight(1.2f), 
                ringColor = JavoraOrange, 
                avatarSize = 90.dp, 
                showTrophy = true,
                scoreColor = JavoraOrange
            )
        }
        
        // Rank 3
        if (users.size >= 3) {
            PodiumItem(users[2], modifier = Modifier.weight(1f), ringColor = JavoraPurple, avatarSize = 70.dp)
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PodiumItem(
    user: RankUser, 
    modifier: Modifier, 
    ringColor: Color, 
    avatarSize: androidx.compose.ui.unit.Dp,
    showTrophy: Boolean = false,
    scoreColor: Color = JavoraPurple
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showTrophy) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = JavoraOrange, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(ringColor, JavoraPink)),
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
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(avatarSize / 2))
                }
            }
            Surface(
                color = JavoraPink,
                shape = CircleShape,
                modifier = Modifier.size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(user.rank.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(user.username ?: "", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(user.score, color = scoreColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RankListItem(user: RankUser) {
    val background = if (user.isCurrentUser) Color(0xFF2D1B10) else JavoraCardBg
    val border = if (user.isCurrentUser) androidx.compose.foundation.BorderStroke(1.dp, JavoraOrange) else null

    Surface(
        color = background,
        shape = RoundedCornerShape(16.dp),
        border = border,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                user.rank.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.name,
                    color = if (user.isCurrentUser) JavoraOrange else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(user.subtitle ?: "", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(user.score, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("poin", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RankScreenPreview() {
    JavoraTheme {
        RankScreen()
    }
}
