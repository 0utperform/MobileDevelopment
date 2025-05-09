package com.example.a0utperform.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Attendance(
    val attendance_id: String? = null,
    val user_id: String,
    val clock_in: String? = null,
    val clock_out: String? = null,
    val status: String,
    val created_at: String? = null
)