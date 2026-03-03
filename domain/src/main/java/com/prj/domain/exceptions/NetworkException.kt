package com.prj.domain.exceptions

/**
 * Class to handle Network Exception when calling API
 */
sealed class NetworkException(message: String? = null) : Exception(message) {
    /**
     * No internet
     */
    data object NoInternetException : NetworkException("No internet connection") {
        private fun readResolve(): Any = NoInternetException
    }

    /**
     * Request timeout
     */
    data object TimeoutException : NetworkException("Request timeout") {
        private fun readResolve(): Any = TimeoutException
    }

    /**
     * Failed to parse server response
     */
    data class JsonParsingException(
        val code: Throwable,
    ) : NetworkException("Parsing error: $code")

    /**
     * Server returned an error response
     * @param code HTTP status code
     * @param errorBody Error response body from server
     */
    data class ServerException(
        val code: Int,
        val errorBody: String?
    ) : NetworkException("Server error: $code - $errorBody")

    /**
     * Unauthorized - 401
     */
    data object UnauthorizedException : NetworkException("Unauthorized access") {
        private fun readResolve(): Any = UnauthorizedException
    }

    /**
     * Forbidden - 403
     */
    data object ForbiddenException : NetworkException("Access forbidden") {
        private fun readResolve(): Any = ForbiddenException
    }

    /**
     * Not Found - 404
     */
    data object NotFoundException : NetworkException("Resource not found") {
        private fun readResolve(): Any = NotFoundException
    }

    /**
     * Unknown network error
     */
    data class UnknownException(
        val originalException: Exception
    ) : NetworkException("Unknown error: ${originalException.message}")
}