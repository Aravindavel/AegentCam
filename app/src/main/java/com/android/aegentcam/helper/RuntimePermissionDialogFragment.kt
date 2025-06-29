package com.android.aegentcam.helper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.FragmentManager
import androidx.core.content.ContextCompat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.multidex.BuildConfig
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.android.aegentcam.R

import java.util.Objects


/*
 * Created by: umasankar
 * description: this class will handle all runtime permission in single dialog fragment*/
class RuntimePermissionDialogFragment : DialogFragment() {


    //    icon Lists
    val cameraIcon = android.R.drawable.ic_menu_camera
    val locationIcon = android.R.drawable.ic_menu_mylocation
    val storageIcon = android.R.drawable.ic_menu_gallery
    val contactIcon = android.R.drawable.ic_menu_call
    val defaultIcon = cameraIcon

    private var requestCodeForCallbackIdentificationSubDivision: Int = 0


  /*  //butterknife view binds
    @BindView(R.id.imgv_df_permissionImage)
    lateinit var permissionTypeImage: ImageView*/
    lateinit var permissionAllow: Button
    lateinit var permissionNotAllow: Button
    lateinit var tv_permissionDescription: TextView


    private val PERMISSION_REQUEST_CODE = 11
    private var permissionsRequestFor: Array<String>? = null
    private var mContext: Context? = null
    private var callbackListener: RuntimePermissionRequestedCallback? = null
    private var permissionIcon: Int = 0 // default 0
    private var permissionDescription: String? = null


    // this variable is declared to handle the allow Textview onClick process Dynamically,
    // if true -> it will request permission,
    // else open settings page to grand permission by user manually
    protected var ableToRequestPermission = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_permission_common, container, false)
        isCancelable = false
        Objects.requireNonNull(dialog!!.window)?.setBackgroundDrawableResource(android.R.color.transparent)
        ButterKnife.bind(this, rootView)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionAllow = view.findViewById(R.id.button_ok)
        permissionNotAllow = view.findViewById(R.id.button_cancel)
        tv_permissionDescription = view.findViewById(R.id.tv_custom_message)
        setImageResourceAndPermissionDescriptionForPopup()
        permissionNotAllow.setOnClickListener {  afterPermissionDenied() }
        permissionAllow.setOnClickListener {
            if (ableToRequestPermission) {
                requestNecessaryPermissions()
            } else {
                mContext!!.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
                dismiss()
            }
        }
    }

    /*@OnClick(R.id.button_cancel)
    fun notAllowPermission() {

    }

    @OnClick(R.id.button_ok)
    fun allowPermission() {
        if (ableToRequestPermission) {
            requestNecessaryPermissions()
        } else {
            mContext!!.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
            dismiss()
        }
    }
*/
    override fun onDetach() {
        super.onDetach()
        mContext = null
        callbackListener = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (permissions.isNotEmpty() && grantResults.isNotEmpty()) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]

                if (!shouldShowRequestPermissionRationale(permission) && grantResult != PackageManager.PERMISSION_GRANTED) {
                    notAbleToRequestPermission()
                    return
                } else if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    afterPermissionDenied()
                    return
                }
            }
            callbackListener!!.permissionGranted(requestCodeForCallbackIdentifications, requestCodeForCallbackIdentificationSubDivision)
            dismiss()

        } else {
            Toast.makeText(mContext, "permission size 0", Toast.LENGTH_SHORT).show()
        }
    }

    fun setImageResourceAndPermissionDescriptionForPopup() {
        getPermissionRequestedForIconAndDescription()
      //  permissionTypeImage.setImageResource(permissionIcon)
        tv_permissionDescription.text = permissionDescription
        permissionAllow.text= getString(R.string.continue_alert)
        permissionNotAllow.text= getString(R.string.not_now)
    }

    fun getPermissionRequestedForIconAndDescription() {
        when (permissionsRequestFor!![0]) {
            WRITE_EXTERNAL_STORAGE_PERMISSION -> {
                permissionIcon = locationIcon
                permissionDescription = mContext!!.resources.getString(R.string.location_permission_description)+" "+getString(R.string.app_name)+" "+getString(R.string.to_access_location)
            }
            LOCATION_PERMISSION -> {
                permissionIcon = locationIcon
                permissionDescription = mContext!!.resources.getString(R.string.location_permission_description)+" "+getString(R.string.app_name)+" "+getString(R.string.to_access_location)
            }
            else -> {
                permissionIcon = locationIcon
                permissionDescription = mContext!!.resources.getString(R.string.location_permission_description)+" "+getString(R.string.app_name)+" "+getString(R.string.to_access_location)
            }
        }
    }

    private fun requestNecessaryPermissions() {
        ableToRequestPermission = true
        requestPermissions(permissionsRequestFor!!, PERMISSION_REQUEST_CODE)
    }

    private fun notAbleToRequestPermission() {
        permissionAllow.text = mContext!!.resources.getString(R.string.settings)
        ableToRequestPermission = false

    }

    private fun afterPermissionDenied() {
        showPermissionDeniedMessageToUser()
        callbackListener!!.permissionDenied(requestCodeForCallbackIdentifications, requestCodeForCallbackIdentificationSubDivision)
        dismiss()
    }

    fun showPermissionDeniedMessageToUser() {
        Toast.makeText(mContext, mContext!!.resources.getString(R.string.enable_permissions_to_proceed_further), Toast.LENGTH_SHORT).show()

    }

    interface RuntimePermissionRequestedCallback {
        fun permissionGranted(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int)

        fun permissionDenied(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int)
    }

    companion object {

        /*if you need to add permission
     * first add in permission list and then add icon list*/

        //    Permissions List
        val CAMERA_PERMISSION = Manifest.permission.CAMERA
        val WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        val CONTACT_PERMISSION = Manifest.permission.READ_CONTACTS
        @RequiresApi(Build.VERSION_CODES.S)
        val BLUETOOTH_PERMISSION = Manifest.permission.BLUETOOTH_CONNECT
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        val WIFI_PERMISSION = Manifest.permission.NEARBY_WIFI_DEVICES
        @RequiresApi(Build.VERSION_CODES.S)
        val BLUETOOTH_SCAN = Manifest.permission.BLUETOOTH_SCAN
        val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
        val MODIFY_AUDIO_PERMISSION = Manifest.permission.MODIFY_AUDIO_SETTINGS
        val DEFAULT_PERMISSION_CODE = Manifest.permission.INTERNET
        val PHONE_STATE = Manifest.permission.READ_PHONE_STATE
        @RequiresApi(33)
        val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS


        val READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
        @RequiresApi(33)
        val MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES
        @RequiresApi(33)
        val MEDIA_VIDEO = Manifest.permission.READ_MEDIA_VIDEO
        @RequiresApi(33)
        val MEDIA_AUDIO = Manifest.permission.READ_MEDIA_AUDIO

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        val POST_NOTIFICATION = Manifest.permission.POST_NOTIFICATIONS
        //public final int storageIcon = android.R.drawable.sym_contact_card; // this may be used as alternative icon for SDcard access

        val PERMISSION_ARRAY =  if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(MEDIA_IMAGES,BLUETOOTH_PERMISSION,LOCATION_PERMISSION,BLUETOOTH_SCAN,WIFI_PERMISSION,NOTIFICATION_PERMISSION)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf( WRITE_EXTERNAL_STORAGE_PERMISSION, READ_EXTERNAL_STORAGE_PERMISSION,
                    BLUETOOTH_PERMISSION,LOCATION_PERMISSION,BLUETOOTH_SCAN)
            } else {
                arrayOf(WRITE_EXTERNAL_STORAGE_PERMISSION, READ_EXTERNAL_STORAGE_PERMISSION,LOCATION_PERMISSION)
            }
        }

        // request Codes For Callback Identifications
        const val cameraAndGallaryCallBackCode = 0
        const val externalStoreageCallbackCode = 1
        const val locationCallbackCode = 2
        const val contactCallbackCode = 3
        const  val audioCallbackCode = 4
        private var requestCodeForCallbackIdentifications = 0
        val runtimePermissionDialogFragment = RuntimePermissionDialogFragment()

        fun checkPermissionStatus(mContext: Context, fragmentManager: FragmentManager, callbackListener: RuntimePermissionRequestedCallback, permissionsRequestFor: Array<String>, requestCodeForCallbackIdentification: Int, requestCodeForCallbackIdentificationSubDivision: Int) {
            requestCodeForCallbackIdentifications = requestCodeForCallbackIdentification
            /*
         * here function check permission status and then checks shouldAskPermissionForThisAndroidOSVersion or not
         * because some custom phone below Android M request permission from user at Run time example: redmi phones*/

            var allPermissionGranted: Boolean? = true

            for (permissionRequestFor in permissionsRequestFor) {
                if (checkSelfPermissions(mContext, permissionRequestFor)) {
                    allPermissionGranted = false
                    break
                }
            }
            if (!(allPermissionGranted)!!) {
                if (shouldAskPermissionForThisAndroidOSVersion()) {
                    runtimePermissionDialogFragment.permissionsRequestFor = permissionsRequestFor
                    runtimePermissionDialogFragment.callbackListener = callbackListener
                    runtimePermissionDialogFragment.requestCodeForCallbackIdentificationSubDivision = requestCodeForCallbackIdentificationSubDivision
                    runtimePermissionDialogFragment.mContext = mContext
                    runtimePermissionDialogFragment.show(fragmentManager, RuntimePermissionDialogFragment::class.java.name)
                } else {
                    //                we write code here becoz of static method, it not static method we call afterPermissionDenied()
                    callbackListener.permissionDenied(requestCodeForCallbackIdentification, requestCodeForCallbackIdentificationSubDivision)
                    Toast.makeText(mContext, mContext.resources.getString(R.string.enable_permissions_to_proceed_further), Toast.LENGTH_SHORT).show()

                }
            } else {
                callbackListener.permissionGranted(requestCodeForCallbackIdentification, requestCodeForCallbackIdentificationSubDivision)
            }
        }

        fun checkSelfPermissions(mContext: Context, permissionRequestFor: String): Boolean {
            return ContextCompat.checkSelfPermission(mContext, permissionRequestFor) != PackageManager.PERMISSION_GRANTED
        }

        fun shouldAskPermissionForThisAndroidOSVersion(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }

        fun dismissDialog() {
            if (runtimePermissionDialogFragment.isVisible)
                runtimePermissionDialogFragment.dismiss()
        }
    }
}
