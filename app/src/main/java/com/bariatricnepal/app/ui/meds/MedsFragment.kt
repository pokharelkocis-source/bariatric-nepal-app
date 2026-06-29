package com.bariatricnepal.app.ui.meds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.R
import com.bariatricnepal.app.data.api.models.Medication
import com.bariatricnepal.app.databinding.FragmentMedsBinding
import com.bariatricnepal.app.databinding.ItemMedDetailBinding
import com.bariatricnepal.app.databinding.ItemValueChipBinding
import com.bariatricnepal.app.ui.common.AccordionBuilder
import com.bariatricnepal.app.util.ApiResult
import com.bariatricnepal.app.util.DateUtils
import com.bariatricnepal.app.util.groupByDateDesc
import com.bariatricnepal.app.util.toSafeBool
import kotlinx.coroutines.launch

class MedsFragment : Fragment() {
    private var _b: FragmentMedsBinding? = null
    private val b get() = _b!!
    private val app get() = requireActivity().application as BNApplication

    // Track which meds have been logged as taken today (local optimistic state)
    private val takenToday = mutableSetOf<String>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMedsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.swipeRefresh.setOnRefreshListener { load() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            b.swipeRefresh.isRefreshing = true
            when (val r = app.repository?.getMedications()) {
                is ApiResult.Success -> render(r.data)
                is ApiResult.Error   -> showError(r.message)
            }
            if (_b != null) b.swipeRefresh.isRefreshing = false
        }
    }

    private fun render(meds: List<Medication>) {
        if (_b == null) return
        val groups = meds.groupByDateDesc { it.created_at }
        AccordionBuilder.build(requireContext(), b.medsContainer, groups, getString(R.string.no_meds)) { parent, med ->
            val mb = ItemMedDetailBinding.inflate(layoutInflater, parent, false)
            mb.tvMedName.text = med.name
            val active = med.is_active.toSafeBool()
            mb.tvMedStatus.text = if (active) "● Active" else "● Completed"
            mb.tvMedStatus.setBackgroundResource(
                if (active) R.drawable.bg_pill_success else R.drawable.bg_stat_card
            )
            mb.tvMedStatus.setTextColor(
                requireContext().getColor(if (active) R.color.bn_success_text else R.color.bn_muted)
            )
            mb.flexMedDetails.removeAllViews()
            val chipInflater = LayoutInflater.from(requireContext())
            fun chip(label: String, value: String?) {
                if (value.isNullOrBlank()) return
                val c = ItemValueChipBinding.inflate(chipInflater, mb.flexMedDetails, false)
                c.tvChipLabel.text = label
                c.tvChipValue.text = value
                mb.flexMedDetails.addView(c.root)
            }
            chip("Dose", med.dosage)
            chip("Frequency", med.frequency)
            // NEW v13: take_time chip
            chip("Take at", med.take_time)
            chip("From", DateUtils.prettyDate(med.start_date))
            chip("Until", DateUtils.prettyDate(med.end_date))
            if (!med.instructions.isNullOrBlank()) {
                mb.tvMedInstructions.visibility = View.VISIBLE
                mb.tvMedInstructions.text = "📝 ${med.instructions}"
            }

            // NEW v13: "Taken today" checkbox — only for active meds
            if (active) {
                val checkBox = CheckBox(requireContext()).apply {
                    text = "Taken today"
                    isChecked = takenToday.contains(med.id)
                    setTextColor(requireContext().getColor(R.color.bn_text))
                    setPadding(0, 12, 0, 0)
                }
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    lifecycleScope.launch {
                        when (val r = app.repository?.logMedicationTaken(med.id, isChecked)) {
                            is ApiResult.Success -> {
                                if (isChecked) takenToday.add(med.id)
                                else takenToday.remove(med.id)
                            }
                            is ApiResult.Error -> {
                                // Revert on failure
                                checkBox.setOnCheckedChangeListener(null)
                                checkBox.isChecked = !isChecked
                                Toast.makeText(requireContext(), r.message, Toast.LENGTH_SHORT).show()
                                // Re-attach listener
                                attachCheckListener(checkBox, med.id)
                            }
                        }
                    }
                }
                parent.addView(checkBox)
            }

            parent.addView(mb.root)
        }
    }

    private fun attachCheckListener(checkBox: CheckBox, medId: String) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                when (val r = app.repository?.logMedicationTaken(medId, isChecked)) {
                    is ApiResult.Success -> {
                        if (isChecked) takenToday.add(medId) else takenToday.remove(medId)
                    }
                    is ApiResult.Error -> {
                        checkBox.setOnCheckedChangeListener(null)
                        checkBox.isChecked = !isChecked
                        Toast.makeText(requireContext(), r.message, Toast.LENGTH_SHORT).show()
                        attachCheckListener(checkBox, medId)
                    }
                }
            }
        }
    }

    private fun showError(msg: String) {
        if (_b == null) return
        b.medsContainer.removeAllViews()
        val tv = TextView(requireContext())
        tv.text = msg
        tv.setTextColor(requireContext().getColor(R.color.bn_red))
        tv.textSize = 13f
        tv.setPadding(0, 48, 0, 0)
        b.medsContainer.addView(tv)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
