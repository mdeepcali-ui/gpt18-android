package com.gptplus18.app.data.api

import com.gptplus18.app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ───
    @POST("api/auth/signup")
    suspend fun signup(@Body body: SignupRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun me(@Header("Authorization") bearer: String): Response<MeResponse>

    @GET("api/auth/subscription")
    suspend fun mySubscription(@Header("Authorization") bearer: String): Response<MySubscriptionResponse>

    @POST("api/auth/start-trial")
    suspend fun startTrial(@Header("Authorization") bearer: String): Response<Any>

    @POST("api/auth/verify-payment")
    suspend fun verifyPayment(
        @Header("Authorization") bearer: String,
        @Body body: VerifyPaymentRequest,
    ): Response<VerifyPaymentResponse>

    // ─── Chat ───
    @GET("api/chat/sessions")
    suspend fun listSessions(@Header("Authorization") bearer: String): Response<SessionsList>

    @POST("api/chat/sessions")
    suspend fun createSession(@Header("Authorization") bearer: String): Response<Session>

    @DELETE("api/chat/sessions/{sid}")
    suspend fun deleteSession(
        @Header("Authorization") bearer: String,
        @Path("sid") sid: Int,
    ): Response<Any>

    @GET("api/chat/sessions/{sid}/messages")
    suspend fun getMessages(
        @Header("Authorization") bearer: String,
        @Path("sid") sid: Int,
    ): Response<MessagesList>

    @POST("api/chat/send")
    suspend fun sendMessage(
        @Header("Authorization") bearer: String,
        @Body body: SendMessageRequest,
    ): Response<SendMessageResponse>

    // ─── Public ───
    @GET("api/subscription-public")
    suspend fun subscriptionPublic(): Response<SubscriptionPublic>
}
