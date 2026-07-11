package com.sinop.sist.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PriceResponseDto(
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("price")
    val price: Double?,
    @SerializedName("currency")
    val currency: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("change")
    val change: Double?,
    @SerializedName("changePercent")
    val changePercent: Double?,
    @SerializedName("lastUpdated")
    val lastUpdated: String?,
    @SerializedName("source")
    val source: String?
)
