package com.bariatricnepal.app.ui.blood

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.databinding.FragmentBloodBinding
import com.bariatricnepal.app.util.ApiResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BloodFragment : Fragment() {
    private var _b: FragmentBloodBinding? = null
    private val b get() = _b!!
    private val app get() = requireActivity().application as BNApplication
    private val adapter = BloodReportAdapter()
    private val sqlFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dispFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val selectedDate: Calendar = Calendar.getInstance()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentBloodBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.rvBloodReports.layoutManager = LinearLayoutManager(requireContext())
        b.rvBloodReports.adapter = adapter
        b.etReportDate.setText(dispFmt.format(selectedDate.time))
        b.etReportDate.setOnClickListener { showDatePicker() }
        b.headerAddReport.setOnClickListener { toggleForm() }
        b.tvToggleAdd.setOnClickListener { toggleForm() }
        b.btnSubmitReport.setOnClickListener { submitReport() }
        b.swipeRefresh.setOnRefreshListener { load() }
        load()
    }

    private fun toggleForm() {
        b.formAddReport.visibility = if (b.formAddReport.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun showDatePicker() {
        DatePickerDialog(requireContext(), { _, y, m, d ->
            selectedDate.set(y, m, d)
            b.etReportDate.setText(dispFmt.format(selectedDate.time))
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).also {
            it.datePicker.maxDate = System.currentTimeMillis()
            it.show()
        }
    }

    private fun load() {
        lifecycleScope.launch {
            b.swipeRefresh.isRefreshing = true
            when (val r = app.repository?.getBloodReports()) {
                is ApiResult.Success -> {
                    if (_b == null) return@launch
                    adapter.submitList(r.data)
                    b.tvBloodEmpty.visibility = if (r.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is ApiResult.Error -> {
                    if (_b == null) return@launch
                    b.tvBloodEmpty.visibility = View.VISIBLE
                    b.tvBloodEmpty.text = r.message
                }
            }
            if (_b != null) b.swipeRefresh.isRefreshing = false
        }
    }

    private fun submitReport() {
        val fields = mutableMapOf<String, String>()
        fields["report_date"] = sqlFmt.format(selectedDate.time)

        fun addField(key: String, et: EditText) {
            val v = et.text.toString().trim()
            if (v.isNotEmpty()) fields[key] = v
        }

        // Original fields
        addField("hb", b.etHb)
        addField("vitamin_b12", b.etB12)
        addField("vitamin_d", b.etVitD)
        addField("ferritin", b.etFerritin)
        addField("calcium", b.etCalcium)
        addField("glucose", b.etGlucose)
        addField("hba1c", b.etHba1c)
        addField("cholesterol", b.etCholesterol)
        addField("triglycerides", b.etTriglycerides)
        addField("notes", b.etReportNotes)
        // NEW v13 fields
        addField("wbc", b.etWbc)
        addField("total_protein", b.etTotalProtein)
        addField("albumin", b.etAlbumin)
        addField("lft", b.etLft)

        b.tvReportFormError.visibility = View.GONE
        b.btnSubmitReport.isEnabled = false

        lifecycleScope.launch {
            when (val r = app.repository?.addBloodReport(fields)) {
                is ApiResult.Success -> {
                    if (_b == null) return@launch
                    b.btnSubmitReport.isEnabled = true
                    listOf(b.etHb, b.etB12, b.etVitD, b.etFerritin, b.etCalcium,
                           b.etGlucose, b.etHba1c, b.etCholesterol, b.etTriglycerides,
                           b.etReportNotes, b.etWbc, b.etTotalProtein, b.etAlbumin, b.etLft
                    ).forEach { it.text.clear() }
                    b.formAddReport.visibility = View.GONE
                    load()
                }
                is ApiResult.Error -> {
                    if (_b == null) return@launch
                    b.btnSubmitReport.isEnabled = true
                    b.tvReportFormError.text = r.message
                    b.tvReportFormError.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
