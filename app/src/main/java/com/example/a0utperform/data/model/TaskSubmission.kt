package com.example.a0utperform.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskSubmission(
    val submission_id: String,
    val task_id: String,
    val user_id: String,
    val team_id:String,
    val submitted_at: String,
    val description: String
)