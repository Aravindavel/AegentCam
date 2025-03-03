package com.android.aegentcam.helper

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.android.aegentcam.R
import com.android.aegentcam.view.fragment.BaseDialogFragment

class WifiP2pConnectingDialog : BaseDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInnerView(R.layout.dialog_loding_progress, "dialog_wifi_p2p_connecting")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.setCanceledOnTouchOutside(false)
        setCancelable(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSingleButton(SINGLE_MODE_AGREE, R.string.cancel)

        val messageTv = view.findViewById<TextView>(R.id.message)
        messageTv.setText(R.string.wifi_connecting_1)
    }

    fun doShow(manager: FragmentManager?) {
        if (!isAdded()) {
            Log.e("baseDialogFragment", "show")
            show(manager!!)
        }
    }
}
