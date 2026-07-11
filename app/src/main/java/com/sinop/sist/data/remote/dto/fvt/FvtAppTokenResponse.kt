package com.sinop.sist.data.remote.dto.fvt

import com.google.gson.annotations.SerializedName

data class FvtAppTokenResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: FvtAppTokenData?,
    @SerializedName("timestamp")
    val timestamp: String?
)

data class FvtAppTokenData(
    @SerializedName("token")
    val token: String?,
    @SerializedName("expiresIn")
    val expiresIn: Long?,
    @SerializedName("origin")
    val origin: String?
)
