package com.example.a0utperform.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StaffData(
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val age: String? = null,
    val email: String,
    val role: String? = null,
    val phone: String,
    val isLogin: Boolean? = false,
    val avatarUrl: String? = null,
    val payroll:Double? = 0.0,
    @SerialName("created_at")
    val createdAt: String
)