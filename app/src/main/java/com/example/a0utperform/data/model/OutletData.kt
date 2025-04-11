package com.example.a0utperform.data.model

import kotlinx.serialization.Serializable


@Serializable
data class OutletData(
    val user_id:String,
    val outlet_id: String
)