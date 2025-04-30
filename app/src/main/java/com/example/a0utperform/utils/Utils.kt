package com.example.a0utperform.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun String.formatToReadableDate(): String {
    return try {
        val parsedDate = OffsetDateTime.parse(this)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())
        parsedDate.format(formatter)
    } catch (e: Exception) {
        this // fallback to raw if parsing fails
    }
}

