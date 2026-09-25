package com.gptplus18.app.data.api

import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE
import com.gptplus18.app.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    // ─── Admin ───
    @GET("api/auth/admin/status")
    suspend fun adminStatus(@Header("Authorization") bearer: String): Response<AdminStatus>

    @GET("api/auth/admin/users")
    suspend fun adminUsers(@Header("Authorization") bearer: String): Response<AdminUsersList>

    @POST("api/auth/admin/grant")
    suspend fun adminGrant(
        @Header("Authorization") bearer: String,
        @Body body: AdminGrantRequest,
    ): Response<AdminActionResult>

    @POST("api/auth/admin/revoke")
    suspend fun adminRevoke(
        @Header("Authorization") bearer: String,
        @Body body: AdminRevokeRequest,
    ): Response<AdminActionResult>

    @POST("api/auth/admin/reset-trial")
    suspend fun adminResetTrial(
        @Header("Authorization") bearer: String,
        @Body body: AdminResetTrialRequest,
    ): Response<AdminActionResult>

    // ─── Chat ───
    @GET("api/chat/sessions")
    suspend fun listSessions(@Header("Authorization") bearer: String): Response<SessionsList>

    // 📋 All Sessions (Chat + Code)
    @GET("api/chat/sessions/all")
    suspend fun listAllSessions(@Header("Authorization") bearer: String): Response<AllSessionsList>

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

    // ─── Chat Upload ───
    @Multipart
    @POST("api/chat/upload-temp")
    suspend fun uploadTemp(
        @Header("Authorization") bearer: String,
        @Part file: MultipartBody.Part,
    ): Response<UploadTempResponse>

    @POST("api/chat/process-uploaded")
    suspend fun processUploaded(
        @Header("Authorization") bearer: String,
        @Body body: ProcessUploadRequest,
    ): Response<ProcessUploadResponse>

    // ─── Code Agent ───
    @POST("api/code/generate")
    suspend fun codeGenerate(
        @Header("Authorization") bearer: String,
        @Body body: CodeRequest,
    ): Response<CodeJobStart>

    @GET("api/code/status/{jobId}")
    suspend fun codeStatus(
        @Header("Authorization") bearer: String,
        @Path("jobId") jobId: String,
    ): Response<CodeJobStatus>

    @GET("api/code/sessions")
    suspend fun codeSessions(@Header("Authorization") bearer: String): Response<CodeSessionsList>

    @GET("api/code/sessions/{sid}/messages")
    suspend fun codeMessages(
        @Header("Authorization") bearer: String,
        @Path("sid") sid: Int,
    ): Response<CodeMessagesList>

    @DELETE("api/code/sessions/{sid}")
    suspend fun codeDeleteSession(
        @Header("Authorization") bearer: String,
        @Path("sid") sid: Int,
    ): Response<Any>

    // ─── Media ───
    @POST("api/chat/song")
    suspend fun generateSong(
        @Header("Authorization") bearer: String,
        @Body body: SongRequest,
    ): Response<SongResponse>

    @POST("api/chat/video")
    suspend fun generateVideo(
        @Header("Authorization") bearer: String,
        @Body body: VideoRequest,
    ): Response<VideoResponse>

    // ─── Public ───
    @GET("api/subscription-public")
    suspend fun subscriptionPublic(): Response<SubscriptionPublic>


    // ─── Notifications (FCM) ───
    @POST("api/notifications/register-token")
    suspend fun registerFcmToken(
        @Header("Authorization") bearer: String,
        @Body body: RegisterTokenRequest,
    ): Response<Any>


    // ─── Admin Notifications (FCM) ───
    @POST("api/admin/notifications/send")
    suspend fun adminSendNotification(
        @Header("Authorization") bearer: String,
        @Body body: AdminSendNotificationRequest,
    ): Response<AdminNotificationResult>

    @POST("api/admin/notifications/send-many")
    suspend fun adminSendMany(
        @Header("Authorization") bearer: String,
        @Body body: AdminSendManyRequest,
    ): Response<AdminNotificationResult>

    @POST("api/admin/notifications/broadcast")
    suspend fun adminBroadcast(
        @Header("Authorization") bearer: String,
        @Body body: AdminBroadcastRequest,
    ): Response<AdminNotificationResult>

    @GET("api/admin/notifications/stats")
    suspend fun adminNotifStats(
        @Header("Authorization") bearer: String,
    ): Response<AdminNotificationStats>

    @GET("api/admin/notifications/tokens")
    suspend fun adminNotifTokens(
        @Header("Authorization") bearer: String,
    ): Response<Map<String, Any>>

    
    // ⭐ M4-b: سجل الوسائط
    @GET("api/chat/history")
    suspend fun getMediaHistory(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int = 50,
    ): retrofit2.Response<com.gptplus18.app.data.models.MediaHistoryResponse>
    
    @DELETE("api/chat/history/{id}")
    suspend fun deleteMediaHistoryItem(
        @Header("Authorization") auth: String,
        @Path("id") id: Int,
    ): retrofit2.Response<Map<String, Any>>
    
    @DELETE("api/chat/history")
    suspend fun clearMediaHistory(
        @Header("Authorization") auth: String,
    ): retrofit2.Response<Map<String, Any>>

}
