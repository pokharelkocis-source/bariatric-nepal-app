package com.bariatricnepal.app.ui.complaints

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.databinding.ActivityComplaintsBinding
import com.bariatricnepal.app.util.ApiResult
import kotlinx.coroutines.launch

class ComplaintsActivity : AppCompatActivity() {
    private lateinit var b: ActivityComplaintsBinding
    private val app get() = application as BNApplication
    private val adapter = ComplaintAdapter()

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityComplaintsBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnBack.setOnClickListener { finish() }
        b.rvComplaints.layoutManager = LinearLayoutManager(this)
        b.rvComplaints.adapter = adapter
        b.swipeRefresh.setOnRefreshListener { load() }
        b.btnSubmit.setOnClickListener { submit() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            b.swipeRefresh.isRefreshing = true
            when (val r = app.repository.getComplaints()) {
                is ApiResult.Success -> {
                    adapter.submitList(r.data)
                    b.tvEmpty.visibility = if (r.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is ApiResult.Error -> {
                    b.tvEmpty.visibility = View.VISIBLE
                    b.tvEmpty.text = r.message
                }
            }
            b.swipeRefresh.isRefreshing = false
        }
    }

    private fun submit() {
        val text = b.etComplaintText.text.toString().trim()
        if (text.isEmpty()) { showError("Please describe what you're experiencing."); return }
        val severity = when (b.rgSeverity.checkedRadioButtonId) {
            b.rbMedium.id -> "medium"
            b.rbHigh.id   -> "high"
            b.rbUrgent.id -> "urgent"
            else          -> "low"
        }
        b.tvFormError.visibility = View.GONE
        b.btnSubmit.isEnabled = false
        lifecycleScope.launch {
            when (val r = app.repository.addComplaint(text, severity)) {
                is ApiResult.Success -> {
                    b.btnSubmit.isEnabled = true
                    b.etComplaintText.text.clear()
                    b.rbLow.isChecked = true
                    load()
                }
                is ApiResult.Error -> {
                    b.btnSubmit.isEnabled = true
                    showError(r.message)
                }
            }
        }
    }

    private fun showError(msg: String) { b.tvFormError.text = msg; b.tvFormError.visibility = View.VISIBLE }
}
