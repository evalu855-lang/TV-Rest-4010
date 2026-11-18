
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

    private val playTime = 40 * 60 * 1000L
    private val restTime = 15 * 60 * 1000L

    private val prefs by lazy {
        getSharedPreferences("TV_REST_STATE", Context.MODE_PRIVATE)
    }

    private val playRunnable = Runnable {
        startRest()
    }

    private val restRunnable = Runnable {
        startPlay()
    }

    companion object {
        const val ACTION_BOOT = "com.tvrest4010.ACTION_BOOT"
        private const val KEY_PHASE = "phase"
        private const val KEY_DEADLINE = "deadline"

        private const val PLAY = "PLAY"
        private const val REST = "REST"
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

        val notification = builder
            .setContentTitle("TV Rest 40/15")
            .setContentText("播放40分鐘，黑屏休息15分鐘")
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

        val isBoot = intent?.action == ACTION_BOOT

        restoreState(isBoot)

        return START_STICKY
    }

    private fun restoreState(isBoot: Boolean) {

        val phase = prefs.getString(KEY_PHASE, null)
        val deadline = prefs.getLong(KEY_DEADLINE, 0L)
        val now = System.currentTimeMillis()

        // 開機時：
        // 如果原本正在休息，就繼續計算剩餘時間。
        // 如果原本正在播放，就重新計算40分鐘。
        if (isBoot) {

            if (phase == REST && deadline > now) {
                resumeRest(deadline)
            } else {
                startPlay()
            }

            return
        }

        // 服務重新啟動時，恢復原本計時狀態。
        if (deadline > now) {

            when (phase) {
                REST -> resumeRest(deadline)
                PLAY -> resumePlay(deadline)
                else -> startPlay()
            }

        } else {

            // 原本的播放時間已結束，進入休息。
            if (phase == PLAY) {
                startRest()
            } else {
                startPlay()
            }
        }
    }

    private fun startPlay() {

        handler.removeCallbacks(playRunnable)
        handler.removeCallbacks(restRunnable)

        removeBlackScreen()

        val deadline = System.currentTimeMillis() + playTime

        prefs.edit()
            .putString(KEY_PHASE, PLAY)
            .putLong(KEY_DEADLINE, deadline)
            .apply()

        handler.postDelayed(playRunnable, playTime)
    }

    private fun resumePlay(deadline: Long) {

        handler.removeCallbacks(playRunnable)
        handler.removeCallbacks(restRunnable)

        removeBlackScreen()

        val remaining = deadline - System.currentTimeMillis()

        handler.postDelayed(playRunnable, remaining)
    }

    private fun startRest() {

        handler.removeCallbacks(playRunnable)
        handler.removeCallbacks(restRunnable)

        val deadline = System.currentTimeMillis() + restTime

        prefs.edit()
            .putString(KEY_PHASE, REST)
            .putLong(KEY_DEADLINE, deadline)
            .apply()

        showBlackScreen()

        handler.postDelayed(restRunnable, restTime)
    }

    private fun resumeRest(deadline: Long) {

        handler.removeCallbacks(playRunnable)
        handler.removeCallbacks(restRunnable)

        showBlackScreen()

        val remaining = deadline - System.currentTimeMillis()

        if (remaining > 0) {
            handler.postDelayed(restRunnable, remaining)
        } else {
            startPlay()
        }
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
