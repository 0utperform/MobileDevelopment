package com.example.a0utperform.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskData(
    val task_id: String,
    val title: String,
    val team_id: String,
    val description: String,
    val status: String, // "pending", "done", etc.
    @SerialName("due_date")
    val dueDate: String?, // Parseable format
    @SerialName("created_at")
    val createdAt:String,
    val is_repeating:Boolean,
    val img_url:String? = "null"

)