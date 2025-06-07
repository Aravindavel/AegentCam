package com.android.aegentcam.webrtcscreenshare.repository

import android.content.Intent
import android.util.Log
import com.android.aegentcam.configs.AppController
import com.android.aegentcam.webrtcscreenshare.socket.SocketClient
import com.android.aegentcam.webrtcscreenshare.utils.DataModel
import com.android.aegentcam.webrtcscreenshare.utils.DataModelType
import com.android.aegentcam.webrtcscreenshare.webrtc.MyPeerObserver
import com.android.aegentcam.webrtcscreenshare.webrtc.WebrtcClient
import com.google.gson.Gson
import org.json.JSONObject
import org.webrtc.*
import javax.inject.Inject


class MainRepository @Inject constructor(
    private val socketClient: SocketClient,
    private val webrtcClient: WebrtcClient
) : SocketClient.Listener, WebrtcClient.Listener {

    private lateinit var username: String
    private lateinit var target: String
    private lateinit var surfaceView: SurfaceViewRenderer
    var listener: Listener? = null

    init {
        AppController.appComponent!!.inject(this)
    }

    fun init(username: String, surfaceView: SurfaceViewRenderer) {
        this.username = username
        this.surfaceView = surfaceView
        initSocket()
        initWebrtcClient()

    }

    private fun initSocket() {
        socketClient.listener = this
        socketClient.init(username)
    }

    fun setPermissionIntentToWebrtcClient(intent:Intent){
        webrtcClient.setPermissionIntent(intent)
    }

    fun sendScreenShareConnection(target: String){
        socketClient.sendMessageToSocket(
            DataModel(
                type = DataModelType.StartStreaming,
                username = username,
                target = target,
                null
            )
        )
    }

    fun startScreenCapturing(surfaceView: SurfaceViewRenderer){
        webrtcClient.startScreenCapturing(surfaceView)
    }

    fun startCall(target: String){
        webrtcClient.call(target)
    }

    fun sendCallEndedToOtherPeer(){
        socketClient.sendMessageToSocket(
            DataModel(
                type = DataModelType.EndCall,
                username = username,
                target = target,
                null
            )
        )
    }

    fun restartRepository(){
        webrtcClient.restart()
    }

    fun onDestroy(){
        socketClient.onDestroy()
        webrtcClient.closeConnection()
    }

    private fun initWebrtcClient() {
        webrtcClient.listener = this
        webrtcClient.initializeWebrtcClient(username, surfaceView,
            object : MyPeerObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    Log.d("ICE", "onIceCandidate: ${p0?.sdp}")
                    p0?.let { webrtcClient.sendIceCandidate(it, target) }
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    super.onConnectionChange(newState)
                    Log.d("TAG", "onConnectionChange: $newState")
                    if (newState == PeerConnection.PeerConnectionState.CONNECTED){
                        listener?.onConnectionConnected()
                    }
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    Log.d("TAG", "onAddStream: $p0")
                    p0?.let { listener?.onRemoteStreamAdded(it) }
                }
            })
    }

    override fun onNewMessageReceived(model: DataModel) {
        when (model.type) {
            DataModelType.StartStreaming -> {
                this.target = model.username
                //notify ui, conneciton request is being made, so show it
                listener?.onConnectionRequestReceived(model.username)
            }
            DataModelType.EndCall -> {
                //notify ui call is ended
                listener?.onCallEndReceived()
            }
            DataModelType.Offer -> {
                webrtcClient.onRemoteSessionReceived(
                    SessionDescription(
                        SessionDescription.Type.OFFER, model.data
                            .toString()
                    )
                )
                this.target = model.username
                webrtcClient.answer(target)
            }
            DataModelType.Answer -> {
                webrtcClient.onRemoteSessionReceived(
                    SessionDescription(SessionDescription.Type.ANSWER, model.data.toString())
                )

            }
            DataModelType.IceCandidates -> {
                try {
                    val json = JSONObject(model.data.toString())
                    val sdp = json.optString("sdp", null)
                    val sdpMid = json.optString("sdpMid", null)
                    val sdpMLineIndex = if (json.has("sdpMLineIndex")) json.getInt("sdpMLineIndex") else -1

                    if (!sdp.isNullOrEmpty() && !sdpMid.isNullOrEmpty() && sdpMLineIndex >= 0) {
                        val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                        webrtcClient.addIceCandidate(candidate)
                    } else {
                        Log.e("WebRTC", "Invalid ICE data: sdp=$sdp, sdpMid=$sdpMid, sdpMLineIndex=$sdpMLineIndex")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("WebRTC", "Exception while parsing ICE candidate: ${e.message}")
                }

            }
            else -> Unit
        }
    }

    override fun onTransferEventToSocket(data: DataModel) {
        socketClient.sendMessageToSocket(data)
    }

    interface Listener {
        fun onConnectionRequestReceived(target: String)
        fun onConnectionConnected()
        fun onCallEndReceived()
        fun onRemoteStreamAdded(stream: MediaStream)
    }
}