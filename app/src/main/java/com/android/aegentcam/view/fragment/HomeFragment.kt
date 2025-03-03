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
                                        R.string.video_recording_stop
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

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(sessionManager.bluetoothDeviceAddress)
        val deviceName = bluetoothDevice.name
        val deviceAddressInfo = bluetoothDevice.address
        val bondState = bluetoothDevice.bondState  // e.g., BOND_BONDED, BOND_NONE, etc.
        val deviceType = bluetoothDevice.type      // e.g., DEVICE_TYPE_CLASSIC, DEVICE_TYPE_LE, etc.

        Log.d("Bluetooth", "Name: $deviceName")
        Log.d("Bluetooth", "Address: $deviceAddressInfo")
        Log.d("Bluetooth", "Bond State: $bondState")
        Log.d("Bluetooth", "Device Type: $deviceType")
        bluetoothDevice.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("GattCallback", "Connected to FITT360. Discovering services...")
                gatt.discoverServices()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Use explicit UUIDs for the Battery Service
                for (i in gatt.services.indices) {
                    val batteryServiceUUID = gatt.services[i].uuid
                    for (j in gatt.services[i].characteristics.indices) {
                        val batteryCharacteristicUUID = gatt.services[i].characteristics[j].uuid
                        val batteryService = gatt.getService(batteryServiceUUID)

                        if (batteryService == null) {
                            Log.e("GattCallback", "Battery service not found!")
                        } else {
                            val batteryCharacteristic =
                                batteryService.getCharacteristic(batteryCharacteristicUUID)
                            if (batteryCharacteristic == null) {
                                Log.e("GattCallback", "Battery characteristic not found!")
                            } else {
                                // Ensure the characteristic is readable
                                if (batteryCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
                                    val initiated = gatt.readCharacteristic(batteryCharacteristic)
                                    Log.d("GattCallback", "Initiated read: $initiated")
                                    if (initiated) {
                                        batteryUDID = gatt.services[i].characteristics[j].uuid.toString()
                                    }
                                } else {
                                    Log.e("GattCallback", "Battery characteristic is not readable!")
                                }
                            }
                        }

                    }
                }
            } else {
                Log.e("GattCallback", "Service discovery failed with status: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid) {
                    UUID.fromString(batteryUDID) -> {
                        val batteryLevel = characteristic.value[0].toInt()
                        requireActivity().runOnUiThread {
                            binding.tvBatteryPercentage.text = "$batteryLevel%"
                        }
                        Log.d("GattCallback", "Battery Level: $batteryLevel%")
                    }

                    // Add additional cases for other characteristics if needed.
                    else -> {
                        Log.d("GattCallback", "Characteristic read: ${characteristic.uuid}")
                    }
                }
            } else {
                Log.e("GattCallback", "Characteristic read failed with status: $status")
            }
        }
    }


}