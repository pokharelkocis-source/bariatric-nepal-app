package com.bariatricnepal.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.databinding.ActivityChangePasswordBinding
import com.bariatricnepal.app.ui.common.MainActivity
import com.bariatricnepal.app.util.ApiResult
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val app get() = application as BNApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Block back navigation - password change is required on first login
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })

        binding.btnSave.setOnClickListener { handleSave() }
    }

    private fun handleSave() {
        val current = binding.etCurrentPassword.text.toString()
        val new = binding.etNewPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        if (current.isEmpty() || new.isEmpty() || confirm.isEmpty()) {
            showError("Please fill in all fields.")
            return
        }
        if (new.length < 8) {
            showError("New password must be at least 8 characters.")
            return
        }
        if (new != confirm) {
            showError("Passwords don't match.")
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            when (val result = app.repository.changePassword(current, new)) {
                is ApiResult.Success -> {
                    setLoading(false)
                    startActivity(Intent(this@ChangePasswordActivity, MainActivity::class.java))
                    finish()
                }
                is ApiResult.Error -> {
                    setLoading(false)
                    showError(result.message)
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !loading
        binding.btnSave.alpha = if (loading) 0.6f else 1f
    }
}
