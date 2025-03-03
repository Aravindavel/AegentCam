package com.android.aegentcam.view.fragment

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.android.aegentcam.R
import com.android.aegentcam.databinding.FragmentSettingsBinding
import com.android.aegentcam.network.NetworkResult
import com.android.aegentcam.view.activity.CameraSettingActivity

class SettingsFragment : BaseFragment() {

    lateinit var binding : FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSettingsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initViews()
        return binding.root
    }

    override fun onSuccess(networkResult: NetworkResult<Any>) {
        
    }

    override fun onFailure(networkResult: NetworkResult<Any>) {
        
    }

    override fun onLoading(networkResult: NetworkResult<Any>) {
        
    }

    private fun initViews() {
        binding.tvAppVersion.text = getAppVersion(requireContext())
        binding.tvDeviceName.text = sessionManager.bluetoothDeviceName
        binding.rltDeviceInfo.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_deviceInfoFragment)
        }
        binding.rltCameraSetting.setOnClickListener {
            val intent = Intent(requireContext(), CameraSettingActivity::class.java)
            val animation = ActivityOptions.makeCustomAnimation(
                requireContext(),
                R.anim.ub__slide_in_right,
                R.anim.ub__slide_out_left
            ).toBundle()
            startActivity(intent, animation)
        }
        binding.rltRecordSetting.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_recordSettingFragment)
        }
        binding.rltAppVersion.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_appVersionFragment)
        }
    }


    fun getAppVersion(context: Context): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            "Version: ${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } else {
            "Version: ${packageInfo.versionName} (${packageInfo.versionCode})"
        }
    }


}