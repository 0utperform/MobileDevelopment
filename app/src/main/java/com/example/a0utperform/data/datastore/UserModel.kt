package com.example.a0utperform.data.datastore



data class UserModel(
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val isLogin: Boolean,
)