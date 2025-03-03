package com.android.aegentcam.view.fragment

import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.aegentcam.configs.AppController
import com.android.aegentcam.helper.CommonMethods
import com.android.aegentcam.helper.SessionManager
import com.android.aegentcam.interfaces.ApiListeners
import com.android.aegentcam.interfaces.ApiService
import com.android.aegentcam.network.NetworkResult
import com.android.aegentcam.viewmodel.CommonViewModel
import javax.inject.Inject

abstract class BaseFragment : Fragment(), ApiListeners {

    lateinit var commonViewModel: CommonViewModel

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods


    override fun onResume() {
        super.onResume()
        AppController.appComponent!!.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppController.appComponent!!.inject(this)
        commonViewModel = ViewModelProvider(this).get(CommonViewModel::class.java)
        commonViewModel.liveDataResponse.observe(this, androidx.lifecycle.Observer {
            when (it) {
                is NetworkResult.Success -> {
                    onSuccess(it)
                }
                is NetworkResult.Error -> {
                    onFailure(it)
                }
                is NetworkResult.Loading -> {
                    onLoading(it)
                }
            }
        })
    }

    /**
     * Solving multiple clicks problem
     */

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

    fun navigate(id : Int,args : Bundle?=null,navOption : NavOptions?=null){
        findNavController().navigate(id,args,navOption)
    }

    fun emptyHashMap() = HashMap<String, String>().apply {

    }
}