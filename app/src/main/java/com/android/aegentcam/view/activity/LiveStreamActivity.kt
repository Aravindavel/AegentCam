package com.android.aegentcam.view.activity

import android.app.ActivityOptions
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.android.aegentcam.R
import com.android.aegentcam.configs.AppController
import com.android.aegentcam.databinding.ActivityLiveStreamBinding
import com.android.aegentcam.databinding.ActivityPreviewBinding
import com.android.aegentcam.helper.CommonKeys.isServiceDestroy
import com.android.aegentcam.network.NetworkResult
import com.android.mediacodec.AVPacket
import com.android.mediacodec.Packet
import com.android.mediacodec.VideoDecoder.AsyncDataListener
import com.android.aegentcam.webrtcscreenshare.repository.MainRepository
import com.android.aegentcam.webrtcscreenshare.service.WebrtcService
import com.android.aegentcam.webrtcscreenshare.service.WebrtcServiceRepository
import com.linkflow.blackboxsdk.NeckbandRestApiClient
import com.linkflow.blackboxsdk.rtsp.RTSPStreamManager
import org.webrtc.MediaStream
import java.security.SecureRandom
import javax.inject.Inject

class LiveStreamActivity : BaseStreamActivity(), SurfaceHolder.Callback, MainRepository.Listener{

    lateinit var binding: ActivityLiveStreamBinding
    private var mStreamManager: RTSPStreamManager? = null

    @Inject
    lateinit var webrtcServiceRepository: WebrtcServiceRepository
    private val capturePermissionRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLiveStreamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppController.appComponent!!.inject(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
        initClickView()
        initScreenSharing()
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

        binding.rltStartLive.setSafeOnClickListener {
            startScreenCapture()
        }

    }

    private fun initScreenSharing() {
        WebrtcService.surfaceView = binding.surfaceView
        WebrtcService.listener = this
        webrtcServiceRepository.startIntent(generateSecureRandomString(6))
    }

    fun generateSecureRandomString(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val random = SecureRandom()
        return (1..length)
            .map { allowedChars[random.nextInt(allowedChars.length)] }
            .joinToString("")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            recreate()
        }
        else if (requestCode == capturePermissionRequestCode) {
            WebrtcService.screenPermissionIntent = data
            webrtcServiceRepository.requestConnection(sessionManager.liveStreamUrl)
        }
    }

    private fun startScreenCapture(){
        val mediaProjectionManager = application.getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(), capturePermissionRequestCode
        )
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
                mRTSPCheckHandler.post(Runnable {
                    binding.rltStartLive.isVisible = true
                    commonMethods.hideProgressDialog() })
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
                Log.e(ContentValues.TAG, "reload")
                mPreviewModel!!.activateRTSP(mBTCommandManager!!.accessToken, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onConnectionRequestReceived(target: String) {
        runOnUiThread{
            binding.apply {

            }
        }
    }

    override fun onConnectionConnected() {
        runOnUiThread {
            binding.apply {
                rltStartLive.isVisible = false
                rltStopLive.isVisible = true
                rltStopLive.setOnClickListener {
                    isServiceDestroy = false
                    webrtcServiceRepository.endCallIntent()
                    restartUi()
                }
            }
        }
    }

    override fun onCallEndReceived() {
        runOnUiThread {
            isServiceDestroy = true
            restartUi()
        }
    }

    override fun onRemoteStreamAdded(stream: MediaStream) {
        runOnUiThread {
            /*binding.surfaceView.isVisible = true
            stream.videoTracks[0].addSink(views.surfaceView)*/
        }
    }

    private fun restartUi(){
        binding.apply {
            rltStopLive.isVisible=false
            rltStartLive.isVisible = true
        }
    }
}