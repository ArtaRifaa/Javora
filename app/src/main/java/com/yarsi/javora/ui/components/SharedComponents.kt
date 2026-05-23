package com.yarsi.javora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import com.yarsi.javora.ui.theme.*

@Composable
fun JavoraBottomNavigation(selectedTab: String = "Geranda", onTabSelected: (String) -> Unit = {}) {
    NavigationBar(
        containerColor = JavoraDarkGray,
        tonalElevation = 8.dp
    ) {
        val tabs = listOf(
            Triple("Geranda", Icons.Default.Home, "Geranda"),
            Triple("Quiz", Icons.Default.Widgets, "Kuis"),
            Triple("Rank", Icons.Default.BarChart, "Peringkat"),
            Triple("Profile", Icons.Default.Person, "Profil")
        )

        tabs.forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = selectedTab == route,
                onClick = { onTabSelected(route) },
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = JavoraOrange,
                    selectedTextColor = JavoraOrange,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun JavoraStandardHeader(
    avatarUrl: String? = null,
    showScore: Boolean = false,
    score: String = "0",
    showLevel: Boolean = false,
    level: Int = 0,
    xp: String = "0"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            JavoraLogo(size = 32.dp, showText = false)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Javora", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showScore) {
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SKOR: ", color = Color.Gray, fontSize = 12.sp)
                        Text(score, color = JavoraOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (showLevel) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("LVL $level", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = JavoraOrange, modifier = Modifier.size(14.dp))
                        Text(" $xp XP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .border(1.dp, JavoraOrange, CircleShape)
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
