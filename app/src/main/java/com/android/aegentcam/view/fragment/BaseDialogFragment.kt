package com.android.aegentcam.view.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.android.aegentcam.R
import java.util.Objects

/**
 * Created by choiseokwon on 2018. 3. 27..
 */
abstract class BaseDialogFragment : DialogFragment() {
    protected var mInnerViewId: Int = 0
    protected var mBtnContainer: RelativeLayout? = null
    protected var mDisagreeBtn: TextView? = null
    protected var mAgreeBtn: TextView? = null
    protected var mClickListener: View.OnClickListener? = null
    protected var mMessage: String? = null
    protected var mTag: String? = null
    private var mDisableButton = false
    private var mSingleMode = -1
    private var mBeforeAddTime: Long = 0

    protected var mListener: DialogBtnClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_base_dialog, container, false)
        val innerView: RelativeLayout =
            view.findViewById<RelativeLayout>(R.id.base_dialog_inner_view)
        innerView.addView(inflater.inflate(mInnerViewId, container, false))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Objects.requireNonNull<Dialog?>(dialog).window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBtnContainer = view.findViewById<RelativeLayout>(R.id.btn_container)
        mDisagreeBtn = view.findViewById<TextView>(R.id.base_dialog_disagree)
        mAgreeBtn = view.findViewById<TextView>(R.id.base_dialog_agree)
        if (mClickListener != null) {
            mDisagreeBtn!!.setOnClickListener(mClickListener)
            mAgreeBtn!!.setOnClickListener(mClickListener)
        }
    }

    fun setClickListener(listener: View.OnClickListener?) {
        mClickListener = listener
    }

    override fun onResume() {
        super.onResume()
        if (mDisableButton) {
            mBtnContainer!!.setVisibility(View.GONE)
        } else if (mSingleMode != -1) {
            mBtnContainer!!.setVisibility(View.VISIBLE)
            if (mSingleMode == SINGLE_MODE_DISAGREE) {
                mAgreeBtn!!.visibility = View.GONE
            } else if (mSingleMode == SINGLE_MODE_AGREE) {
                mDisagreeBtn!!.visibility = View.GONE
            } else if (mSingleMode == MULTI_MODE) {
                mAgreeBtn!!.visibility = View.VISIBLE
                mDisagreeBtn!!.visibility = View.VISIBLE
            }
            mSingleMode = -1
        }
    }

    protected fun setSingleButton(singleMode: Int, text: Int) {
        mSingleMode = singleMode
        if (text != -1) {
            mAgreeBtn!!.setText(text)
        }
    }

    protected fun setMultiButton() {
        mSingleMode = MULTI_MODE
    }

    fun disableButton() {
        mDisableButton = true
    }

    fun setDisagreeText(text: Int) {
        mDisagreeBtn!!.setText(text)
    }

    fun setAgreeText(text: Int) {
        mAgreeBtn!!.setText(text)
    }

    fun setInnerView(viewId: Int, tag: String?) {
        mInnerViewId = viewId
        mTag = tag
    }

    fun setListener(listener: DialogBtnClickListener?) {
        mListener = listener
    }

    fun showWithMessage(fm: FragmentManager, message: String?) {
        if (!isAdded && System.currentTimeMillis() - mBeforeAddTime > MAX_ADDED_DELAY) {
            mBeforeAddTime = System.currentTimeMillis()
            mMessage = message
            val ft = fm.beginTransaction()
            val fragment = fm.findFragmentByTag(mTag)
            if (fragment != null) {
                ft.remove(fragment)
                ft.addToBackStack(null)
            }
            ft.add(this, mTag)
            ft.commitAllowingStateLoss()
        }
    }

    fun show(fm: FragmentManager) {
        if (!isAdded && System.currentTimeMillis() - mBeforeAddTime > MAX_ADDED_DELAY) {
            mBeforeAddTime = System.currentTimeMillis()
            val ft = fm.beginTransaction()
            val fragment = fm.findFragmentByTag(mTag)
            if (fragment != null) {
                ft.remove(fragment)
                ft.addToBackStack(null)
            }
            ft.add(this, mTag)
            ft.commitAllowingStateLoss()
        }
    }

    override fun dismiss() {
        mMessage = null
        if (isAdded) {
            try {
                super.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun dismissAllowingStateLoss() {
        mMessage = null
        if (isAdded) {
            try {
                super.dismissAllowingStateLoss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    interface DialogBtnClickListener {
        fun clicked(action: Int)
    }

    companion object {
        private const val MAX_ADDED_DELAY = 1000
        const val CLICKED_TYPE_DISAGREE: Int = 0
        const val CLICKED_TYPE_AGREE: Int = 1
        protected const val SINGLE_MODE_DISAGREE: Int = 10
        val SINGLE_MODE_AGREE = 11
        protected const val MULTI_MODE: Int = 12
    }
}
