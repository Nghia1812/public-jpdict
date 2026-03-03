package com.prj.data.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.prj.data.BuildConfig
import com.prj.data.remote.api.TranslateApiService
import com.prj.data.remote.deserializer.DtoDeserializer
import com.prj.data.remote.api.ExamApiService
import com.prj.data.remote.dto.BaseSectionDto
import com.prj.data.remote.network.NetworkMonitor
import com.prj.domain.repository.INetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ExamGson

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TranslateGson

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL_TRANSLATE = BuildConfig.BASE_URL_TRANSLATE
    private const val BASE_URL_JLPT_EXAM = BuildConfig.BASE_URL_JLPT_EXAM

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        return Cache(File(context.cacheDir, "api_cache"), 10L * 1024 * 1024) // 10MB cache
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message ->
            Timber.d("HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
//            .addInterceptor(
//                FakeNetworkInterceptor(
//                    if (BuildConfig.DEBUG)
//                        NetworkMode.JSON_ERROR
//                    else
//                        NetworkMode.REAL
//                )
//            )
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=300") // 5 min
                    .build()
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @ExamGson
    fun provideExamGson(): Gson =
        GsonBuilder()
            .registerTypeAdapter(BaseSectionDto::class.java, DtoDeserializer())
            .create()

    @Provides
    @Singleton
    @TranslateGson
    fun provideTranslateGson(): Gson =
        GsonBuilder().create()

    // Provide Retrofit for Translate API
    @Provides
    @Singleton
    @Named("TranslateRetrofit")
    fun provideTranslateRetrofit(okHttpClient: OkHttpClient, @TranslateGson gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_TRANSLATE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    // Provide Retrofit for JLPT Exam API
    @Provides
    @Singleton
    @Named("ExamRetrofit")
    fun provideExamRetrofit(okHttpClient: OkHttpClient, @ExamGson gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_JLPT_EXAM)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()


    // Provide services
    @Provides
    @Singleton
    fun provideTranslateService(@Named("TranslateRetrofit") retrofit: Retrofit): TranslateApiService =
        retrofit.create(TranslateApiService::class.java)

    @Provides
    @Singleton
    fun provideExamService(@Named("ExamRetrofit") retrofit: Retrofit): ExamApiService =
        retrofit.create(ExamApiService::class.java)

}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkMonitorModule {
    @Binds
    abstract fun bindNetworkMonitor(
        impl: NetworkMonitor
    ): INetworkMonitor
}

/**
 * Error Testing purpose only
 */

enum class NetworkMode {
    REAL,
    NO_INTERNET,
    TIMEOUT,
    SERVER_500,
    JSON_ERROR
}

/**
 * Fake network error interceptor
 */
class FakeNetworkInterceptor(
    private val mode: NetworkMode
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        if (!request.url.host.contains("translation.googleapis.com")) {
            return chain.proceed(request)
        }

        when (mode) {

            NetworkMode.NO_INTERNET ->
                throw IOException("No internet connection")

            NetworkMode.TIMEOUT ->
                throw SocketTimeoutException("Request timeout")

            NetworkMode.SERVER_500 ->
                return buildResponse(
                    request = request,
                    code = 500,
                    body = """
                        {
                          "error": {
                            "code": 500,
                            "message": "Internal server error"
                          }
                        }
                    """
                )

            NetworkMode.JSON_ERROR ->
                return buildResponse(
                    request = request,
                    code = 200,
                    body = """
                        {
                          "data": {
                            "translations": [
                              { "model": "nmt" }
                            ]
                          }
                        }
                    """
                )

            NetworkMode.REAL -> Unit
        }

        return chain.proceed(request)
    }

    private fun buildResponse(
        request: Request,
        code: Int,
        body: String
    ): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("Fake response")
            .body(
                body.toResponseBody(
                    "application/json".toMediaType()
                )
            )
            .build()
}