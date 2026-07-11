package com.sinop.sist.data.remote.dto.yahoo

import com.google.gson.annotations.SerializedName

data class YahooChartResponse(
    @SerializedName("chart")
    val chart: YahooChart
)

data class YahooChart(
    @SerializedName("result")
    val result: List<YahooChartResult>?,
    @SerializedName("error")
    val error: YahooChartError?
)

data class YahooChartResult(
    @SerializedName("meta")
    val meta: YahooChartMeta
)

data class YahooChartMeta(
    @SerializedName("symbol")
    val symbol: String?,
    @SerializedName("regularMarketPrice")
    val regularMarketPrice: Double?,
    @SerializedName("previousClose")
    val previousClose: Double?,
    @SerializedName("currency")
    val currency: String?
)

data class YahooChartError(
    @SerializedName("code")
    val code: String?,
    @SerializedName("description")
    val description: String?
)
