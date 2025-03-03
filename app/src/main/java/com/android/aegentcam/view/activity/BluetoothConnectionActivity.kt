package com.android.aegentcam.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.aegentcam.R
import com.android.aegentcam.configs.AppController
import com.android.aegentcam.databinding.ActivityBluetoothConnectionBinding
import com.android.aegentcam.helper.BTItem
import com.android.aegentcam.helper.gone
import com.android.aegentcam.helper.visible
import com.android.aegentcam.network.NetworkResult
import com.android.aegentcam.view.adapter.BTDeviceRecyclerAdapter
import com.linkflow.blackboxsdk.helper.BTConnectHelper
import com.linkflow.blackboxsdk.helper.WifiConnectHelper
import com.linkflow.blackboxsdk.manager.BTCommandManager.STEP
import com.linkflow.blackboxsdk.permission.PermissionHelper

class BluetoothConnectionActivity : BaseActivity(), BTDeviceRecyclerAdapter.ItemClickListener {

    lateinit var binding: ActivityBluetoothConnectionBinding
    private var mBTConnectHelper: BTConnectHelper? = null
    lateinit var mAdapter : BTDeviceRecyclerAdapter
    private var mBeforeConnectedAddress = ""
    private var mCurrentConnectingAddress = ""
    private var backPressed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()
        initRecyclerView()
    }


    @SuppressLint("MissingPermission")
    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.connect_device)
        binding.header.ivBack.visible()
        binding.header.ivBack.setOnClickListener {
            onBackPressed()
        }
        mBTConnectHelper = mBTCommandManager!!.connectHelper
        mBTConnectHelper!!.addListener(mBTListener)
        binding.btnStarted.setSafeOnClickListener {
            sessionManager.bluetoothDeviceAddress = mBeforeConnectedAddress // Replace with FITT360's MAC address
           startActivity(Intent(this@BluetoothConnectionActivity, MainActivity::class.java))
        }

    }


    private fun initRecyclerView() {
        binding.rvBluetoothList.setItemAnimator(null)
        val manager = LinearLayoutManager(this)
        mAdapter = BTDeviceRecyclerAdapter(this, this)
        binding.rvBluetoothList.setLayoutManager(manager)
        binding.rvBluetoothList.setAdapter(mAdapter)

        for (device in mBTConnectHelper!!.bondedBTDevices) {
            mAdapter.addItem(device, BTItem.STATE_NONE)
        }
    }

    override fun onResume() {
        super.onResume()
        WifiConnectHelper.getInstance(AppController.contexts)
            .setListener(null)
        mBTConnectHelper!!.registerReceiver(applicationContext)
        mBTConnectHelper!!.startDiscovery(-1, true)
    }

    override fun onDestroy() {
        mBTConnectHelper!!.removeListener(mBTListener)
        mBTConnectHelper!!.stopDiscovery()
        super.onDestroy()
    }

    override fun onConnectState(step: STEP?) {
        super.onConnectState(step)
        when (step) {
            STEP.STEP_BT_FINDING -> {}
            STEP.STEP_BT -> if (mBTConnectHelper!!.connectedDevice != null) {
                mAdapter.addItem(mBTConnectHelper!!.connectedDevice, BTItem.STATE_CONNECTING)
                mAdapter.changedState(
                    mAdapter.getCorrectDevicePosition(mCurrentConnectingAddress),
                    BTItem.STATE_CONNECTING
                )
            }

            STEP.STEP_DONE -> {
                WifiConnectHelper.getInstance(AppController.contexts)
                    .disconnect(true, -1, null)
                mBTConnectHelper!!.stopDiscovery()
                if (mBTConnectHelper!!.connectedDevice != null) {
                    mAdapter.addItem(mBTConnectHelper!!.connectedDevice, BTItem.STATE_CONNECTED)
                    mBeforeConnectedAddress = mBTConnectHelper!!.connectedDevice.address
                    mAdapter.changedState(
                        mAdapter.getCorrectDevicePosition(mBeforeConnectedAddress),
                        BTItem.STATE_CONNECTED
                    )
                    binding.btnStarted.visible()
                }
            }

            STEP.STEP_NONE -> mAdapter.changedState(
                mAdapter.getCorrectDevicePosition(
                    mBeforeConnectedAddress
                ), BTItem.STATE_NONE
            )
            else ->{}
        }

        if (mBTCommandManager!!.isTryToConnecting) {
            if (mBeforeConnectedAddress == "" && mBTConnectHelper!!.targetDevice != null) {
                mBeforeConnectedAddress = mBTConnectHelper!!.targetDevice.address
            }
        }
    }


    private val mBTListener: BTConnectHelper.Listener = object : BTConnectHelper.Listener {
        override fun foundBTDevice(
            device: BluetoothDevice,
            isCorrectDevice: Boolean,
            bondState: Int
        ) {
            mAdapter.addItem(device, BTItem.STATE_NONE)
        }

        override fun changedBondedState(device: BluetoothDevice, state: Int) {
            if (state == BluetoothDevice.BOND_BONDED) {
                mBTConnectHelper!!.startDiscovery(-1, true)
                mAdapter.changedState(
                    mAdapter.getCorrectDevicePosition(device.address),
                    BTItem.STATE_CONNECTING
                )

                mAdapter.changedBonded(device.address)
                mAdapter.notifyDataSetChanged()

                mBTCommandManager!!.connect(device)
            } else if (state == BluetoothDevice.BOND_NONE) {
                Toast.makeText(
                    this@BluetoothConnectionActivity,
                    R.string.bt_pairing_request_cancel,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        override fun discoveryFinished(
            device: BluetoothDevice?,
            foundDevice: Boolean,
            isBonded: Boolean
        ) {

        }


        override fun discoveryTryCnt(tryCnt: Int, maxTryCnt: Int, foundTargetDevice: Boolean) {
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        getIntent()
    }

    override fun onSuccess(networkResult: NetworkResult<Any>) {
        
    }

    override fun onFailure(networkResult: NetworkResult<Any>) {
        
    }

    override fun onLoading(networkResult: NetworkResult<Any>) {
        
    }

    @SuppressLint("MissingPermission")
    override fun clickedItem(position: Int) {
        val item = mAdapter.getItem(position)
        if (item != null) {
            binding.btnStarted.gone()

            mCurrentConnectingAddress = item.mDevice.address
            mBTConnectHelper!!.stopDiscovery()

            val device = mAdapter.getItem(position).mDevice
            if (mBTConnectHelper!!.isBondedDevice(device)) {
                mAdapter.changedState(position, BTItem.STATE_CONNECTING)
                mBTCommandManager!!.connect(device)
            } else {
                Toast.makeText(this, R.string.bt_pairing_request, Toast.LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (PermissionHelper.getInstance()
                            .isConfirmPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    ) {
                        device.createBond()
                    }
                } else {
                    device.createBond()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (backPressed >= 1) {
            finishAffinity()
            super.onBackPressed()
        } else {
            backPressed += 1
            commonMethods.showToast(this, getString(R.string.press_back_again))
        }
    }
}