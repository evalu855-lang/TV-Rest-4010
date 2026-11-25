
package com.tvrest4010

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 40, 40, 40)

        val title = TextView(this)
        title.text = "TV Rest 40/15"
        title.textSize = 28f
        layout.addView(title)

        val status = TextView(this)
        status.text = "每播放40分鐘，自動黑屏休息15分鐘"
        status.textSize = 18f
        layout.addView(status)

        val permissionButton = Button(this)
        permissionButton.text = "開啟畫面覆蓋權限"

        permissionButton.setOnClickListener {
            val permissionIntent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(permissionIntent)
        }

        layout.addView(permissionButton)

        val startButton = Button(this)
        startButton.text = "開始計時"

        startButton.setOnClickListener {

            val serviceIntent =
                Intent(this, TimerService::class.java).apply {
                    action = TimerService.ACTION_START
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            status.text = "已開始40分鐘播放計時"
        }

        layout.addView(startButton)

        setContentView(layout)
    }
}
