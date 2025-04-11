package com.example.a0utperform.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TeamData(
    val user_id :String,
    val team_id: String
)
