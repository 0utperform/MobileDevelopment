package com.example.a0utperform.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskAssignment(
    val task_id: String,
    val user_id: String
)