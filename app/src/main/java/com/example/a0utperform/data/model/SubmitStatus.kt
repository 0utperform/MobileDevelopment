package com.example.a0utperform.data.model

sealed class SubmitStatus {
    object Loading : SubmitStatus()
    object Success : SubmitStatus()
    data class Error(val message: String) : SubmitStatus()
}