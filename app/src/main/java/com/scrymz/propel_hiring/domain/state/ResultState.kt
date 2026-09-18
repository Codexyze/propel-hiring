package com.scrymz.propel_hiring.domain.state

/**
 * Generic sealed class representing operation results in the domain layer.
 */
sealed class ResultState<out T> {
    object Loading : ResultState<Nothing>()
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val message: String) : ResultState<Nothing>()
}
