package com.bariatricnepal.app.ui.weight

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.data.api.models.Profile
import com.bariatricnepal.app.databinding.FragmentWeightBinding
import com.bariatricnepal.app.ui.complaints.ComplaintsActivity
import com.bariatricnepal.app.util.ApiResult
import com.bariatricnepal.app.util.formatNumber
import com.bariatricnepal.app.util.toSafeDouble
import kotlinx.coroutines.launch

class WeightFragment : Fragment() {

    private var _binding: FragmentWeightBinding? = null
    private val binding get() = _binding!!
    private val app get() = requireActivity().application as BNApplication
    private val adapter = WeightLogAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvWeightHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWeightHistory.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { loadAll() }
        binding.btnLogWeight.setOnClickListener { submitWeight() }
        binding.cardComplaints.setOnClickListener {
            startActivity(Intent(requireContext(), ComplaintsActivity::class.java))
        }

        loadAll()
    }

    private fun loadAll() {
        loadProfile()
        loadHistory()
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            when (val result = app.repository?.getProfile()) {
                is ApiResult.Success -> applyProfile(result.data)
                is ApiResult.Error -> { /* keep last known values */ }
            }
        }
    }

    private fun applyProfile(p: Profile) {
        if (_binding == null) return
        binding.tvStatValWeight.text = p.current_weight.formatNumber("kg")
        binding.tvStatValBmi.text = p.current_bmi.formatNumber()
        binding.tvStatValTarget.text =
            if (p.target_weight.toSafeDouble() != null) p.target_weight.formatNumber("kg") else "Not set"

        val initial = p.initial_weight.toSafeDouble()
        val current = p.current_weight.toSafeDouble()
        if (initial != null && current != null) {
            val lost = initial - current
            binding.tvStatValLost.text = when {
                lost > 0 -> "-${String.format("%.1f", lost)} kg"
                lost < 0 -> "+${String.format("%.1f", -lost)} kg"
                else -> "0 kg"
            }
        }
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            binding.swipeRefresh.isRefreshing = true
            when (val result = app.repository?.getWeights()) {
                is ApiResult.Success -> {
                    if (_binding == null) return@launch
                    val logs = result.data
                    adapter.submitList(logs)
                    binding.tvWeightEmpty.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
                    val points = logs.reversed().mapNotNull { it.weight_kg.toSafeDouble()?.toFloat() }
                    binding.weightChart.setValues(points)
                }
                is ApiResult.Error -> {
                    if (_binding == null) return@launch
                    binding.tvWeightEmpty.visibility = View.VISIBLE
                    binding.tvWeightEmpty.text = result.message
                }
            }
            if (_binding != null) binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun submitWeight() {
        val weightStr = binding.etWeight.text.toString().trim()
        val weight = weightStr.toDoubleOrNull()
        if (weight == null || weight <= 0) {
            showFormError("Please enter a valid weight.")
            return
        }
        // CHANGED v13: height no longer sent — plugin uses patient's stored height
        val notes = binding.etWeightNotes.text.toString().trim().ifEmpty { null }

        binding.tvWeightFormError.visibility = View.GONE
        binding.btnLogWeight.isEnabled = false

        lifecycleScope.launch {
            when (val result = app.repository?.logWeight(weight, notes)) {
                is ApiResult.Success -> {
                    if (_binding == null) return@launch
                    binding.etWeight.text.clear()
                    binding.etWeightNotes.text.clear()
                    binding.btnLogWeight.isEnabled = true
                    loadAll()
                }
                is ApiResult.Error -> {
                    if (_binding == null) return@launch
                    binding.btnLogWeight.isEnabled = true
                    showFormError(result.message)
                }
            }
        }
    }

    private fun showFormError(message: String) {
        binding.tvWeightFormError.text = message
        binding.tvWeightFormError.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
