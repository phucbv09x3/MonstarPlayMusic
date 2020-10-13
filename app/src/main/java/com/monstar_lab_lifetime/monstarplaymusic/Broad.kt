package com.monstar_lab_lifetime.monstarplaymusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class Broad : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val ac=intent?.action
        intent?.putExtra("ok",ac.toString())
        context?.sendBroadcast(intent)
    }
}