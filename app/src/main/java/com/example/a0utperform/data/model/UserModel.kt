package com.example.a0utperform.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UserModel(
    @SerialName("user_id")
    val userId: String,
    val email: String,
    val name: String,
    val role: String? = null,
    val phone: String,
    val isLogin: Boolean? = false,
    @SerialName("created_at")
    val createdAt: String
)