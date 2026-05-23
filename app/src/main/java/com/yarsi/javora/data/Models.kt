package com.yarsi.javora.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Topic(
    val title: String,
    val description: String,
    val status: String,
    val progress: Float,
    val icon: ImageVector,
    val statusColor: Color
)

data class RankUser(
    val rank: Int,
    val name: String,
    val username: String? = null,
    val subtitle: String? = null,
    val score: String,
    val isCurrentUser: Boolean = false
)

data class QuizQuestion(
    val question: String,
    val codeSnippet: String? = null,
    val options: List<String>,
    val correctAnswerIndex: Int
)
