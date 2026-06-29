package com.bariatricnepal.app.data.api

import com.bariatricnepal.app.data.api.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Mirrors the REST routes registered in includes/class-bn-api.php
 * Base path: /wp-json/bariatric-nepal/v1/
 */
interface BNApiService {

    // ── Auth ─────────────────────────────────────────────────────────────
    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<GenericResponse>

    @POST("change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<GenericResponse>

    // ── Profile ──────────────────────────────────────────────────────────
    @GET("profile")
    suspend fun getProfile(): Response<Profile>

    @Multipart
    @POST("profile/picture")
    suspend fun uploadProfilePicture(@Part picture: MultipartBody.Part): Response<GenericResponse>

    // ── Weight ───────────────────────────────────────────────────────────
    @GET("weight")
    suspend fun getWeights(): Response<List<WeightLog>>

    // CHANGED v13: WeightRequest no longer includes height_cm
    @POST("weight")
    suspend fun logWeight(@Body body: WeightRequest): Response<WeightResponse>

    // ── Blood Reports ────────────────────────────────────────────────────
    @GET("blood-reports")
    suspend fun getBloodReports(): Response<List<BloodReport>>

    // CHANGED v13: accepts wbc, total_protein, albumin, lft in the field map
    @FormUrlEncoded
    @POST("blood-reports")
    suspend fun addBloodReport(@FieldMap fields: Map<String, String>): Response<GenericResponse>

    // ── Complaints ───────────────────────────────────────────────────────
    @GET("complaints")
    suspend fun getComplaints(): Response<List<Complaint>>

    @POST("complaints")
    suspend fun addComplaint(@Body body: ComplaintRequest): Response<GenericResponse>

    // ── Diet & Medications ───────────────────────────────────────────────
    @GET("diet-charts")
    suspend fun getDietCharts(): Response<List<DietChart>>

    @GET("medications")
    suspend fun getMedications(): Response<List<Medication>>

    // NEW v13: log medication taken/not-taken for today
    @POST("medication-log")
    suspend fun logMedicationTaken(@Body body: MedLogRequest): Response<GenericResponse>

    // ── Daily Intake (NEW v13) ────────────────────────────────────────────
    @GET("daily-intake")
    suspend fun getDailyIntake(): Response<DailyIntake>

    @POST("daily-intake")
    suspend fun updateDailyIntake(@Body body: IntakeUpdateRequest): Response<IntakeUpdateResponse>

    // ── Notifications ────────────────────────────────────────────────────
    @GET("notifications")
    suspend fun getNotifications(): Response<List<AppNotification>>

    @POST("notifications/read")
    suspend fun markAllNotificationsRead(): Response<GenericResponse>

    @POST("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<GenericResponse>
}
