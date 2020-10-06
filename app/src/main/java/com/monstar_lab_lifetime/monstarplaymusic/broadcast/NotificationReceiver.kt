package com.monstar_lab_lifetime.monstarplaymusic.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.monstar_lab_lifetime.monstarplaymusic.service.MusicService

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context!!.sendBroadcast(Intent("ACTION").putExtra("actionname",intent))
    }
}