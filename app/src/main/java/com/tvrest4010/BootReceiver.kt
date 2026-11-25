
package com.tvrest4010

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val serviceIntent = Intent(
            context,
            TimerService::class.java
        ).apply {
            action = TimerService.ACTION_BOOT
        }

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                context.startForegroundService(serviceIntent)

            } else {

                context.startService(serviceIntent)
            }

        } catch (e: Exception) {

            // 開機啟動失敗時避免造成系統當機
        }
    }
}
