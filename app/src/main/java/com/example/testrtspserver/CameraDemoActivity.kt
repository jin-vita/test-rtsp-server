package com.example.testrtspserver

import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.TextureView
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.library.view.AutoFitTextureView
import com.pedro.rtspserver.RtspServerCamera1
import com.pedro.rtspserver.server.ClientListener
import com.pedro.rtspserver.server.ServerClient
import kotlin.system.exitProcess

class CameraDemoActivity : AppCompatActivity(), ConnectChecker, ClientListener, TextureView.SurfaceTextureListener {
    companion object{
        private val TAG: String = CameraDemoActivity::class.java.simpleName
    }
    private lateinit var rtspServerCamera1: RtspServerCamera1
    private lateinit var surfaceView: AutoFitTextureView
    private var isInit = false

    private val handler by lazy { Handler(mainLooper) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_camera_demo)
        surfaceView = findViewById(R.id.surfaceView)
        rtspServerCamera1 = RtspServerCamera1(surfaceView, this, 1935)
        rtspServerCamera1.streamClient.setClientListener(this)
        surfaceView.surfaceTextureListener = this

        startPrepareCheck()
    }

    private fun startPrepareCheck() {
        handler.post(object : Runnable {
            override fun run() {
                if (prepare()) {
                    rtspServerCamera1.startStream()
                    ScreenOrientation.lockScreen(this@CameraDemoActivity)
                    handler.removeCallbacks(this)
                } else {
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun prepare(): Boolean {
        rtspServerCamera1.resolutionsBack.forEach{
            Log.d(TAG, "resolutionsBack ${it.height} * ${it.width}")
        }
        rtspServerCamera1.resolutionsFront.forEach{
            Log.d(TAG, "resolutionsFront ${it.height} * ${it.width}")
        }

//        val prepared = rtspServerCamera1.prepareAudio() && rtspServerCamera1.prepareVideo(1280, 720, 10, 300 * 1024, 0)
//        val prepared = rtspServerCamera1.prepareAudio() && rtspServerCamera1.prepareVideo(800, 600, 10, 300 * 1024, 0)
        val prepared = rtspServerCamera1.prepareAudio() && rtspServerCamera1.prepareVideo()
        isInit = true
        adaptPreview()
        return prepared
    }

    private fun adaptPreview() {
        val isPortrait = CameraHelper.isPortrait(this)
        val w = if (isPortrait) rtspServerCamera1.streamHeight else rtspServerCamera1.streamWidth
        val h = if (isPortrait) rtspServerCamera1.streamWidth else rtspServerCamera1.streamHeight
        surfaceView.setAspectRatio(w, h)
    }

    private fun restart() {
        Log.i(TAG, "restart !!!!")
        val componentName = packageManager.getLaunchIntentForPackage(packageName)?.component
        startActivity(Intent.makeRestartActivityTask(componentName))
        exitProcess(0)
    }

    override fun onNewBitrate(bitrate: Long) {

    }

    override fun onConnectionSuccess() {
        toast("Connected")
    }

    override fun onConnectionFailed(reason: String) {
        Log.e(TAG, "failed: $reason")
        toast("Failed: $reason")
        rtspServerCamera1.stopStream()
        ScreenOrientation.unlockScreen(this)

        restart()
    }

    override fun onConnectionStarted(url: String) {
    }

    override fun onDisconnect() {
        toast("Disconnected")
    }

    override fun onAuthError() {
        toast("Auth error")
        rtspServerCamera1.stopStream()
        ScreenOrientation.unlockScreen(this)
    }

    override fun onAuthSuccess() {
        toast("Auth success")
    }

    override fun onClientConnected(client: ServerClient) {
        toast("Client connected: ${client.clientAddress}")
    }

    override fun onClientDisconnected(client: ServerClient) {
        toast("Client disconnected: ${client.clientAddress}")
    }

//    override fun onClientNewBitrate(bitrate: Long, client: ServerClient) {
//        Log.d(TAG, "onClientNewBitrate: $bitrate")
//    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (!rtspServerCamera1.isOnPreview && isInit) {
            rtspServerCamera1.startPreview()
            adaptPreview()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        if (rtspServerCamera1.isStreaming) {
            rtspServerCamera1.stopStream()
        }
        if (rtspServerCamera1.isOnPreview) rtspServerCamera1.stopPreview()
        ScreenOrientation.unlockScreen(this)
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }
}