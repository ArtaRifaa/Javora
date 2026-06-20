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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    avatarUrl: String? = null,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onTabSelected: (String) -> Unit = {}
) {
    Scaffold(
        containerColor = JavoraDarkBg,
        topBar = { JavoraStandardHeader(avatarUrl = avatarUrl) },
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
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Peringkat\nTeratas", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = JavoraOrange,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = JavoraOrange)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Tampilkan peringkatmu di paling atas (Sticky-like)
            if (currentUserRank != null) {
                item {
                    Text("PERINGKAT KAMU", color = JavoraOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    RankListItem(
                        RankUser(
                            rank = currentUserRank,
                            name = "Kamu",
                            score = currentUserScore ?: "0",
                            isCurrentUser = true
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("DAFTAR PERINGKAT", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(users) { user ->
                RankListItem(user)
                Spacer(modifier = Modifier.height(12.dp))
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun RankListItem(user: RankUser) {
    val cardColor = if (user.isCurrentUser) Color(0xFF2D1B10) else JavoraCardBg
    val borderColor = if (user.isCurrentUser) JavoraOrange else Color.Transparent

    Surface(
        color = cardColor,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nomor Peringkat
            Surface(
                color = if (user.rank <= 3) JavoraOrange.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#${user.rank}",
                        color = if (user.rank <= 3) JavoraOrange else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Nama User
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    user.subtitle ?: "Java Coder",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            // Skor
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${user.score} XP",
                    color = JavoraOrange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
