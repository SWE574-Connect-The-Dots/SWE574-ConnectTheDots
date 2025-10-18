package com.yybb.myapplication.presentation.ui.utils

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun formatDisplayDate(dateString: String): String {
    val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    // Try parsing as ISO 8601 ZonedDateTime
    try {
        val zonedDateTime = ZonedDateTime.parse(dateString)
        return zonedDateTime.format(outputFormatter)
    } catch (e: DateTimeParseException) {
        // Ignore and try next format
    }

    // Try parsing as YYYY-MM-DD LocalDate
    try {
        val localDate = LocalDate.parse(dateString)
        return localDate.format(outputFormatter)
    } catch (e: DateTimeParseException) {
        // Ignore and return original string if all parsing fails
    }

    return dateString // Return original string if no format matches
}
