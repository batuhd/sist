package com.sinop.sist.data.remote.api

import com.sinop.sist.data.remote.dto.fvt.FvtAppTokenResponse
import com.sinop.sist.data.remote.dto.fvt.FvtFundResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface FvtApiService {

    @GET("api/app-token")
    suspend fun getAppToken(
        @Header("x-device-id") deviceId: String
    ): Response<FvtAppTokenResponse>

    @GET("api/funds/{code}")
    suspend fun getFund(
        @Path("code") code: String
    ): Response<FvtFundResponse>
}
