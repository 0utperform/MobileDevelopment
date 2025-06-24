package com.example.a0utperform.data.model

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate,
    var status: String? = null // null, "completed", "absent"
)