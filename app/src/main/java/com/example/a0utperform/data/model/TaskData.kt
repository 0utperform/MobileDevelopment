package com.example.a0utperform.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TaskData(
    val task_id: String? = "null",
    val title: String,
    val team_id: String,
    val description: String,
    val status: String, // "pending", "done", etc.
    @SerialName("due_date")
    val dueDate: String? = "null", // Parseable format
    @SerialName("created_at")
    val createdAt:String? = "null",
    val is_repeating:Boolean? = null,
    val img_url:String? = "null",
    val submission_per_day: Int? = 0,

    @Transient
    var completedSubmissions: Int = 0,

    @Transient
    var totalTargetSubmissions: Int = 0

)