package com.orbitalsonic.myapplication

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orbitalsonic.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var sharedPreferenceUtils:SharedPreferenceUtils? = null

    private val REQUIRED_PERMISSIONS =
        mutableListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }.toTypedArray()

    private var settingLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result?.let {
//            if (it.resultCode == Activity.RESULT_OK) {
//
//            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var isGranted = true
        REQUIRED_PERMISSIONS.forEach {
            if (permissions[it] != true) {
                isGranted = false
                return@forEach
            }
        }
        if (isGranted) {
            sharedPreferenceUtils?.isFirstTimeAskingPermission = true
            onPermissionGranted()
        } else {
            onPermissionNotGranted()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        sharedPreferenceUtils = SharedPreferenceUtils(getSharedPreferences("app_preferences", Application.MODE_PRIVATE))

        if (isPermissionGranted()) {
            performOperations()
        } else {
            binding.tvMessage.text = getString(R.string.no_permission_is_granted)
        }

        binding.btnPermission.setOnClickListener {
            if (isPermissionGranted()) {
                performOperations()
            } else {
                askForPermission()
            }
        }
    }

    private fun performOperations() {
        binding.tvMessage.text = getString(R.string.all_permission_granted)
        showToast(getString(R.string.all_permission_granted))
    }

    private fun isPermissionGranted(): Boolean {
        var isGranted = true
        REQUIRED_PERMISSIONS.forEach {
            if (ContextCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                isGranted = false
                return@forEach
            }

        }
        return isGranted
    }

    private fun askForPermission() {
        var isRationale = false
        REQUIRED_PERMISSIONS.forEach {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
                isRationale = true
                return@forEach
            }
        }

        if (isRationale) {
            sharedPreferenceUtils?.isFirstTimeAskingPermission = false
            showPermissionDialog()
        } else {
            if (sharedPreferenceUtils?.isFirstTimeAskingPermission == true) {
                requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
            } else {
                showSettingDialog()
            }
        }
    }

    private fun onPermissionGranted() {
        binding.tvMessage.text = getString(R.string.all_permission_granted)
        showToast(getString(R.string.all_permission_granted))
    }

    private fun onPermissionNotGranted() {
        binding.tvMessage.text = getString(R.string.please_grant_permission)
        showToast(getString(R.string.please_grant_permission))
    }

    private fun showPermissionDialog() {
        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.please_grant_permission))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.enable)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        builder.show()
    }

    private fun showSettingDialog() {
        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.allow_permissoin_settings))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.setting)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                openSettingPage()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        builder.show()
    }

    private fun openSettingPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        settingLauncher.launch(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}