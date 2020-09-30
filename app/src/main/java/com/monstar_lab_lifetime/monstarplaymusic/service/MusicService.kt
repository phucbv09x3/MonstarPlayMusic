package com.monstar_lab_lifetime.monstarplaymusic.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.monstar_lab_lifetime.monstarplaymusic.viewmodel.MusicViewModel

class MusicService : Service() {
    private lateinit var model: MusicViewModel
     var mediaPlayer: MediaPlayer?=null

    override fun onBind(intent: Intent?): IBinder? {
        mediaPlayer?.start()
        return MyBinder(this)

    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer?.isLooping
        mediaPlayer?.start()
        return START_STICKY
    }

    fun play() {
        mediaPlayer?.start()
    }

    class MyBinder : Binder {
        var musicService: MusicService
        constructor(musicService: MusicService) {
            this.musicService = musicService
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
    }
}