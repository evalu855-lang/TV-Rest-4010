package com.tvrest4010

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
        title.text = "TV Rest 40/10"
        title.textSize = 28f
        layout.addView(title)

        val status = TextView(this)
        status.text = "請先允許顯示在其他應用程式上層"
        status.textSize = 18f
        layout.addView(status)

        val permissionButton = Button(this)
        permissionButton.text = "開啟畫面覆蓋權限"
        permissionButton.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
        layout.addView(permissionButton)

        val startButton = Button(this)
        startButton.text = "開始休息計時"
        startButton.setOnClickListener {
            startService(Intent(this, TimerService::class.java))
            status.text = "計時已啟動：測試版 1 分鐘播放／1 分鐘休息"
        }
        layout.addView(startButton)

        setContentView(layout)
    }
}
