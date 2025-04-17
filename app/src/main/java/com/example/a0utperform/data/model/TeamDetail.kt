package com.example.a0utperform.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDetail(
    val team_id: String,
    val name: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    val outlet_id: String? = null,
    val staffSize:String? = null,
    val img_url:String? = null,
    val description:String?=null
)