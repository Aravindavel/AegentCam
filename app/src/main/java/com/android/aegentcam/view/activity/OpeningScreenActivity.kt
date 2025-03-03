package com.android.aegentcam.view.activity

import android.content.Intent
import android.os.Bundle
import com.android.aegentcam.databinding.ActivityOpeningScreenBinding
import com.android.aegentcam.helper.RuntimePermissionDialogFragment
import com.android.aegentcam.helper.RuntimePermissionDialogFragment.Companion.checkPermissionStatus
import com.android.aegentcam.network.NetworkResult

class OpeningScreenActivity : BaseActivity(), RuntimePermissionDialogFragment.RuntimePermissionRequestedCallback {

    lateinit var binding : ActivityOpeningScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpeningScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnGetStarted.setOnClickListener {
            checkAllPermission()
        }
    }

    private fun checkAllPermission() {
        checkPermissionStatus(this, supportFragmentManager, this, RuntimePermissionDialogFragment.PERMISSION_ARRAY, 0, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onSuccess(networkResult: NetworkResult<Any>) {

    }

    override fun onFailure(networkResult: NetworkResult<Any>) {

    }

    override fun onLoading(networkResult: NetworkResult<Any>) {

    }

    override fun permissionGranted(
        requestCodeForCallbackIdentificationCode: Int,
        requestCodeForCallbackIdentificationCodeSubDivision: Int
    ) {
        sessionManager.getStartedShown = "1"
        startActivity(Intent(this, BluetoothConnectionActivity::class.java))
    }

    override fun permissionDenied(
        requestCodeForCallbackIdentificationCode: Int,
        requestCodeForCallbackIdentificationCodeSubDivision: Int
    ) {
        
    }
}