package com.android.aegentcam.view.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.aegentcam.R
import com.android.aegentcam.databinding.FragmentAppVersionBinding
import com.android.aegentcam.helper.visible

class AppVersionFragment : Fragment() {

    lateinit var binding: FragmentAppVersionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAppVersionBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initView()
        return binding.root
    }

    private fun initView() {
        binding.header.tvTitle.text = getString(R.string.app_version)
        binding.header.ivBack.visible()
        binding.header.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.tvAppVersion.text = getString(R.string.current_version)+" "+getAppVersion(requireContext())
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