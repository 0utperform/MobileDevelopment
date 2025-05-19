package com.example.a0utperform.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LeaveRequest(
    @SerialName("request_id") val requestId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    val reason: String,
    val status: String,
    val created_at: String,
    val type: String
) : Parcelable