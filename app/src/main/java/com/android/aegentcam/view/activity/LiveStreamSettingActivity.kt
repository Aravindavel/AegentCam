package com.android.aegentcam.view.activity

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.aegentcam.R
import com.android.aegentcam.databinding.ActivityLiveStreamSettingBinding
import com.android.aegentcam.network.NetworkResult

class LiveStreamSettingActivity : BaseActivity() {
    lateinit var binding: ActivityLiveStreamSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveStreamSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()
        initClickViews()
    }

    private fun initClickViews() {
        binding.header.ivBack.setSafeOnClickListener { onBackPressed() }
        binding.rltSave.setSafeOnClickListener {
            sessionManager.liveStreamUrl = binding.edtLiveUrl.text.toString()
            onBackPressed()
        }
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.live_stream_setting)
        binding.edtLiveUrl.setText(sessionManager.liveStreamUrl)
    }

    override fun onSuccess(networkResult: NetworkResult<Any>) {
        
    }

    override fun onFailure(networkResult: NetworkResult<Any>) {
        
    }

    override fun onLoading(networkResult: NetworkResult<Any>) {
        
    }
}