package com.android.aegentcam.view.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.android.aegentcam.R
import com.android.aegentcam.configs.AppController
import com.android.aegentcam.helper.CommonMethods
import com.android.aegentcam.helper.SessionManager
import com.android.aegentcam.interfaces.ApiListeners
import com.android.aegentcam.interfaces.ApiService
import com.android.aegentcam.network.NetworkResult
import com.android.aegentcam.viewmodel.CommonViewModel
import com.linkflow.blackboxsdk.manager.BTCommandManager
import com.linkflow.blackboxsdk.manager.BTCommandManager.ConnectStateListener
import com.linkflow.blackboxsdk.manager.BTCommandManager.PARING
import com.linkflow.blackboxsdk.manager.BTCommandManager.STATE
import com.linkflow.blackboxsdk.manager.BTCommandManager.STEP
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), ApiListeners, ConnectStateListener,
    BTCommandManager.Listener {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    var commonViewModel: CommonViewModel? = null
    var mBTCommandManager: BTCommandManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppController.appComponent!!.inject(this)
        setTheme(currentTheme)
        setTheme(currentFont)
        commonViewModel = ViewModelProvider(this).get(CommonViewModel::class.java)

        mBTCommandManager = BTCommandManager.getInstance(applicationContext)
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.colorPrimary)
        }

        commonViewModel?.liveDataResponse?.observe(this, androidx.lifecycle.Observer {
            when (it) {
                is NetworkResult.Success<*> -> {
                    onSuccess(it)
                }
                is NetworkResult.Error<*> -> {
                    onFailure(it)
                }
                is NetworkResult.Loading<*> -> {
                    onLoading(it)
                }
            }
        })

    }

    fun emptyHashMap() = HashMap<String, String>().apply {

    }


    override fun onResume() {
        super.onResume()
        mBTCommandManager!!.setConnectListener(this)
        mBTCommandManager!!.setConnectStateListener(this)
    }

    override fun onConnectState(step: STEP?) {
    }

    override fun alert(alert: BTCommandManager.ALERT?) {
        if (mBTCommandManager!!.connectStateListener != null) {
//            mBTCommandManager!!.connectStateListener.alert(alert)
        }
    }

    override fun connectedBTCommandModule(
        success: Boolean,
        device: BluetoothDevice?,
        token: String?
    ) {
        if (success && device != null) {
            mBTCommandManager!!.commandGetCameraSinglePosition { success, response ->
                if (success) {
                    mBTCommandManager!!.singleCameraPosition =
                        response.getJSONObject("result").getInt("position")
                }
            }
            mBTCommandManager!!.setCommandReceivedListener { type, success, response ->
                if (success) {
                    if (type == BTCommandManager.RECEIVED_TYPE_RECORD) {
                        val isRecording = response.getBoolean("activate")
                        mBTCommandManager!!.enabledRecord(isRecording)
                    } else if (type == BTCommandManager.RECEIVED_TYPE_STORAGE_EVENT) {
                    } else if (type == BTCommandManager.RECEIVED_TYPE_WIFI_STATUS) {
                    }
                }
            }
            if (mBTCommandManager!!.connectStateListener != null) {
                mBTCommandManager!!.connectStateListener.onConnectState(mBTCommandManager!!.currentStep)
            }
        }
    }

    override fun bluetoothState(
        bluetoothDevice: BluetoothDevice?,
        state: STATE?,
        paring: PARING?,
        isRemoteDevice: Boolean
    ) {
        when (paring) {
            PARING.CANCELED -> Toast.makeText(
                this,
                R.string.bt_pairing_request_cancel,
                Toast.LENGTH_LONG
            ).show()

            PARING.BONDED -> {}
            PARING.BONDED_NONE -> {}
            else->{}
        }
        when (state) {
            STATE.STATE_CONNECTING -> if (mBTCommandManager!!.connectStateListener != null) {
                mBTCommandManager!!.connectStateListener.onConnectState(STEP.STEP_BT)
            }

            STATE.STATE_CONNECTED -> {}
            STATE.STATE_DISCONNECTED -> if (mBTCommandManager!!.connectStateListener != null) {
                mBTCommandManager!!.connectStateListener.onConnectState(if (isRemoteDevice) STEP.STEP_BT_FINDING else STEP.STEP_NONE)
            }
            else->{}
        }
    }

    override fun failedRemoteReconnectSoStartDiscovery() {
        Toast.makeText(this, R.string.failed_connect_as_remote, Toast.LENGTH_LONG).show()
    }

    override fun bluetoothDiscovery(cnt: Int, max: Int) {
        if (cnt > 4 && cnt == max - 1) {
            Toast.makeText(this, R.string.bt_not_found, Toast.LENGTH_SHORT).show()
        }
        if (cnt == max) {
            Toast.makeText(this, R.string.failed_find_so_stop_discovery, Toast.LENGTH_LONG).show()
        }
    }

    override fun step(step: STEP?) {
        if (mBTCommandManager!!.connectStateListener != null) {
            mBTCommandManager!!.connectStateListener.onConnectState(step)
        }
    }

    override fun restartAfterLocationEnabled() {
    }


    class SafeClickListener(
        private var defaultInterval: Int = 1000,
        private val onSafeCLick: (View) -> Unit
    ) : View.OnClickListener {
        private var lastTimeClicked: Long = 0
        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
                return
            }
            lastTimeClicked = SystemClock.elapsedRealtime()
            onSafeCLick(v)
        }
    }

    fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }


    companion object {
        var currentTheme = R.style.ThemeAegentCam
        var currentFont = R.style.font_1

        fun isDarkTheme(resources: Resources): Boolean {
            return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }
    }
}