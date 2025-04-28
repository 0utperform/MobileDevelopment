package com.example.a0utperform.data.model

data class SubmissionWithEvidence(
    val submission: TaskSubmission,
    val evidenceList: List<TaskEvidence>
)