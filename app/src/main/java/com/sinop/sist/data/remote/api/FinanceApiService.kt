package com.sinop.sist.data.remote.api

import com.sinop.sist.data.remote.dto.PriceResponseDto
import com.sinop.sist.data.remote.dto.yahoo.YahooChartResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FinanceApiService {

    @GET("quote/{symbol}")
    suspend fun getQuote(@Path("symbol") symbol: String): Response<PriceResponseDto>

    @GET("quote")
    suspend fun getQuotes(@Query("symbols") symbols: String): Response<List<PriceResponseDto>>

    @GET("search")
    suspend fun searchSymbol(@Query("q") query: String): Response<List<PriceResponseDto>>

    @GET("v8/finance/chart/{symbol}")
    suspend fun getYahooChart(@Path("symbol") symbol: String): Response<YahooChartResponse>
}
