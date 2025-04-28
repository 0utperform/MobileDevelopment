package com.example.a0utperform.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskEvidence(
    val evidence_id: String,
    val submission_id: String,
    val file_url: String
)