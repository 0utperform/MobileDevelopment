package com.example.a0utperform.data.model

data class AttendanceStats(
    val completed: Int,
    val absent: Int,
    val total: Int,
    val percentage: Double
)