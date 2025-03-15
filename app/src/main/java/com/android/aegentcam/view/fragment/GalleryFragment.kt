package com.android.aegentcam.view.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.aegentcam.R
import com.android.aegentcam.configs.AppController
import com.android.aegentcam.databinding.FragmentGalleryBinding
import com.android.aegentcam.helper.MediaModel
import com.android.aegentcam.helper.WifiEnableDialog
import com.android.aegentcam.helper.WifiP2pConnectingDialog
import com.android.aegentcam.model.GalleryItem
import com.android.aegentcam.network.NetworkResult
import com.android.aegentcam.view.activity.MainActivity
import com.android.aegentcam.view.adapter.GalleryRecyclerAdapter
import com.linkflow.blackboxsdk.Constant
import com.linkflow.blackboxsdk.NeckbandRestApiClient
import com.linkflow.blackboxsdk.helper.WifiConnectHelper
import com.linkflow.blackboxsdk.helper.media.DownloadHelper
import com.linkflow.blackboxsdk.manager.BTCommandManager
import com.linkflow.blackboxsdk.manager.BTCommandManager.CommandCallback
import com.linkflow.blackboxsdk.manager.BTCommandManager.STEP
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Objects


class GalleryFragment : BaseFragment(), WifiConnectHelper.ConnectListener,
    BTCommandManager.ConnectStateListener,
    GalleryRecyclerAdapter.ItemClickListener,
    MediaModel.Listener {

    lateinit var binding : FragmentGalleryBinding

    val ACTION_CHANGED_WIFI_P2P_STATUS: String = "changed_wifi_p2p_status"
    val ACTION_CHANGE_WIFI_P2P_STATUS: String = "change_wifi_p2p_status"
    val ACTION_UNPAIRED_DEVICE: String = "unpaired_device"
    val MAX_WIFI_TIMEOUT: Int = 1000 * 60 * 15

    private enum class STORE_TYPE {
        TYPE_VIDEO, TYPE_PHOTO, TYPE_FILE
    }

    private var mWifiP2pConnectingDialog: WifiP2pConnectingDialog? = null
    private var mWifiEnableDialog: WifiEnableDialog? = null
    private var mWifiHelper: WifiConnectHelper? = null

    private var mAdapter: GalleryRecyclerAdapter? = null
    private var mMediaModel: MediaModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentGalleryBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initViews()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        AppController.globalApplicationContext!!.setCurrentActivity(requireActivity())
        WifiConnectHelper.getInstance(AppController.globalApplicationContext)
            .setListener(this)

        // Re-register WiFi state receiver to ensure it listens every time
        val filter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        requireContext().registerReceiver(wifiStateReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
       /* mWifiHelper!!.enabledWifi()
        disconnectWifiP2p(AppController.globalApplicationContext)*/
        requireContext().unregisterReceiver(wifiStateReceiver)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(wifiP2pChangedReceiver)
        super.onDestroy()
    }

    override fun onSuccess(networkResult: NetworkResult<Any>) {
        
    }

    override fun onFailure(networkResult: NetworkResult<Any>) {
        
    }

    override fun onLoading(networkResult: NetworkResult<Any>) {
        
    }

    private fun initViews() {
        binding.tvTitle.text = getString(R.string.gallery)
        mMediaModel = MediaModel(this)
        mWifiHelper = WifiConnectHelper.getInstance(AppController.globalApplicationContext)
        mWifiHelper!!.registerReceiver(AppController.globalApplicationContext)

        val manager = GridLayoutManager(requireContext(), 3)
        mAdapter = GalleryRecyclerAdapter(requireContext(), this)
        binding.rvGallery.layoutManager = manager
        binding.rvGallery.adapter = mAdapter

        initDialog()

        val filter = IntentFilter(ACTION_CHANGED_WIFI_P2P_STATUS).apply {
            addAction(ACTION_CHANGE_WIFI_P2P_STATUS)
            addAction(ACTION_UNPAIRED_DEVICE)
        }
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(wifiP2pChangedReceiver, filter)
    }

    private val wifiStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                changedWifiState(state)
            }
        }
    }

    override fun changedWifiState(state: Int) {
        Log.d("WifiState", "Changed state: $state")
        if (state == WifiManager.WIFI_STATE_ENABLED) {
            mWifiEnableDialog!!.dismissAllowingStateLoss()
            requestToChangeWifiP2pStatus()
        }
    }

    override fun completedConnectP2p(info: WifiP2pInfo) {
        mWifiP2pConnectingDialog!!.dismissAllowingStateLoss()
        NeckbandRestApiClient.setBaseUrl(info.groupOwnerAddress.hostAddress)
        getMediaList()
    }

    override fun discoveryP2pTryCnt(i: Int, i1: Int) {}

    override fun disconnectP2p(forceClose: Boolean, i: Int, wifiP2pInfo: WifiP2pInfo?, s: String?) {
        if (!forceClose) {
            requestToChangeWifiP2pStatus()
        }
    }

    override fun onConnectState(step: STEP?) {
        if (step == STEP.STEP_DONE) {
            if (!mWifiHelper!!.isConnected) {
                requestToChangeWifiP2pStatus()
            } else {
                getMediaList()
            }
        }
    }

    override fun alert(alert: BTCommandManager.ALERT?) {
        
    }

    private fun initDialog() {
        mWifiP2pConnectingDialog = WifiP2pConnectingDialog()
        mWifiP2pConnectingDialog!!.setClickListener(View.OnClickListener {
            mWifiP2pConnectingDialog!!.dismissAllowingStateLoss()
            disconnectWifiP2p(AppController.globalApplicationContext)
        })

        mWifiEnableDialog = WifiEnableDialog()
        mWifiEnableDialog!!.setClickListener(View.OnClickListener { view ->
            if (view.id == R.id.base_dialog_agree) {
                mWifiHelper!!.registerReceiver(AppController.globalApplicationContext)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivity(Intent(Settings.Panel.ACTION_WIFI))
                } else {
                    mWifiHelper!!.enabledWifi()
                }
            }
            mWifiEnableDialog!!.dismissAllowingStateLoss()
        })
    }

    private fun requestToChangeWifiP2pStatus() {
        (context as MainActivity).mBTCommandManager!!.commandSetWifiStatusAsDelay(true,
            MAX_WIFI_TIMEOUT,
            CommandCallback { success, response ->
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
                            changeWifiP2pStatus(requireContext(), true, ssid)
                        } else {
                            Handler().postDelayed({ requestToChangeWifiP2pStatus() }, 1000)
                        }
                    }
                }
            })
    }

    fun changeWifiP2pStatus(context: Context?, wifiP2pEnabled: Boolean, wifiP2pSSID: String?) {
        if (wifiP2pEnabled) {
            if (!mWifiHelper!!.isEnabledWifi) {
                mWifiEnableDialog!!.show((context as MainActivity).supportFragmentManager)
            } else {
                if (!mWifiHelper!!.isConnected && wifiP2pSSID != null) {
                    if (!mWifiP2pConnectingDialog!!.isAdded) {
                        mWifiP2pConnectingDialog!!.doShow((context as MainActivity).supportFragmentManager)
                    }
                    mWifiHelper!!.startDiscovery(wifiP2pSSID)
                }else {
                    mWifiHelper!!.startDiscovery(wifiP2pSSID)
                    getMediaList()
                }
            }
        } else {
            disconnectWifiP2p(AppController.globalApplicationContext)
            Toast.makeText(context, R.string.wifi_disconnected_time_out, Toast.LENGTH_SHORT).show()
        }
    }

    protected fun disconnectWifiP2p(context: Context?) {
        mWifiHelper!!.unregisterReceiver(context)
        mWifiHelper!!.stopDiscovery(null)
        mWifiHelper!!.disconnect(true, -1, null)
    }

    fun getMediaList() {
        if ((context as MainActivity).mBTCommandManager!!.isConnected()) {
            if (mMediaModel != null) {
                try {
                    mMediaModel!!.getMediaList((context as MainActivity).mBTCommandManager!!.getAccessToken(), 0, 5000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun clickedItem(position: Int, isDelete: Boolean) {
        val item = mAdapter!!.getItem(position)
        try {
            if (isDelete) {
                mMediaModel!!.delete(
                    (context as MainActivity).mBTCommandManager!!.accessToken,
                    arrayOf<String?>(item.fileName)
                )
            } else {
                downloadMedia(item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun downloadMedia(galleryItem: GalleryItem) {
        val file = File(Constant.RECORD_SAVE_PATH)
        if (!file.exists()) {
            file.mkdirs()
        }
        mMediaModel!!.download(
            (context as MainActivity).mBTCommandManager!!.getAccessToken(),
            galleryItem,
            object : DownloadHelper.DownloadListener {
                override fun beginDownload() {
                    Toast.makeText(
                        requireContext(),
                        R.string.download_start,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun progress(current: Long, max: Long) {
                }

                override fun endDownload(success: Boolean) {
                    Toast.makeText(
                        requireContext(),
                        if (success) R.string.download_done else R.string.download_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                @Throws(FileNotFoundException::class)
                override fun getCreatedFileOutputStream(item: DownloadHelper.Item): OutputStream? {
                    val isVideo: Boolean = galleryItem.fileName!!.matches(Regex(".+\\.(mp4|MP4)$"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        return getMediaOutputStream(
                            if (isVideo) STORE_TYPE.TYPE_VIDEO else STORE_TYPE.TYPE_PHOTO,
                            galleryItem.fileName!!,
                            item
                        )!!
                    } else {
                        try {
                            item.mPath =
                                (if (isVideo) Constant.RECORD_SAVE_PATH else Constant.PICTURE_SAVE_PATH) + galleryItem.fileName
                            val file = File(item.mPath)
                            if (file.exists()) {
                                file.delete()
                            }
                            if (file.createNewFile()) {
                                return FileOutputStream(file)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        return null
                    }
                }

                override fun needToDeleteFile(s: String) {
                }

                @Throws(FileNotFoundException::class)
                fun getMediaOutputStream(
                    type: STORE_TYPE?,
                    filename: String,
                    item: DownloadHelper.Item
                ): OutputStream? {
                    val resolver: ContentResolver = requireContext().contentResolver
                    val contentValues = ContentValues()
                    var mediaUri: Uri? = null
                    when (type) {
                        STORE_TYPE.TYPE_FILE -> {
                            contentValues.put(
                                MediaStore.Files.FileColumns.DISPLAY_NAME,
                                "$filename.txt"
                            )
                            contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain")
                            contentValues.put(
                                MediaStore.Files.FileColumns.RELATIVE_PATH,
                                Constant.GPS_SAVE_PATH
                            )
                            mediaUri = resolver.insert(
                                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                                contentValues
                            )
                        }

                        STORE_TYPE.TYPE_PHOTO -> {
                            contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, filename)
                            contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpg")
                            contentValues.put(
                                MediaStore.Images.ImageColumns.RELATIVE_PATH,
                                Constant.PICTURE_SAVE_PATH
                            )
                            mediaUri = resolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                contentValues
                            )
                        }

                        STORE_TYPE.TYPE_VIDEO -> {
                            contentValues.put(MediaStore.Video.VideoColumns.DISPLAY_NAME, filename)
                            contentValues.put(MediaStore.Video.VideoColumns.MIME_TYPE, "video/mp4")
                            contentValues.put(
                                MediaStore.Video.VideoColumns.RELATIVE_PATH,
                                Constant.RECORD_SAVE_PATH
                            )
                            mediaUri = resolver.insert(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                contentValues
                            )
                        }

                        else ->{}
                    }
                    item.mPath = getOriginalPath(requireActivity(), mediaUri)
                    return resolver.openOutputStream(Objects.requireNonNull(mediaUri)!!)
                }

                @SuppressLint("Range")
                fun getOriginalPath(context: Context, contentUri: Uri?): String {
                    val proj = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
                    cursor!!.moveToNext()
                    val path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                    cursor.close()
                    return path
                }
            })
    }


    override fun completedGetMediaList(success: Boolean, allItems: ArrayList<GalleryItem>?) {
        if (success) {
            mAdapter!!.setAllList(allItems!!)
        }
    }

    override fun completedDelete(success: Boolean, filenames: Array<String?>?, path: String?) {
        if (success) {
            getMediaList()
            Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show()
        }
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

                    ACTION_UNPAIRED_DEVICE -> disconnectWifiP2p(
                        AppController.globalApplicationContext
                    )
                }
            }
        }
    }
}