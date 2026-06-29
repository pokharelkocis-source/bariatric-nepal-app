package com.bariatricnepal.app.ui.common

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.data.api.ApiClient
import com.bariatricnepal.app.databinding.ActivityServerSetupBinding
import com.bariatricnepal.app.ui.login.LoginActivity

class ServerSetupActivity : AppCompatActivity() {
    private lateinit var b: ActivityServerSetupBinding
    private val app get() = application as BNApplication

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        if (app.sessionStore.isServerConfigured) { goNext(); return }
        b = ActivityServerSetupBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnContinue.setOnClickListener { handleContinue() }
    }

    private fun handleContinue() {
        val input = b.etServerUrl.text.toString().trim()
        if (input.length < 4 || !input.contains(".")) {
            b.tvServerError.visibility = View.VISIBLE; return
        }
        b.tvServerError.visibility = View.GONE
        app.sessionStore.baseUrl = ApiClient.normalizeBaseUrl(input)
        app.rebuildRepository()
        goNext()
    }

    private fun goNext() {
        val target = if (app.sessionStore.isLoggedIn) MainActivity::class.java else LoginActivity::class.java
        startActivity(Intent(this, target)); finish()
    }
}
