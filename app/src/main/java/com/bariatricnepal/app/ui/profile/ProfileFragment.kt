package com.bariatricnepal.app.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.R
import com.bariatricnepal.app.databinding.FragmentProfileBinding
import com.bariatricnepal.app.ui.common.MainActivity
import com.bariatricnepal.app.ui.login.ChangePasswordActivity
import com.bariatricnepal.app.ui.login.LoginActivity
import com.bariatricnepal.app.util.ApiResult
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!
    private val app get() = requireActivity().application as BNApplication

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri: Uri -> uploadPhoto(uri) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentProfileBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.swipeRefresh.setOnRefreshListener { load() }
        b.btnChangePhoto.setOnClickListener { openGallery() }
        b.btnUpdate.setOnClickListener { updateProfile() }
        b.btnChangePassword.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }
        b.btnLogout.setOnClickListener { confirmLogout() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            b.swipeRefresh.isRefreshing = true
            when (val r = app.repository?.getProfile()) {
                is ApiResult.Success -> {
                    if (_b == null) return@launch
                    val p = r.data
                    b.tvProfileName.text = p.full_name
                    b.etFullName.setText(p.full_name)
                    b.etPhone.setText(p.phone ?: "")
                    b.etEmail.setText(p.email ?: "")
                    showAvatar(p.profile_picture)
                    app.sessionStore.profilePicture = p.profile_picture
                }
                is ApiResult.Error -> { /* ignore */ }
            }
            _b?.swipeRefresh?.isRefreshing = false
        }
    }

    private fun showAvatar(url: String?) {
        if (_b == null) return
        if (url.isNullOrBlank()) {
            b.ivAvatar.visibility = View.GONE
            b.tvAvatarInitials.visibility = View.VISIBLE
            val name = app.sessionStore.fullName.orEmpty()
            b.tvAvatarInitials.text =
                if (name.isNotBlank()) name.trim().take(1).uppercase() else "🙂"
        } else {
            b.tvAvatarInitials.visibility = View.GONE
            b.ivAvatar.visibility = View.VISIBLE
            Glide.with(this).load(url).circleCrop().into(b.ivAvatar)
        }
        // Also update toolbar avatar in MainActivity
        (requireActivity() as? MainActivity)?.showAvatar(url)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImage.launch(intent)
    }

    private fun uploadPhoto(uri: Uri) {
        showAvatarMsg("Uploading…", false)
        lifecycleScope.launch {
            try {
                val file = uriToFile(uri)
                if (file == null) {
                    showAvatarMsg("Could not read the image.", true)
                    return@launch
                }
                when (val r = app.repository?.uploadProfilePicture(file)) {
                    is ApiResult.Success -> {
                        if (_b == null) return@launch
                        showAvatarMsg("Photo updated!", false)
                        r.data.url?.let { url: String ->
                            showAvatar(url)
                            app.sessionStore.profilePicture = url
                        }
                    }
                    is ApiResult.Error -> showAvatarMsg(r.message, true)
                }
            } catch (e: Exception) {
                showAvatarMsg("Upload failed.", true)
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val ctx = requireContext()
            val ins = ctx.contentResolver.openInputStream(uri) ?: return null
            val ext = ctx.contentResolver.getType(uri)?.substringAfterLast("/") ?: "jpg"
            val tmp = File(ctx.cacheDir, "upload_${System.currentTimeMillis()}.$ext")
            FileOutputStream(tmp).use { out -> ins.copyTo(out) }
            tmp
        } catch (e: Exception) {
            null
        }
    }

    private fun showAvatarMsg(text: String, isError: Boolean) {
        if (_b == null) return
        b.tvAvatarMsg.visibility = View.VISIBLE
        b.tvAvatarMsg.text = text
        b.tvAvatarMsg.setTextColor(
            requireContext().getColor(if (isError) R.color.bn_red else R.color.bn_green)
        )
    }

    private fun updateProfile() {
        val name = b.etFullName.text.toString().trim()
        if (name.isEmpty()) {
            b.tvUpdateError.text = "Full name is required."
            b.tvUpdateError.visibility = View.VISIBLE
            return
        }
        b.tvUpdateError.visibility = View.GONE

        // Update session and toolbar immediately
        app.sessionStore.fullName = name
        b.tvProfileName.text = name
        (requireActivity() as? MainActivity)?.mainBinding?.tvUserName?.text = name

        b.tvUpdateError.text = "Profile saved! Changes will sync with server on next login."
        b.tvUpdateError.visibility = View.VISIBLE
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout_btn))
            .setMessage(getString(R.string.logout_confirm))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                app.sessionStore.clearSession()
                startActivity(
                    Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
