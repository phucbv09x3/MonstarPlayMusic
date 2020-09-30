package com.monstar_lab_lifetime.monstarplaymusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.monstar_lab_lifetime.monstarplaymusic.R
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.viewmodel.MusicViewModel

class MusicService : Service() {
    private lateinit var model: MusicViewModel
    lateinit var mediaPlayer: MediaPlayer
    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder(this)

    }

    override fun onCreate() {
        super.onCreate()
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI)!!
//        mediaPlayer.isLooping
//        mediaPlayer.start()
//        return START_STICKY
//    }

    fun play() {
        mediaPlayer.start()
    }

    class MyBinder : Binder {
        val musicService: MusicService
        constructor(musicService: MusicService) {
            this.musicService = musicService
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
    }
}