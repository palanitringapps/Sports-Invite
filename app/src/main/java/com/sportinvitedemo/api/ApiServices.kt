package com.sportinvitedemo.api

import com.sportinvitedemo.data.InviteLinkResponse
import com.sportinvitedemo.data.MemberInviteModel
import com.sportinvitedemo.data.RoleBasedInviteRequest
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit


interface ApiServices {

    companion object {
        private const val BASE_API = "https://demo.com" //need to update Base URL
        private const val timeoutTime = 5 // Need to update to 30 second once valida base URL added

        private val defaultHttpClient = OkHttpClient.Builder()
            .connectTimeout(timeoutTime.toLong(), TimeUnit.SECONDS)
            .writeTimeout(timeoutTime.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeoutTime.toLong(), TimeUnit.SECONDS).build()


        private val sessionRetrofit = Retrofit.Builder()
            .baseUrl(BASE_API)
            .addConverterFactory(GsonConverterFactory.create())
            .client(defaultHttpClient)
            .build()

        val service: ApiServices = sessionRetrofit.create(ApiServices::class.java)
    }

    @GET("/teams/{teamId}")
    suspend fun getInviteIdInfo(@Path("teamId") request: String): MemberInviteModel

    @POST(" /teams/{teamId}/invites")
    suspend fun getInviteLink(
        @Path("teamId") id: String,
        @Body request: RoleBasedInviteRequest
    ): InviteLinkResponse
}
