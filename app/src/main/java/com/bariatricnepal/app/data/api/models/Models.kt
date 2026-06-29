package com.bariatricnepal.app.data.api.models

/**
 * IMPORTANT: WordPress's $wpdb returns every database column as a PHP
 * string, regardless of the column's actual SQL type (int, decimal, etc.).
 * That means most "numeric" fields below arrive over the wire as JSON
 * strings like "85.5", not JSON numbers. Gson's String adapter safely
 * accepts either a JSON string OR a JSON number, so declaring these as
 * String here (and converting with the helpers in NumberUtils.kt when we
 * actually need to do math) is the safe, crash-proof choice.
 */

// ── Auth ─────────────────────────────────────────────────────────────────

data class LoginRequest(val identifier: String, val password: String)

data class LoginResponse(
    val token: String,
    val patient_id: String,
    val full_name: String,
    val password_changed: Boolean
)

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String
)

// ── Generic ──────────────────────────────────────────────────────────────

data class GenericResponse(val message: String?, val url: String? = null)

// ── Profile ──────────────────────────────────────────────────────────────

data class BmiCategory(val label: String?, val color: String?)

data class Profile(
    val id: String,
    val full_name: String,
    val phone: String?,
    val email: String?,
    val dob: String?,
    val gender: String?,
    val surgery_date: String?,
    val surgery_type: String?,
    val initial_weight: String?,
    val initial_height: String?,
    val target_weight: String?,
    // NEW v13: protein & water goals set by doctor
    val protein_goal_g: String?,
    val water_goal_ml: String?,
    val profile_picture: String?,
    val current_weight: String?,
    val current_bmi: String?,
    val bmi_category: BmiCategory?
)

data class UpdateProfileRequest(
    val full_name: String,
    val phone: String?,
    val email: String?
)

// ── Weight ───────────────────────────────────────────────────────────────

data class WeightLog(
    val id: String,
    val weight_kg: String,
    val bmi: String?,
    val notes: String?,
    val logged_at: String
)

// CHANGED v13: height_cm removed — plugin now reads initial_height from patient record
data class WeightRequest(
    val weight_kg: Double,
    val notes: String?
)

data class WeightResponse(
    val message: String?,
    val bmi: String?,
    val bmi_category: BmiCategory?
)

// ── Blood Reports ────────────────────────────────────────────────────────

data class BloodReport(
    val id: String,
    val report_date: String,
    val hb: String?,
    // NEW v13: wbc, total_protein, albumin, lft
    val wbc: String?,
    val vitamin_b12: String?,
    val vitamin_d: String?,
    val total_protein: String?,
    val albumin: String?,
    val ferritin: String?,
    val calcium: String?,
    val glucose: String?,
    val hba1c: String?,
    val cholesterol: String?,
    val triglycerides: String?,
    val lft: String?,
    val notes: String?,
    val file_url: String?,
    val reviewed: String?,
    val doctor_notes: String?
)

// ── Complaints ───────────────────────────────────────────────────────────

data class Complaint(
    val id: String,
    val complaint: String,
    val severity: String,
    val status: String,
    val doctor_reply: String?,
    val created_at: String
)

data class ComplaintRequest(val complaint: String, val severity: String)

// ── Diet ─────────────────────────────────────────────────────────────────

data class Meal(
    val meal: String?,
    val time: String?,
    val food: String?,
    val calories: String?
)

data class DietChart(
    val id: String,
    val title: String,
    val chart_data: List<Meal>?,
    val valid_from: String?,
    val valid_until: String?,
    val notes: String?,
    val created_at: String?
)

// ── Medications ──────────────────────────────────────────────────────────

data class Medication(
    val id: String,
    val name: String,
    val dosage: String?,
    val frequency: String?,
    // NEW v13: take_time field
    val take_time: String?,
    val instructions: String?,
    val start_date: String?,
    val end_date: String?,
    val is_active: String?,
    val created_at: String?
)

// NEW v13: request body for POST /medication-log
data class MedLogRequest(
    val med_id: String,
    val taken: Boolean
)

// ── Daily Intake (NEW v13) ────────────────────────────────────────────────

data class DailyIntake(
    val patient_id: String?,
    val intake_date: String?,
    val protein_g: String?,   // arrives as string from WP
    val water_ml: String?,    // arrives as string from WP
    val logged_at: String?
)

data class IntakeUpdateRequest(
    val protein_g: Double?,
    val water_ml: Int?
)

data class IntakeUpdateResponse(
    val message: String?,
    val data: DailyIntake?
)

// ── Notifications ────────────────────────────────────────────────────────

data class AppNotification(
    val id: String,
    val type: String?,
    val title: String,
    val message: String,
    val icon: String?,
    val is_read: String?,
    val created_at: String
)
