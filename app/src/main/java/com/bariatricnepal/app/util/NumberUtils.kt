package com.bariatricnepal.app.util

/** Safely parses a possibly-null/possibly-non-numeric String into a Double, or null. */
fun String?.toSafeDouble(): Double? = this?.trim()?.toDoubleOrNull()

/** Safely parses into an Int, or null. */
fun String?.toSafeInt(): Int? = this?.trim()?.toDoubleOrNull()?.toInt()

/** Treats DB string flags like "1"/"0"/"true"/"false" as a boolean. */
fun String?.toSafeBool(): Boolean = this == "1" || this.equals("true", ignoreCase = true)

/** Formats a numeric string for display, dropping a trailing ".0" and rounding to 1 decimal. */
fun String?.formatNumber(suffix: String = ""): String {
    val d = this.toSafeDouble() ?: return "—"
    val rounded = Math.round(d * 10) / 10.0
    val text = if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
    return if (suffix.isNotEmpty()) "$text $suffix" else text
}
