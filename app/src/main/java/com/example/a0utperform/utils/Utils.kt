package com.example.a0utperform.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
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


@RequiresApi(Build.VERSION_CODES.O)

fun formatToSupabaseTimestamp(dueDate: ZonedDateTime): String {
    // Convert the ZonedDateTime to UTC with the correct timezone
    val utcDateTime = dueDate.withZoneSameInstant(ZoneOffset.UTC)

    // Extract microsecond precision
    val microseconds = utcDateTime.get(ChronoField.MICRO_OF_SECOND)

    // Format date and manually add microseconds and +00 offset
    val dateTimePart = utcDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    return "%s.%06d+00".format(dateTimePart, microseconds)
}

fun formatHoursToDigitalTime(hours: Double): String {
    val totalMinutes = (hours * 60).toInt()
    val hr = totalMinutes / 60
    val min = totalMinutes % 60
    return String.format("%d:%02d", hr, min)
}

fun formatToRupiah(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}