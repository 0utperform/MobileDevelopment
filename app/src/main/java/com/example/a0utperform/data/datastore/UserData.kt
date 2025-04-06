package com.example.a0utperform.data.datastore

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val user_id: String,
    val name: String,
    val email: String,
    val phone: String,
    val created_at : String
)