package com.example.a0utperform.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OutletWithManager(
    val outlet: OutletDetail,
    val managerName: String
)
