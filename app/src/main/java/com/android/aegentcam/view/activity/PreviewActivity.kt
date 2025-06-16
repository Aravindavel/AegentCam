package com.android.aegentcam.view.activity

import android.app.ActivityOptions
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.aegentcam.R
import com.android.aegentcam.databinding.ActivityPreviewBinding
import com.android.aegentcam.network.NetworkResult
import com.android.mediacodec.AVPacket
import com.android.mediacodec.Packet
import com.android.mediacodec.VideoDecoder.AsyncDataListener
import com.linkflow.blackboxsdk.NeckbandRestApiClient
import com.linkflow.blackboxsdk.rtsp.RTSPStreamManager

class PreviewActivity : BaseStreamActivity(), SurfaceHolder.Callback {

    lateinit var binding : ActivityPreviewBinding

    private var mStreamManager: RTSPStreamManager? = null

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
        initClickView()
    }

    private fun initClickView() {
        binding.ivBack.setSafeOnClickListener {
            if (mWifiHelper!!.isConnected) {
                releaseWait(true)
            } else {
                finish()
            }
        }

        binding.btnMicOn.setOnClickListener {
            if (!mExitingRTSP) {
                mIsMuted = !mIsMuted
                mStreamManager!!.setAudioDisable(mIsMuted)
                binding.btnMicOn.setImageResource(if (mIsMuted) R.drawable.ic_mic_off else R.drawable.ic_mic_on)
            }
        }

        binding.btnSetting.setSafeOnClickListener {
            if (mWifiHelper!!.isConnected) {
                val intent = Intent(this, CameraSettingActivity::class.java)
                val animation = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.ub__slide_in_right, R.anim.ub__slide_out_left
                ).toBundle()
                startActivityForResult(intent, 101,animation);
            }
        }
       
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            recreate()
        }
    }

    override fun onSuccess(networkResult: NetworkResult<Any>) {
        
    }

    override fun onFailure(networkResult: NetworkResult<Any>) {
        
    }

    override fun onLoading(networkResult: NetworkResult<Any>) {
        
    }

    private fun initView() {
        commonMethods.showProgressDialog(this)
        mStreamManager =
            RTSPStreamManager.builder().setAsyncDataListener(object : AsyncDataListener {
                override fun asyncData(type: Int, packet: Packet) {
                    // packet has all information about stream. you can use it.
                    if (type == AVPacket.PT_VIDEO) {
                    } else if (type == AVPacket.PT_AUDIO) {
                    }
                }

                override fun disablePreview(): Boolean {
                    return false
                }
            }).setFrameCallback {
                mRTSPCheckHandler.removeMessages(RTSP_STARTED_CHECK)
                mRTSPCheckHandler.post(Runnable { commonMethods.hideProgressDialog() })
            }.setAudioDecodedListener { bytes, i, i1 -> }.setResolution(3840, 1280).build()

        binding.surface.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mStreamManager!!.setSurface(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        
    }

    @Synchronized
    override fun releaseWait(forceClose: Boolean) {
        super.releaseWait(forceClose)
        if (forceClose) {
            commonMethods.showProgressDialog(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        commonMethods.hideProgressDialog()
    }

    @Synchronized
    override fun release() {
        if (mStreamManager != null) {
            mStreamManager!!.stop { finish() }
        } else {
            finish()
        }
    }

     override fun reload() {
        if (mWifiHelper!!.isConnected) {
            try {
                mStreamManager!!.setUrl(NeckbandRestApiClient.getRTSPUrl())
                if (!mStreamManager!!.isStarted) {
                    mStreamManager!!.start(resources.openRawResourceFd(R.raw.aac_44k))
                }
                Log.e(TAG, "reload")
                Log.e(TAG, "rtspUrl: ${NeckbandRestApiClient.getRTSPUrl()}")
                mPreviewModel!!.activateRTSP(mBTCommandManager!!.accessToken, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}