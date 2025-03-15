package com.android.aegentcam.view.fragment

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.android.aegentcam.R
import com.android.aegentcam.configs.AppController
import com.android.aegentcam.databinding.FragmentHomeBinding
import com.android.aegentcam.helper.DepthPageTransformer
import com.android.aegentcam.network.NetworkResult
import com.android.aegentcam.view.activity.CameraSettingActivity
import com.android.aegentcam.view.activity.MainActivity
import com.android.aegentcam.view.activity.PreviewActivity
import com.android.aegentcam.view.adapter.ImageSliderAdapter
import com.linkflow.blackboxsdk.manager.BTCommandManager.CommandCallback
import java.util.UUID


class HomeFragment : BaseFragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var viewPager: ViewPager

    var batteryUDID = ""
    var storageUDID = ""

    val MSG_TAKE_A_PHOTO = 10
    val MSG_CHANGE_RECORD_STATE = 11
    var mIsRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(layoutInflater)
        AppController.appComponent!!.inject(this)
        initViews()
    }

    private fun initViews() {
        binding.tvModelName.text = sessionManager.bluetoothDeviceName
        binding.rltCapture.setSafeOnClickListener {
            if ((requireActivity() as? MainActivity)!!.mBTCommandManager!!.isConnected) {
                mMainHandler.removeMessages(MSG_TAKE_A_PHOTO)
                mMainHandler.sendEmptyMessageDelayed(MSG_TAKE_A_PHOTO, 2000)
            }
        }

        binding.rltRecord.setSafeOnClickListener {
            if ((requireActivity() as? MainActivity)!!.mBTCommandManager!!.isConnected) {
                mMainHandler.removeMessages(MSG_CHANGE_RECORD_STATE)
                if (mIsRecording) {
                    //if does not enough to make video file, will be ignore stop recording, so we need some delay.
                    mMainHandler.sendEmptyMessageDelayed(MSG_CHANGE_RECORD_STATE, 2000)
                } else {
                    mMainHandler.sendEmptyMessage(MSG_CHANGE_RECORD_STATE)
                }
            }
        }

        binding.cvPreview.setSafeOnClickListener {
            val intent = Intent(requireContext(), PreviewActivity::class.java)
            val animation = ActivityOptions.makeCustomAnimation(
                requireContext(),
                R.anim.ub__slide_in_right,
                R.anim.ub__slide_out_left
            ).toBundle()
            startActivity(intent, animation)
        }

    }

    override fun onResume() {
        super.onResume()
        binding.tvBatteryPercentage.text = "${(context as MainActivity).mBTCommandManager!!.statusItem.mBatteryLevel.toString()}%"
        val usedStorageMB = (context as MainActivity).mBTCommandManager!!.statusItem.mInternalStorage[0].toLong()
        val totalStorageMB = (context as MainActivity).mBTCommandManager!!.statusItem.mInternalStorage[1].toLong()

        val storageUsagePercentage = calculateStorageUsagePercentage(usedStorageMB, totalStorageMB)

        binding.tvMemory.text = storageUsagePercentage
    }

    fun calculateStorageUsagePercentage(usedMB: Long, totalMB: Long): String {
        if (totalMB == 0L) return "N/A" // Avoid division by zero
        val usagePercentage = (usedMB.toDouble() / totalMB.toDouble()) * 100
        return String.format("%.1f%%", usagePercentage)
    }

    private val mMainHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == MSG_TAKE_A_PHOTO) {
                if ((requireActivity() as? MainActivity)!!.mBTCommandManager!!.isConnected) {
                    (requireActivity() as? MainActivity)!!.mBTCommandManager!!.commandTakeAPhoto(CommandCallback { success, jsonObject ->
                        Toast.makeText(
                            requireContext(),
                            if (success) R.string.photo_captured else R.string.photo_capture_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }
            } else if (msg.what == MSG_CHANGE_RECORD_STATE) {
                if ((requireActivity() as? MainActivity)!!.mBTCommandManager!!.isConnected()) {
                    mIsRecording = !mIsRecording
                    (requireActivity() as? MainActivity)!!.mBTCommandManager!!.commandSetRecordState(mIsRecording,
                        CommandCallback { success, jsonObject ->
                            Toast.makeText(
                                requireContext(),
                                if (success) {
                                    if (mIsRecording)
                                        R.string.video_recording_start
                                    else
                                        R.string.video_recording_stop
                                }else
                                    R.string.video_recording_failed,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //initImageSlider()
        return binding.root
    }

    override fun onSuccess(networkResult: NetworkResult<Any>) {

    }

    override fun onFailure(networkResult: NetworkResult<Any>) {

    }

    override fun onLoading(networkResult: NetworkResult<Any>) {

    }

    private fun initImageSlider() {
        viewPager = binding.viewPager
        val images = listOf(
            "https://aegenttech.com/wp-content/uploads/2024/04/img2.png",
            "https://aegenttech.com/wp-content/uploads/2024/07/slide2.jpg",
            "https://aegenttech.com/wp-content/uploads/2024/07/slide4.jpg",
            "https://aegenttech.com/wp-content/uploads/2024/07/slide5.jpg",
            "https://aegenttech.com/wp-content/uploads/2024/07/slide6.jpg",
            "https://aegenttech.com/wp-content/uploads/2024/07/bg7.jpg",
        )

        val sliderAdapter = ImageSliderAdapter(requireContext(), images)
        viewPager.adapter = sliderAdapter
        viewPager.setPageTransformer(true,DepthPageTransformer())
        sliderAdapter.notifyDataSetChanged()
        binding.dotIndicator.setViewPager(viewPager)
    }

}