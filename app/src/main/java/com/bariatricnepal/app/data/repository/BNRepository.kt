package com.bariatricnepal.app.data.repository

import com.bariatricnepal.app.data.api.BNApiService
import com.bariatricnepal.app.data.api.models.*
import com.bariatricnepal.app.util.ApiResult
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.IOException

/** Shape of a WordPress REST API error body: {"code":"...","message":"...","data":{"status":401}} */
private data class WpErrorBody(val code: String?, val message: String?)

class BNRepository(private val api: BNApiService) {

    private suspend fun <T> call(block: suspend () -> Response<T>): ApiResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = block()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ApiResult.Success(body)
                    } else {
                        ApiResult.Error("Empty response from server.")
                    }
                } else {
                    val raw = response.errorBody()?.string()
                    val message = try {
                        Gson().fromJson(raw, WpErrorBody::class.java)?.message
                    } catch (e: Exception) {
                        null
                    }
                    ApiResult.Error(message ?: "Server error (${response.code()}). Please try again.")
                }
            } catch (e: IOException) {
                ApiResult.Error("Can't reach the server. Check your internet connection.")
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Something went wrong. Please try again.")
            }
        }
    }

    // ── Auth ─────────────────────────────────────────────────────────────
    suspend fun login(identifier: String, password: String) =
        call { api.login(LoginRequest(identifier, password)) }

    suspend fun logout() = call { api.logout() }

    suspend fun changePassword(current: String, new: String) =
        call { api.changePassword(ChangePasswordRequest(current, new)) }

    // ── Profile ──────────────────────────────────────────────────────────
    suspend fun getProfile() = call { api.getProfile() }

    suspend fun uploadProfilePicture(file: File): ApiResult<GenericResponse> {
        val mimeType = when (file.extension.lowercase()) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            else -> "image/jpeg"
        }
        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("profile_picture", file.name, requestFile)
        return call { api.uploadProfilePicture(part) }
    }

    // ── Weight ───────────────────────────────────────────────────────────
    suspend fun getWeights() = call { api.getWeights() }

    // CHANGED v13: removed heightCm — plugin uses patient's stored initial_height
    suspend fun logWeight(weightKg: Double, notes: String?) =
        call { api.logWeight(WeightRequest(weightKg, notes)) }

    // ── Blood Reports ────────────────────────────────────────────────────
    suspend fun getBloodReports() = call { api.getBloodReports() }

    // CHANGED v13: callers may now include wbc, total_protein, albumin, lft in the map
    suspend fun addBloodReport(fields: Map<String, String>) =
        call { api.addBloodReport(fields) }

    // ── Complaints ───────────────────────────────────────────────────────
    suspend fun getComplaints() = call { api.getComplaints() }

    suspend fun addComplaint(complaint: String, severity: String) =
        call { api.addComplaint(ComplaintRequest(complaint, severity)) }

    // ── Diet & Medications ──────────────────────────────────────────────
    suspend fun getDietCharts() = call { api.getDietCharts() }

    suspend fun getMedications() = call { api.getMedications() }

    // NEW v13: log a medication as taken/not-taken for today
    suspend fun logMedicationTaken(medId: String, taken: Boolean) =
        call { api.logMedicationTaken(MedLogRequest(medId, taken)) }

    // ── Daily Intake (NEW v13) ───────────────────────────────────────────
    suspend fun getDailyIntake() = call { api.getDailyIntake() }

    suspend fun updateDailyIntake(proteinG: Double?, waterMl: Int?) =
        call { api.updateDailyIntake(IntakeUpdateRequest(proteinG, waterMl)) }

    // ── Notifications ────────────────────────────────────────────────────
    suspend fun getNotifications() = call { api.getNotifications() }

    suspend fun markAllNotificationsRead() = call { api.markAllNotificationsRead() }

    suspend fun markNotificationRead(id: String) = call { api.markNotificationRead(id) }
}
