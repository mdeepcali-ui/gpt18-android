package com.gptplus18.app.util

/**
 * نتيجة عملية — نجاح أو خطأ
 */
sealed class Result<out T> {

    data class Success<T>(val data: T) : Result<T>()

    data class Error(val message: String) : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}
