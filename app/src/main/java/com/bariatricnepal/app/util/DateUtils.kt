package com.bariatricnepal.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    // MySQL DATETIME format, e.g. "2026-06-21 14:05:00"
    private val SQL_FORMATS = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    private fun parse(raw: String?): Date? {
        if (raw.isNullOrBlank()) return null
        for (pattern in SQL_FORMATS) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                return sdf.parse(raw)
            } catch (e: Exception) {
                // try next pattern
            }
        }
        return null
    }

    /** "21 Jun 2026" */
    fun prettyDate(raw: String?): String {
        val d = parse(raw) ?: return raw ?: "—"
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(d)
    }

    /** "Sunday, 21 Jun 2026" */
    fun prettyDateLong(raw: String?): String {
        val d = parse(raw) ?: return raw ?: "Date not recorded"
        return SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(d)
    }

    /** "21 Jun 2026, 2:05 PM" */
    fun prettyDateTime(raw: String?): String {
        val d = parse(raw) ?: return raw ?: "—"
        return SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(d)
    }

    /** Grouping key: "2026-06-21" (just the calendar date, no time) */
    fun dateKey(raw: String?): String {
        val d = parse(raw) ?: return "undated"
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d)
    }

    fun isToday(raw: String?): Boolean {
        val d = parse(raw) ?: return false
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val key = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d)
        return today == key
    }

    /** "3 hours ago", "2 days ago", etc. — simple relative time for notifications. */
    fun timeAgo(raw: String?): String {
        val d = parse(raw) ?: return ""
        val diffMs = Date().time - d.time
        if (diffMs < 0) return "just now"
        val minutes = diffMs / 60000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            days < 30 -> "$days day${if (days == 1L) "" else "s"} ago"
            else -> prettyDate(raw)
        }
    }
}

/**
 * Groups a list of items by the calendar date portion of [dateOf], newest
 * date first — used for the date-wise diet/medication accordion screens,
 * mirroring BN_Patient::group_by_date() on the WordPress side.
 */
fun <T> List<T>.groupByDateDesc(dateOf: (T) -> String?): List<Pair<String, List<T>>> {
    val groups = LinkedHashMap<String, MutableList<T>>()
    for (item in this) {
        val key = DateUtils.dateKey(dateOf(item))
        groups.getOrPut(key) { mutableListOf() }.add(item)
    }
    return groups.entries
        .sortedByDescending { it.key }
        .map { it.key to it.value }
}
