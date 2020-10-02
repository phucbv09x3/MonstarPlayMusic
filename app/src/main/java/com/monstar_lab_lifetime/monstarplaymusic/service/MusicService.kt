package com.monstar_lab_lifetime.monstarplaymusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.monstar_lab_lifetime.monstarplaymusic.R
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.view.MusicManager
import com.monstar_lab_lifetime.monstarplaymusic.viewmodel.MusicViewModel

class MusicService : Service() {
    private lateinit var model: MusicViewModel
    private var musicManager: MusicManager? = null
    var mediaPlayer: MediaPlayer? = null

    var music: Music? = null
    override fun onBind(intent: Intent?): IBinder? {
        mediaPlayer?.start()
        return MyBinder(this)
    }

    fun getMusicManager() = musicManager
    fun getModel() = model
    override fun onCreate() {
        super.onCreate()
        model = MusicViewModel()
        musicManager = MusicManager()
        registerChanel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var intent = intent
        val bundle=intent!!.extras
        var link = bundle!!.getString("uri")
        var name = bundle!!.getString("name")
        mediaPlayer = MediaPlayer.create(this, Uri.parse(link))
        mediaPlayer?.isLooping
        mediaPlayer?.start()
        registerChanel()
        createNotificationMusic(name!!)
        return START_NOT_STICKY
    }

    fun play(item: Music) {
        musicManager?.setData(this, item.uri!!)
       // createNotificationMusic(item)
    }

    class MyBinder : Binder {
        var musicService: MusicService

        constructor(musicService: MusicService) {
            this.musicService = musicService
        }
    }

    private fun registerChanel() {
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MusicService",
                "MusicService",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "YOUR_NOTIFICATION_CHANNEL_DESCRIPTION"
            mNotificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotificationMusic(item: String) {
        //tao notification build de setup thong so
        val noti = NotificationCompat.Builder(
            this,
            "MusicService"
        )
        noti.setContentTitle("Music Offline")
        noti.setContentText(item)
        noti.setSmallIcon(R.drawable.ic_baseline_library_music_24)
        noti.setLargeIcon(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.aodai
            )
        )
        startForeground(10, noti.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
    }
}