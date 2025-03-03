package com.android.aegentcam.view.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.aegentcam.R
import com.android.aegentcam.configs.AppController
import com.android.aegentcam.helper.BTItem
import com.android.aegentcam.helper.BaseBTItem
import com.android.aegentcam.helper.CommonMethods
import com.android.aegentcam.helper.SessionManager
import javax.inject.Inject


class BTDeviceRecyclerAdapter(
    private val mContext: Context,
    private val mListener: ItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mItems: ArrayList<BaseBTItem> = ArrayList<BaseBTItem>()

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    init {
        AppController.appComponent!!.inject(this)
    }

    fun getItem(position: Int): BTItem {
        return mItems[position] as BTItem
    }

    fun getCorrectDevicePosition(address: String?): Int {
        if (address != null) {
            val size = mItems.size
            for (i in 0 until size) {
                if (mItems[i].mViewType === BaseBTItem.VIEW_TYPE_DEVICE) {
                    if ((mItems[i] as BTItem).mDevice.getAddress().equals(address)) {
                        return i
                    }
                }
            }
        }
        return -1
    }

    fun addItem(device: BluetoothDevice?, state: Int): Boolean {
        var noDevice = true
        if (device != null) {
            var btItem: BTItem
            val size = mItems.size
            for (i in 0 until size) {
                btItem = mItems[i] as BTItem
                if (btItem.mDevice.getAddress().equals(device.address)) {
                    noDevice = false
                    notifyItemChanged(i)
                    break
                }
            }
            if (noDevice) {
                mItems.add(BTItem(device, state))
                notifyItemInserted(mItems.size - 1)
            }
        }
        return noDevice
    }

    fun changedBonded(address: String?) {
        val size = mItems.size
        for (i in 0 until size) {
            if ((mItems[i] as BTItem).mDevice.getAddress().equals(address)) {
                val item: BTItem = mItems[i] as BTItem
                mItems.removeAt(i)
                mItems.add(i, item)
                changedState(i, BTItem.STATE_CONNECTING)
                break
            }
        }
    }

    fun changedState(position: Int, connectState: Int) {
        if (position > -1) {
            (mItems[position] as BTItem).state = connectState
            notifyItemChanged(position)
        }
    }

    private val connectingDevicePosition: Int
        get() {
            for (i in mItems.indices) {
                if ((mItems[i] as BTItem).state !== BTItem.STATE_NONE) {
                    return i
                }
            }
            return -1
        }

    fun clear(isConnecting: Boolean) {
        var item: BTItem? = null
        if (isConnecting) {
            val connectingDevicePosition = connectingDevicePosition
            if (connectingDevicePosition != -1) {
                item = mItems[connectingDevicePosition] as BTItem
            }
        }
        notifyItemRangeRemoved(0, itemCount)
        mItems.clear()
        if (item != null) {
            mItems.add(0, item)
            notifyItemInserted(0)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        return BTDeviceHolder(
            LayoutInflater.from(mContext).inflate(R.layout.holder_bt_device, viewGroup, false),
            mListener
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BTDeviceHolder) {
            val item: BTItem = mItems[position] as BTItem
            holder.setData(item,mContext,sessionManager)
        }
    }


    override fun getItemCount(): Int {
        return mItems.size
    }

    private class BTDeviceHolder(itemView: View, private val mListener: ItemClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val mContainer: RelativeLayout =
            itemView.findViewById<RelativeLayout>(R.id.container)
        private val mConnectContainer: LinearLayout
        private val mProgressBar: ProgressBar
        private val mNameTv: TextView
        private val mStateTv: TextView
        private val mState = intArrayOf(R.string.connecting_2, R.string.connected_1)

        init {
            mContainer.setOnClickListener(this)
            mConnectContainer = itemView.findViewById<LinearLayout>(R.id.connect_state)
            mProgressBar = itemView.findViewById<ProgressBar>(R.id.progress)

            mStateTv = itemView.findViewById<TextView>(R.id.state)
            mNameTv = itemView.findViewById<TextView>(R.id.name)
        }

        @SuppressLint("MissingPermission")
        fun setData(item: BTItem,context: Context,sessionManager: SessionManager) {
            mNameTv.text = item.mDevice.name
            if (item.state != BTItem.STATE_NONE) {
                mConnectContainer.visibility = View.VISIBLE
                mStateTv.setText(mState[item.state])
                if (item.state == BTItem.STATE_CONNECTED) {
                    sessionManager.bluetoothDeviceName = item.mDevice.name
                    sessionManager
                    mProgressBar.visibility = View.GONE
                    mStateTv.setTextColor(ContextCompat.getColor(context, R.color.txt_active))
                } else {
                    mStateTv.setTextColor(ContextCompat.getColor(context, R.color.error_red))
                    mProgressBar.visibility = View.VISIBLE
                }
            } else {
                mConnectContainer.visibility = View.GONE
            }
        }

        override fun onClick(v: View) {
            if (v.id == R.id.container) {
                mListener.clickedItem(layoutPosition)
            }
        }
    }

    interface ItemClickListener {
        fun clickedItem(position: Int)
    }
}