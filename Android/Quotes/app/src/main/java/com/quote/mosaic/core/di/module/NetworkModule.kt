package com.quote.mosaic.core.di.module

import android.content.Context
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.api.ApiService
import com.quote.mosaic.data.api.NetworkApiClient
import com.quote.mosaic.data.error.ApiErrorExtractor
import com.quote.mosaic.data.error.ResponseErrorMessageExtractor
import com.quote.mosaic.data.network.BroadcastReceiverBackedNetworkStatusProvider
import com.quote.mosaic.data.network.NetworkStatusProvider
import com.quote.mosaic.BuildConfig
import com.quote.mosaic.core.App
import com.quote.mosaic.core.CacheDir
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val CACHE_SIZE = 1024L * 1024L // 1M
private const val CACHE_DIR_NAME = "quote_http_cache"
private const val NETWORK_TIMEOUT_SECONDS = 30L

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun apiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun networkErrorExtractor(): ResponseErrorMessageExtractor = ApiErrorExtractor

    @Provides
    @Singleton
    fun networkStatusProvider(context: Context): NetworkStatusProvider =
        BroadcastReceiverBackedNetworkStatusProvider(context)

    @Provides
    @Singleton
    fun apiClient(
        apiService: ApiService,
        errorMessageExtractor: ResponseErrorMessageExtractor,
        networkStatusProvider: NetworkStatusProvider,
        userManager: UserManager
    ): ApiClient =
        NetworkApiClient(apiService, errorMessageExtractor, networkStatusProvider, userManager)

    @Provides
    fun provideRetrofitCallAdapterFactory(): CallAdapter.Factory =
        RxJava2CallAdapterFactory.create()

    @Provides
    fun provideRetrofitConverterFactory(objectMapper: ObjectMapper): Converter.Factory =
        JacksonConverterFactory.create(objectMapper)

    @Provides
    fun provideJacksonObjectMapper(): ObjectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)

    @Provides
    @CacheDir
    fun provideCacheFile(app: App) = File(app.cacheDir, CACHE_DIR_NAME)

    @Provides
    fun provideOkHttpCacheDir(@CacheDir cacheDir: File): Cache = Cache(cacheDir, CACHE_SIZE)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        okhttpCache: Cache,
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
//        .cache(okhttpCache)
        .addNetworkInterceptor(httpLoggingInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        converterFactory: Converter.Factory
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BuildConfig.API_URL)
        .addCallAdapterFactory(callAdapterFactory)
        .addConverterFactory(converterFactory)
        .build()

    @Provides
    fun httpLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingLevel = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }

        return HttpLoggingInterceptor()
            .setLevel(loggingLevel)
    }
}