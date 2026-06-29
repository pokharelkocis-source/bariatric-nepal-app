package com.bariatricnepal.app.ui.common

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.ui.login.LoginActivity

/**
 * Hardcoded to www.bariatricnepal.com — skips setup screen entirely.
 */
class ServerSetupActivity : AppCompatActivity() {
    private val app get() = application as BNApplication

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val target = if (app.sessionStore.isLoggedIn)
            MainActivity::class.java else LoginActivity::class.java
        startActivity(Intent(this, target))
        finish()
    }
}
