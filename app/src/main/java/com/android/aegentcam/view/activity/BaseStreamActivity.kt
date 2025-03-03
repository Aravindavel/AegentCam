package com.android.aegentcam.view.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.aegentcam.R
import com.android.aegentcam.configs.AppController
import com.android.aegentcam.helper.WifiEnableDialog
import com.android.aegentcam.helper.WifiP2pConnectingDialog
import com.android.aegentcam.network.NetworkResult
import com.linkflow.blackboxsdk.NeckbandRestApiClient
import com.linkflow.blackboxsdk.helper.WifiConnectHelper
import com.linkflow.blackboxsdk.manager.BTCommandManager.STEP
import com.linkflow.blackboxsdk.model.PreviewModel

open class BaseStreamActivity : BaseActivity(), WifiConnectHelper.ConnectListener {
     var mWifiP2pConnectingDialog: WifiP2pConnectingDialog? = null
     var mWifiEnableDialog: WifiEnableDialog? = null
     var mWifiHelper: WifiConnectHelper? = null

     var mPreviewModel: PreviewModel? = null

     var mIsMuted: Boolean = false
     var mExitingRTSP: Boolean = false

     val mRTSPCheckHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RTSP_STARTED_CHECK) {
                reload()
                sendEmptyMessageDelayed(RTSP_STARTED_CHECK, 8000)
            } else if (msg.what == RTSP_RELEASED) {
                release()
            } else if (msg.what == RTSP_RELOAD) {
                removeMessages(RTSP_RELOAD)
                reload()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPreviewModel = PreviewModel(null)
        mWifiHelper = WifiConnectHelper.getInstance(AppController.globalApplicationContext)
        mWifiHelper!!.registerReceiver(AppController.globalApplicationContext)

        initDialog()

        val filter = IntentFilter(ACTION_CHANGED_WIFI_P2P_STATUS)
        filter.addAction(ACTION_CHANGE_WIFI_P2P_STATUS)
        filter.addAction(ACTION_UNPAIRED_DEVICE)
        LocalBroadcastManager.getInstance(this).registerReceiver(wifiP2pChangedReceiver, filter)
    }

    private fun requestToChangeWifiP2pStatus() {
        mBTCommandManager!!.commandSetWifiStatusAsDelay(
            true, MAX_WIFI_TIMEOUT
        ) { success, response ->
            Log.e(
                TAG,
                "request to change wifi p2p status - $success"
            )
            if (success) {
                if (response.has("result")) {
                    AppController.globalApplicationContext!!.setRequestWifiChangedFrom(AppController.REQUEST_WIFI_CHANGED_FROM_PREVIEW)
                    val result = response.getJSONObject("result")
                    val enabledAlready = result.getBoolean("wifi")
                    if (enabledAlready && result.has("ssid")) {
                        val ssid = result.getString("ssid")
                        changeWifiP2pStatus(applicationContext, true, ssid)
                    } else {
                        Handler()
                            .postDelayed({ requestToChangeWifiP2pStatus() }, 1000)
                    }
                }
            }
        }
    }

    @Synchronized
    open fun releaseWait(forceClose: Boolean) {
        mExitingRTSP = true
        mRTSPCheckHandler.removeMessages(RTSP_STARTED_CHECK)
        mRTSPCheckHandler.removeMessages(RTSP_RELOAD)
        if (forceClose) {
            try {
                Log.e(TAG, "release wait")
                mPreviewModel!!.activateRTSP(mBTCommandManager!!.accessToken, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mRTSPCheckHandler.sendEmptyMessageDelayed(RTSP_RELEASED, 2000)
        }
    }

    @Synchronized
    open fun release() {
    }

     open fun reload() {
        if (mWifiHelper!!.isConnected) {
            try {
                Log.e(TAG, "reload")
                mPreviewModel!!.activateRTSP(mBTCommandManager!!.accessToken, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onConnectState(step: STEP?) {
        super.onConnectState(step)
        if (step == STEP.STEP_DONE) {
            Log.e("preview", "on connect state - " + mWifiHelper!!.isConnected)
            if (!mWifiHelper!!.isConnected) {
                requestToChangeWifiP2pStatus()
            } else {
                AppController.globalApplicationContext!!
                    .setRequestWifiChangedFrom(AppController.REQUEST_WIFI_CHANGED_FROM_PREVIEW)
                try {
                    mPreviewModel!!.setMuteState(mBTCommandManager!!.accessToken, false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mRTSPCheckHandler.removeMessages(RTSP_STARTED_CHECK)
                mRTSPCheckHandler.sendEmptyMessageDelayed(RTSP_STARTED_CHECK, 3000)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppController.globalApplicationContext!!.setCurrentActivity(this)
        WifiConnectHelper.getInstance(AppController.globalApplicationContext!!).setListener(
            this
        )
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(wifiP2pChangedReceiver)
        super.onDestroy()
    }

    override fun onSuccess(networkResult: NetworkResult<Any>) {

    }

    override fun onFailure(networkResult: NetworkResult<Any>) {

    }

    override fun onLoading(networkResult: NetworkResult<Any>) {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            releaseWait(true)
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun changedWifiState(state: Int) {
        if (state == WifiManager.WIFI_STATE_ENABLED) {
            mWifiEnableDialog!!.dismissAllowingStateLoss()
            requestToChangeWifiP2pStatus()
        }
    }

    override fun completedConnectP2p(info: WifiP2pInfo) {
        mWifiP2pConnectingDialog!!.dismissAllowingStateLoss()
        NeckbandRestApiClient.setBaseUrl(info.groupOwnerAddress.hostAddress)
        mRTSPCheckHandler.postDelayed({
            try {
                Log.e(TAG, "completed connect p2p")
                mPreviewModel!!.activateRTSP(mBTCommandManager!!.accessToken, true)
                mRTSPCheckHandler.removeMessages(RTSP_STARTED_CHECK)
                mRTSPCheckHandler.sendEmptyMessageDelayed(
                    RTSP_STARTED_CHECK,
                    6000
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 2000)
    }

    override fun discoveryP2pTryCnt(i: Int, i1: Int) {
    }

    override fun disconnectP2p(
        forceClose: Boolean,
        p2pState: Int,
        info: WifiP2pInfo,
        targetWifiP2pName: String
    ) {
        if (!forceClose) {
            requestToChangeWifiP2pStatus()
        }
    }

     fun disconnectWifiP2p(context: Context?) {
        mWifiHelper!!.unregisterReceiver(context)
        mWifiHelper!!.stopDiscovery(null)
        mWifiHelper!!.disconnect(true, -1, null)
    }

    fun changeWifiP2pStatus(context: Context?, wifiP2pEnabled: Boolean, wifiP2pSSID: String?) {
        if (wifiP2pEnabled) {
            if (!mWifiHelper!!.isEnabledWifi) {
                mWifiEnableDialog!!.show(supportFragmentManager)
            } else {
                if (!mWifiHelper!!.isConnected && wifiP2pSSID != null) {
                    if (!mWifiP2pConnectingDialog!!.isAdded()) {
                        mWifiP2pConnectingDialog!!.doShow(supportFragmentManager)
                    }
                    mWifiHelper!!.startDiscovery(wifiP2pSSID)
                }
            }
        } else {
            disconnectWifiP2p(AppController.globalApplicationContext!!)
            Toast.makeText(context, R.string.wifi_disconnected_time_out, Toast.LENGTH_SHORT).show()
            releaseWait(true)
        }
    }

    private fun initDialog() {
        mWifiP2pConnectingDialog = WifiP2pConnectingDialog()
        mWifiP2pConnectingDialog!!.setClickListener(View.OnClickListener {
            mWifiP2pConnectingDialog!!.dismissAllowingStateLoss()
            disconnectWifiP2p(AppController.globalApplicationContext!!)
            mRTSPCheckHandler.removeMessages(RTSP_STARTED_CHECK)
            mRTSPCheckHandler.removeMessages(RTSP_RELOAD)
            release()
        })

        mWifiEnableDialog = WifiEnableDialog()
        mWifiEnableDialog!!.setClickListener(View.OnClickListener { view ->
            if (view.id == R.id.base_dialog_agree) {
                mWifiHelper!!.registerReceiver(AppController.globalApplicationContext!!)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivity(Intent(Settings.Panel.ACTION_WIFI))
                } else {
                    mWifiHelper!!.enabledWifi()
                }
            }
            mWifiEnableDialog!!.dismissAllowingStateLoss()
        })
    }

    private val wifiP2pChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (intent.getIntExtra(
                    "from",
                    -1
                ) != AppController.REQUEST_WIFI_CHANGED_FROM_PREVIEW
            ) {
                return
            }
            if (action != null) {
                when (action) {
                    ACTION_CHANGE_WIFI_P2P_STATUS -> requestToChangeWifiP2pStatus()
                    ACTION_CHANGED_WIFI_P2P_STATUS -> {
                        val wifiP2pEnabledAlready = intent.getBooleanExtra("enabled", false)
                        val wifiP2pSSID = intent.getStringExtra("ssid")
                        changeWifiP2pStatus(context, wifiP2pEnabledAlready, wifiP2pSSID)
                    }

                    ACTION_UNPAIRED_DEVICE -> disconnectWifiP2p(AppController.globalApplicationContext!!)
                }
            }
        }
    }

    companion object {
        private val TAG: String = BaseStreamActivity::class.java.simpleName
        val ACTION_CHANGED_WIFI_P2P_STATUS: String = "changed_wifi_p2p_status"
        val ACTION_CHANGE_WIFI_P2P_STATUS: String = "change_wifi_p2p_status"
        val ACTION_UNPAIRED_DEVICE: String = "unpaired_device"
        val MAX_WIFI_TIMEOUT: Int = 1000 * 60 * 15
        val RTSP_STARTED_CHECK: Int = 10
        val RTSP_RELEASED: Int = 11
        val RTSP_RELOAD: Int = 12
    }
}
