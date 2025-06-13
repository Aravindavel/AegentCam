package com.android.aegentcam.webrtcscreenshare.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.android.aegentcam.R
import com.android.aegentcam.configs.AppController
import com.android.aegentcam.helper.CommonKeys.isServiceDestroy
import com.android.aegentcam.webrtcscreenshare.repository.MainRepository
import org.webrtc.MediaStream
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

class WebrtcService @Inject constructor() : Service() , MainRepository.Listener {


    companion object {
        var screenPermissionIntent : Intent ?= null
        var surfaceView:SurfaceViewRenderer?=null
        var listener: MainRepository.Listener?=null
    }

    @Inject lateinit var mainRepository: MainRepository

    private lateinit var notificationManager: NotificationManager
    private lateinit var username:String

    override fun onCreate() {
        super.onCreate()
        AppController.appComponent!!.inject(this)
        notificationManager = getSystemService(
            NotificationManager::class.java
        )
        mainRepository.listener = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent!=null){
            when(intent.action){
                "StartIntent"->{
                    this.username = intent.getStringExtra("username").toString()
                    mainRepository.init(username, surfaceView!!)
                    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    createNotificationChannel()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        startForeground(1, buildNotification(),
                            FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING)
                    }else
                        startForeground(1, buildNotification())
                }
                "StopIntent"->{
                    stopMyService()
                }
                "EndCallIntent"->{
                    mainRepository.sendCallEndedToOtherPeer()
                    stopMyService()
                }
                "AcceptCallIntent"->{
                    val target = intent.getStringExtra("target")
                    target?.let {
                        mainRepository.startCall(it)
                    }
                }
                "RequestConnectionIntent"->{
                    val target= intent.getStringExtra("target")
                    target?.let {
                        mainRepository.setPermissionIntentToWebrtcClient(screenPermissionIntent!!)
                        mainRepository.startScreenCapturing(surfaceView!!)
                        mainRepository.sendScreenShareConnection(it)
                    }
                }
            }
        }

        return START_STICKY
    }

    private fun stopMyService(){
        if (isServiceDestroy) {
            mainRepository.onDestroy()
            notificationManager.cancelAll()
            stopSelf()
            isServiceDestroy = !isServiceDestroy
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel1",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "channel1")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Screen Sharing Active")
            .setContentText("Your screen is being shared")
            .build()
    }

    override fun onConnectionRequestReceived(target: String) {
        listener?.onConnectionRequestReceived(target)
    }

    override fun onConnectionConnected() {
        listener?.onConnectionConnected()
    }

    override fun onCallEndReceived() {
        listener?.onCallEndReceived()
        stopMyService()
    }

    override fun onRemoteStreamAdded(stream: MediaStream) {
        listener?.onRemoteStreamAdded(stream)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}