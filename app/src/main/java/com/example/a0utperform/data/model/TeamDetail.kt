package com.example.a0utperform.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDetail(
    val team_id: String,
    val name: String,
    @SerialName("created_at")
    val createdAt: String? = null,
)