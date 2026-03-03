package com.prj.data.repository

import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.prj.data.BuildConfig
import com.prj.domain.exceptions.NetworkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException

abstract class BaseApiRepository {

    suspend fun <T> safeApiCall(api: suspend () -> Response<T>): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api()
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    Timber.d("API call with code: $body")
                    Result.success(body)
                } else {
                    Timber.e("API call failed with code: $response")
                    Result.failure(handleErrorResponse(response))
                }
            } catch (e: IOException) {
                // Network connectivity issues (no internet, DNS failure, etc.)
                Timber.e(e, "Network connectivity error")
                Result.failure(NetworkException.NoInternetException)
            } catch (e: SocketTimeoutException){
                // Request timed out
                Timber.e(e, "Request timed out")
                Result.failure(NetworkException.TimeoutException)
            } catch (e: JsonSyntaxException) {
                Result.failure(NetworkException.JsonParsingException(e))
            }
            catch (e: Exception) {
                // Any other error
                Timber.e(e, "Unexpected error in API call")
                Result.failure(NetworkException.UnknownException(e))
            }
        }
    }

    private fun <T> handleErrorResponse(response: Response<T>): NetworkException {
        val errorBody = response.errorBody()?.string()

        return when (response.code()) {
            401 -> NetworkException.UnauthorizedException
            403 -> NetworkException.ForbiddenException
            404 -> NetworkException.NotFoundException
            else -> NetworkException.ServerException(response.code(), errorBody)
        }
    }
}