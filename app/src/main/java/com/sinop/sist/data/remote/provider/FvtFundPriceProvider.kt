package com.sinop.sist.data.remote.provider

import android.content.Context
import android.content.SharedPreferences
import com.sinop.sist.data.remote.api.FvtApiService
import com.sinop.sist.data.remote.dto.fvt.FvtFund
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

class FvtFundPriceProvider(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val deviceId: String
        get() {
            var id = prefs.getString(KEY_DEVICE_ID, null)
            if (id == null) {
                id = UUID.randomUUID().toString().replace("-", "")
                prefs.edit().putString(KEY_DEVICE_ID, id).apply()
            }
            return id
        }

    private var cachedToken: String? = prefs.getString(KEY_TOKEN, null)
    private var tokenFetchedAt: Long = prefs.getLong(KEY_TOKEN_TIME, 0L)
    private val tokenMutex = Mutex()

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request()
        val path = request.url.encodedPath
        val code = if (path.startsWith("/api/funds/")) {
            path.removePrefix("/api/funds/").substringBefore("/")
        } else null
        val referer = if (!code.isNullOrBlank()) {
            "https://fvt.com.tr/fonlar/yatirim-fonlari/$code"
        } else {
            "https://fvt.com.tr/"
        }

        val requestBuilder = request.newBuilder()
            .header("User-Agent", USER_AGENT)
            .header("x-device-id", deviceId)
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", "tr-TR,tr;q=0.9")
            .header("Referer", referer)
            .header("Origin", "https://fvt.com.tr")

        cachedToken?.let { token ->
            requestBuilder.header("Cookie", "fvt_at=$token")
        }

        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val api: FvtApiService by lazy {
        retrofit.create(FvtApiService::class.java)
    }

    suspend fun fetchFundPrice(code: String): Result<FvtFund> {
        val upperCode = code.trim().uppercase().removeSuffix(".IS")

        ensureToken()

        return try {
            val response = api.getFund(upperCode)
            if (response.isSuccessful) {
                val fund = response.body()?.data?.fund
                if (fund != null && !fund.fiyat.isNullOrBlank()) {
                    Result.success(fund)
                } else {
                    Result.failure(Exception("FVT'den fiyat bulunamadı: $upperCode"))
                }
            } else {
                if (response.code() == 401) {
                    clearToken()
                }
                Result.failure(Exception("FVT HTTP ${response.code()}: $upperCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureToken() {
        val now = System.currentTimeMillis()
        val tokenValid = cachedToken != null && (now - tokenFetchedAt) < TOKEN_TTL_MS
        if (tokenValid) return

        tokenMutex.withLock {
            val now2 = System.currentTimeMillis()
            if (cachedToken != null && (now2 - tokenFetchedAt) < TOKEN_TTL_MS) return@withLock

            try {
                val response = api.getAppToken(deviceId)
                if (response.isSuccessful) {
                    val token = response.body()?.data?.token
                    if (!token.isNullOrBlank()) {
                        cachedToken = token
                        tokenFetchedAt = now2
                        prefs.edit()
                            .putString(KEY_TOKEN, token)
                            .putLong(KEY_TOKEN_TIME, now2)
                            .apply()
                    } else {
                        throw Exception("FVT token boş döndü")
                    }
                } else {
                    throw Exception("FVT token alınamadı: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                clearToken()
                throw e
            }
        }
    }

    private fun clearToken() {
        cachedToken = null
        tokenFetchedAt = 0L
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_TOKEN_TIME)
            .apply()
    }

    companion object {
        private const val BASE_URL = "https://fvt.com.tr/"
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36"
        private const val PREFS_NAME = "fvt_provider_prefs"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_TOKEN = "fvt_token"
        private const val KEY_TOKEN_TIME = "fvt_token_time"
        private const val TOKEN_TTL_MS = 60 * 60 * 1000L // 1 saat (token 2 saat geçerli ama güvenli marj)
    }
}
