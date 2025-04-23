package com.example.a0utperform.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskSubmission(
    @SerialName("submission_id")
    val submissionId: String, // UUID or string identifier

    @SerialName("task_id")
    val taskId: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("submitted_at")
    val submittedAt: String, // ISO 8601 datetime string (e.g., "2025-04-23T12:34:56Z")

    @SerialName("description")
    val note: String? = null // Optional note or description
)