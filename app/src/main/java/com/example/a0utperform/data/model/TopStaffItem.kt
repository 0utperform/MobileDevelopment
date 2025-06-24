package com.example.a0utperform.data.model

data class TopStaffItem(
    val userId: String,
    val name: String,
    val role: String? = null,
    val profileUrl: String? = null,
    val completionRate: Double
)