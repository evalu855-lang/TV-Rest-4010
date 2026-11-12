package com.tvrest4010

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class TimerService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var blackScreen: View? = null
    private var windowManager: WindowManager? = null

    // 播放40分鐘，休息15分鐘
    private val playTime: Long = 40 * 60 * 1000L
    private val restTime: Long = 15 * 60 * 1000L

    // 顯示黑屏，15分鐘後恢復播放
    private val showRest: Runnable = object : Runnable {
        override fun run() {
            showBlackScreen()
            handler.postDelayed(hideRest, restTime)
        }
    }

    // 移除黑屏，40分鐘後再次休息
    private val hideRest: Runnable = object : Runnable {
        override fun run() {
            removeBlackScreen()
            handler.postDelayed(showRest, playTime)
        }
    }

    override fun onCreate() {
        super.onCreate()

        windowManager =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager

        createNotification()
    }

    private fun createNotification() {
        val channelId = "tv_rest_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "TV Rest 40/15",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            manager.createNotificationChannel(channel)
        }

        val builder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, channelId)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(this)
            }

        val notification: Notification = builder
            .setContentTitle("TV Rest 40/15")
            .setContentText("開機先黑屏15分鐘，播放40分鐘，休息15分鐘")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        handler.removeCallbacks(showRest)
        handler.removeCallbacks(hideRest)

        removeBlackScreen()

        // 開機後立即黑屏15分鐘
        showBlackScreen()
        handler.postDelayed(hideRest, restTime)

        return START_STICKY
    }

    private fun showBlackScreen() {
        if (blackScreen != null) return

        val view = View(this)
        view.setBackgroundColor(Color.BLACK)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.OPAQUE
        )

        params.gravity = Gravity.TOP or Gravity.START

        try {
            windowManager?.addView(view, params)
            blackScreen = view
        } catch (e: Exception) {
            blackScreen = null
        }
    }

    private fun removeBlackScreen() {
        blackScreen?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
            }
        }

        blackScreen = null
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        removeBlackScreen()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
