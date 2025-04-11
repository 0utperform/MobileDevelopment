package com.example.a0utperform.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OutletDetail(
    val outlet_id :String,
    val name: String,
    val location:String,
    val created_at:String
)
