package com.android.aegentcam.view.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.android.aegentcam.R
import com.android.aegentcam.databinding.ActivityFullScreenSliderBinding
import com.android.aegentcam.network.NetworkResult
import com.android.aegentcam.view.adapter.MediaSliderAdapter

class FullScreenSliderActivity : BaseActivity() {

    lateinit var binding : ActivityFullScreenSliderBinding
    private lateinit var mediaList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFullScreenSliderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()

    }

    private fun initViews() {
        mediaList = intent.getStringArrayListExtra("mediaList") ?: arrayListOf()
        val startPosition = intent.getIntExtra("position", 0)

        val adapter = MediaSliderAdapter(this, mediaList)
        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(startPosition, false)
    }

    override fun onSuccess(networkResult: NetworkResult<Any>) {
        
    }

    override fun onFailure(networkResult: NetworkResult<Any>) {
        
    }

    override fun onLoading(networkResult: NetworkResult<Any>) {
        
    }
}