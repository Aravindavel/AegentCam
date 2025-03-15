package com.android.aegentcam.helper

import android.content.SharedPreferences
import com.android.aegentcam.configs.AppController
import javax.inject.Inject


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SessionManager {
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    init {
        AppController.appComponent!!.inject(this)
    }

    var appName: String?
        get() = sharedPreferences!!.getString("appName", "")
        set(appName) = sharedPreferences!!.edit().putString("appName", appName).apply()

    var getStartedShown: String
        get() = sharedPreferences.getString("getStartedShown", "0").toString()
        set(getStartedShown) = sharedPreferences.edit().putString("getStartedShown", getStartedShown).apply()

    var bluetoothDeviceName: String
        get() = sharedPreferences.getString("bluetoothDeviceName", "").toString()
        set(bluetoothDeviceName) = sharedPreferences.edit().putString("bluetoothDeviceName", bluetoothDeviceName).apply()

    var bluetoothDeviceAddress: String
        get() = sharedPreferences.getString("bluetoothDeviceAddress", "").toString()
        set(bluetoothDeviceAddress) = sharedPreferences.edit().putString("bluetoothDeviceAddress", bluetoothDeviceAddress).apply()

    var liveStreamUrl: String
        get() = sharedPreferences.getString("liveStreamUrl", "rtmp://192.168.0.85/live/tony").toString()
        set(liveStreamUrl) = sharedPreferences.edit().putString("liveStreamUrl", liveStreamUrl).apply()


    fun clearAll() {
        sharedPreferences!!.edit().clear().apply()
    }
}