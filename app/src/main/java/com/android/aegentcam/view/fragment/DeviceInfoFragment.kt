package com.android.aegentcam.view.fragment

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.aegentcam.R
import com.android.aegentcam.databinding.FragmentDeviceInfoBinding
import com.android.aegentcam.helper.visible
import com.android.aegentcam.network.NetworkResult
import com.android.aegentcam.view.activity.MainActivity
import java.util.UUID


class DeviceInfoFragment : BaseFragment() {

    lateinit var binding: FragmentDeviceInfoBinding
    var batteryUDID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentDeviceInfoBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.device_information)
        binding.header.ivBack.visible()
        binding.header.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.tvDeviceName.text = sessionManager.bluetoothDeviceName
        binding.tvFirmware.text = (context as MainActivity).mBTCommandManager!!.softwareItem.mVersion.toString()
        binding.tvReleaseDate.text = (context as MainActivity).mBTCommandManager!!.softwareItem.mReleaseDate
        binding.tvBatteryPercentage.text = "${(context as MainActivity).mBTCommandManager!!.statusItem.mBatteryLevel.toString()}%"
        val usedStorageMB = (context as MainActivity).mBTCommandManager!!.statusItem.mInternalStorage[0].toLong()
        val totalStorageMB = (context as MainActivity).mBTCommandManager!!.statusItem.mInternalStorage[1].toLong()

        val usedStorageFormatted = formatMBtoGB(usedStorageMB)
        val totalStorageFormatted = formatMBtoGB(totalStorageMB)

        binding.tvInternalStorage.text = "$usedStorageFormatted / $totalStorageFormatted"

    }

    fun formatMBtoGB(megabytes: Long): String {
        val gb = megabytes.toDouble() / 1000.0
        return String.format("%.1f GB", gb)
    }


    override fun onSuccess(networkResult: NetworkResult<Any>) {
        
    }

    override fun onFailure(networkResult: NetworkResult<Any>) {
        
    }

    override fun onLoading(networkResult: NetworkResult<Any>) {
        
    }


}