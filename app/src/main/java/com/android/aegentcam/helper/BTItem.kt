package com.android.aegentcam.helper

import android.bluetooth.BluetoothDevice

class BTItem(var mDevice: BluetoothDevice, var state: Int) : BaseBTItem(VIEW_TYPE_DEVICE) {
    var mChecked: Boolean = false

    fun setChecked(checked: Boolean) {
        mChecked = checked
    }

    companion object {
        const val STATE_NONE: Int = -1
        const val STATE_CONNECTING: Int = 0
        const val STATE_CONNECTED: Int = 1
    }
}
