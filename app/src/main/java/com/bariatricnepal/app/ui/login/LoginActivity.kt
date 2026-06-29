package com.bariatricnepal.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.R
import com.bariatricnepal.app.databinding.ActivityLoginBinding
import com.bariatricnepal.app.ui.common.MainActivity
import com.bariatricnepal.app.ui.common.ServerSetupActivity
import com.bariatricnepal.app.util.ApiResult
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var b: ActivityLoginBinding
    private val app get() = application as BNApplication
    private var passwordVisible = false

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        if (!app.sessionStore.isServerConfigured) {
            startActivity(Intent(this, ServerSetupActivity::class.java)); finish(); return
        }
        if (app.sessionStore.isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java)); finish(); return
        }
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnLogin.setOnClickListener { handleLogin() }
        b.btnTogglePassword.setOnClickListener { togglePassword() }
        b.tvChangeServer.setOnClickListener { changeServer() }
    }

    private fun togglePassword() {
        passwordVisible = !passwordVisible
        b.etPassword.inputType = if (passwordVisible)
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        else
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        b.btnTogglePassword.setImageResource(
            if (passwordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye
        )
        b.etPassword.setSelection(b.etPassword.text.length)
    }

    private fun changeServer() {
        app.sessionStore.baseUrl = null
        startActivity(Intent(this, ServerSetupActivity::class.java)); finish()
    }

    private fun handleLogin() {
        val id = b.etIdentifier.text.toString().trim()
        val pw = b.etPassword.text.toString()
        if (id.isEmpty() || pw.isEmpty()) { showError("Please enter both your phone/email and password."); return }
        setLoading(true)
        lifecycleScope.launch {
            when (val r = app.repository.login(id, pw)) {
                is ApiResult.Success -> {
                    val data = r.data
                    app.sessionStore.token = data.token
                    app.sessionStore.patientId = data.patient_id
                    app.sessionStore.fullName = data.full_name
                    setLoading(false)
                    val target = if (!data.password_changed) ChangePasswordActivity::class.java else MainActivity::class.java
                    startActivity(Intent(this@LoginActivity, target)); finish()
                }
                is ApiResult.Error -> { setLoading(false); showError(r.message) }
            }
        }
    }

    private fun showError(msg: String) { b.tvLoginError.text = msg; b.tvLoginError.visibility = View.VISIBLE }
    private fun setLoading(on: Boolean) {
        b.progressBar.visibility = if (on) View.VISIBLE else View.GONE
        b.btnLogin.isEnabled = !on
        b.btnLogin.alpha = if (on) 0.6f else 1f
    }
}
