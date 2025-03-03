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

    override fun onSuccess(networkResult: NetworkResult<Any>) {
        
    }

    override fun onFailure(networkResult: NetworkResult<Any>) {
        
    }

    override fun onLoading(networkResult: NetworkResult<Any>) {
        
    }


}