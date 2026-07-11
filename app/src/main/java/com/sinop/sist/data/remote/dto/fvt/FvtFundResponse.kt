package com.sinop.sist.data.remote.dto.fvt

import com.google.gson.annotations.SerializedName

data class FvtFundResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: FvtFundData?,
    @SerializedName("timestamp")
    val timestamp: String?
)

data class FvtFundData(
    @SerializedName("fund")
    val fund: FvtFund?,
    @SerializedName("returns")
    val returns: FvtReturns?,
    @SerializedName("priceHistory")
    val priceHistory: List<FvtPriceHistoryItem>?
)

data class FvtFund(
    @SerializedName("fonKodu")
    val fonKodu: String?,
    @SerializedName("fonAdi")
    val fonAdi: String?,
    @SerializedName("fiyat")
    val fiyat: String?,
    @SerializedName("sonGuncelleme")
    val sonGuncelleme: String?,
    @SerializedName("paraBirimi")
    val paraBirimi: String?,
    @SerializedName("kategori")
    val kategori: String?,
    @SerializedName("getiri")
    val getiri: String?
)

data class FvtReturns(
    @SerializedName("kod")
    val kod: String?,
    @SerializedName("gunlukGetiri")
    val gunlukGetiri: String?,
    @SerializedName("haftalikGetiri")
    val haftalikGetiri: String?,
    @SerializedName("aylikGetiri")
    val aylikGetiri: String?,
    @SerializedName("ytdGetiri")
    val ytdGetiri: String?,
    @SerializedName("birYillikGetiri")
    val birYillikGetiri: String?
)

data class FvtPriceHistoryItem(
    @SerializedName("kod")
    val kod: String?,
    @SerializedName("fiyat")
    val fiyat: String?,
    @SerializedName("tarih")
    val tarih: String?,
    @SerializedName("getiri")
    val getiri: String?
)
