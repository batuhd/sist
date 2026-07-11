package com.sinop.sist.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sinop.sist.SistApplication

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as? SistApplication ?: return
            app.scheduleWorkers()
        }
    }
}
