package com.bariatricnepal.app.ui.intake

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.databinding.FragmentIntakeBinding
import com.bariatricnepal.app.util.ApiResult
import com.bariatricnepal.app.util.toSafeDouble
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * NEW v13: Daily protein & water intake tracking screen.
 * Endpoints: GET /daily-intake  |  POST /daily-intake
 * Goals (protein_goal_g, water_goal_ml) come from the patient Profile.
 */
class IntakeFragment : Fragment() {

    private var _b: FragmentIntakeBinding? = null
    private val b get() = _b!!
    private val app get() = requireActivity().application as BNApplication

    // Cached goals from profile
    private var proteinGoal = 60.0
    private var waterGoal   = 2000

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentIntakeBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.swipeRefresh.setOnRefreshListener { load() }
        b.btnSaveIntake.setOnClickListener { saveIntake() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            b.swipeRefresh.isRefreshing = true

            // Load profile for goals
            val profileResult = app.repository?.getProfile()
            if (profileResult is ApiResult.Success) {
                val p = profileResult.data
                proteinGoal = p.protein_goal_g.toSafeDouble() ?: 60.0
                waterGoal   = p.water_goal_ml?.toIntOrNull() ?: 2000
                b.tvGoalSummary.text =
                    "Daily goals: ${proteinGoal.toInt()}g protein · ${waterGoal}ml water (set by doctor)"
            }

            // Load today's intake
            when (val r = app.repository?.getDailyIntake()) {
                is ApiResult.Success -> renderIntake(
                    r.data.protein_g.toSafeDouble() ?: 0.0,
                    r.data.water_ml?.toIntOrNull() ?: 0
                )
                is ApiResult.Error -> renderIntake(0.0, 0) // no record yet — start at 0
            }

            if (_b != null) b.swipeRefresh.isRefreshing = false
        }
    }

    private fun renderIntake(proteinG: Double, waterMl: Int) {
        if (_b == null) return

        val protPct  = ((proteinG / proteinGoal) * 100).coerceAtMost(100.0).roundToInt()
        val waterPct = ((waterMl.toDouble() / waterGoal) * 100).coerceAtMost(100.0).roundToInt()

        b.progressProtein.progress = protPct
        b.tvProteinLabel.text = "${proteinG.toInt()}g / ${proteinGoal.toInt()}g"

        b.progressWater.progress = waterPct
        b.tvWaterLabel.text = "${waterMl}ml / ${waterGoal}ml"

        b.etProtein.setText(proteinG.toInt().toString())
        b.etWater.setText(waterMl.toString())
    }

    private fun saveIntake() {
        val protein = b.etProtein.text.toString().trim().toDoubleOrNull()
        val water   = b.etWater.text.toString().trim().toIntOrNull()

        if (protein == null && water == null) {
            Toast.makeText(requireContext(), "Enter a protein or water value", Toast.LENGTH_SHORT).show()
            return
        }

        b.btnSaveIntake.isEnabled = false
        lifecycleScope.launch {
            when (val r = app.repository?.updateDailyIntake(protein, water)) {
                is ApiResult.Success -> {
                    if (_b == null) return@launch
                    val data = r.data.data
                    renderIntake(
                        data?.protein_g.toSafeDouble() ?: (protein ?: 0.0),
                        data?.water_ml?.toIntOrNull() ?: (water ?: 0)
                    )
                    Toast.makeText(requireContext(), r.data.message ?: "Saved!", Toast.LENGTH_SHORT).show()
                }
                is ApiResult.Error -> {
                    Toast.makeText(requireContext(), r.message, Toast.LENGTH_SHORT).show()
                }
            }
            if (_b != null) b.btnSaveIntake.isEnabled = true
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
