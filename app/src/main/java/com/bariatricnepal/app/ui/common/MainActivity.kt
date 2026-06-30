package com.bariatricnepal.app.ui.common

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
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

    // True when DietFragment is shown "outside" the normal bottom-nav flow
    // (opened from the Meds tab's "View Diet Chart" card)
    private var isShowingDietOverlay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!app.sessionStore.isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java)); finish(); return
        }
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mainBinding.tvUserName.text = app.sessionStore.fullName ?: getString(R.string.app_name)
        mainBinding.notifFrame.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        mainBinding.bottomNav.setOnItemSelectedListener { item ->
            isShowingDietOverlay = false
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_weight  -> WeightFragment()
                R.id.nav_blood   -> BloodFragment()
                R.id.nav_intake  -> IntakeFragment()
                R.id.nav_meds    -> MedsFragment()
                R.id.nav_profile -> ProfileFragment()
                else             -> WeightFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment).commit()
            true
        }
        if (savedInstanceState == null) mainBinding.bottomNav.selectedItemId = R.id.nav_weight

        // Back press: if showing Diet overlay, return to Meds tab instead of exiting
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isShowingDietOverlay) {
                    isShowingDietOverlay = false
                    mainBinding.bottomNav.selectedItemId = R.id.nav_meds
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        loadProfileHeader()
        refreshUnreadBadge()
    }

    /** Called from MedsFragment's "View Diet Chart" card. */
    fun openDiet() {
        isShowingDietOverlay = true
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, DietFragment()).commit()
    }

    override fun onResume() { super.onResume(); refreshUnreadBadge() }

    private fun loadProfileHeader() {
        showAvatar(app.sessionStore.profilePicture)
        lifecycleScope.launch {
            try {
                when (val r = app.repository.getProfile()) {
                    is ApiResult.Success -> {
                        mainBinding.tvUserName.text = r.data.full_name
                        app.sessionStore.fullName = r.data.full_name
                        app.sessionStore.profilePicture = r.data.profile_picture
                        showAvatar(r.data.profile_picture)
                    }
                    is ApiResult.Error -> {}
                }
            } catch (e: Exception) {}
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
        lifecycleScope.launch {
            try {
                when (val r = app.repository.getNotifications()) {
                    is ApiResult.Success -> {
                        val unread = r.data.count { it.is_read != "1" }
                        mainBinding.tvNotifBadge.visibility =
                            if (unread > 0) View.VISIBLE else View.GONE
                        mainBinding.tvNotifBadge.text =
                            if (unread > 9) "9+" else unread.toString()
                    }
                    is ApiResult.Error -> {}
                }
            } catch (e: Exception) {}
        }
    }
}
