package com.bariatricnepal.app.util

/** Simple sealed result wrapper so UI code doesn't deal with exceptions directly. */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}
