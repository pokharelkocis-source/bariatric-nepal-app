package com.bariatricnepal.app.ui.common

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.R
import com.bariatricnepal.app.databinding.ActivityMainBinding
import com.bariatricnepal.app.ui.blood.BloodFragment
import com.bariatricnepal.app.ui.diet.DietFragment
import com.bariatricnepal.app.ui.intake.IntakeFragment
import com.bariatricnepal.app.ui.login.LoginActivity
import com.bariatricnepal.app.ui.meds.MedsFragment
import com.bariatricnepal.app.ui.notifications.NotificationsActivity
import com.bariatricnepal.app.ui.profile.ProfileFragment
import com.bariatricnepal.app.ui.weight.WeightFragment
import com.bariatricnepal.app.util.ApiResult
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding: ActivityMainBinding
        private set

    private val app get() = application as BNApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!app.sessionStore.isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Make sure repository is ready
        if (app.repository == null) app.rebuildRepository()

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        mainBinding.tvUserName.text = app.sessionStore.fullName ?: getString(R.string.app_name)

        mainBinding.notifFrame.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        mainBinding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_weight  -> WeightFragment()
                R.id.nav_blood   -> BloodFragment()
                R.id.nav_intake  -> IntakeFragment()
                R.id.nav_diet    -> DietFragment()
                R.id.nav_meds    -> MedsFragment()
                R.id.nav_profile -> ProfileFragment()
                else             -> WeightFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }

        if (savedInstanceState == null) {
            mainBinding.bottomNav.selectedItemId = R.id.nav_weight
        }

        loadProfileHeader()
        refreshUnreadBadge()
    }

    override fun onResume() {
        super.onResume()
        refreshUnreadBadge()
    }

    private fun loadProfileHeader() {
        showAvatar(app.sessionStore.profilePicture)
        val repo = app.repository ?: return
        lifecycleScope.launch {
            try {
                when (val result = repo.getProfile()) {
                    is ApiResult.Success -> {
                        val p = result.data
                        mainBinding.tvUserName.text = p.full_name
                        app.sessionStore.fullName = p.full_name
                        app.sessionStore.profilePicture = p.profile_picture
                        showAvatar(p.profile_picture)
                    }
                    is ApiResult.Error -> { /* keep cached */ }
                }
            } catch (e: Exception) { /* ignore */ }
        }
    }

    fun showAvatar(url: String?) {
        if (url.isNullOrBlank()) {
            mainBinding.ivAvatar.visibility = View.GONE
            mainBinding.tvAvatarInitials.visibility = View.VISIBLE
            val name = app.sessionStore.fullName.orEmpty()
            mainBinding.tvAvatarInitials.text =
                if (name.isNotBlank()) name.trim().take(1).uppercase() else "🙂"
        } else {
            mainBinding.tvAvatarInitials.visibility = View.GONE
            mainBinding.ivAvatar.visibility = View.VISIBLE
            Glide.with(this).load(url).circleCrop().into(mainBinding.ivAvatar)
        }
    }

    fun refreshUnreadBadge() {
        val repo = app.repository ?: return
        lifecycleScope.launch {
            try {
                when (val result = repo.getNotifications()) {
                    is ApiResult.Success -> {
                        val unread = result.data.count { it.is_read != "1" }
                        if (unread > 0) {
                            mainBinding.tvNotifBadge.visibility = View.VISIBLE
                            mainBinding.tvNotifBadge.text = if (unread > 9) "9+" else unread.toString()
                        } else {
                            mainBinding.tvNotifBadge.visibility = View.GONE
                        }
                    }
                    is ApiResult.Error -> { /* ignore */ }
                }
            } catch (e: Exception) { /* ignore */ }
        }
    }
}
