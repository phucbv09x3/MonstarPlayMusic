package com.monstar_lab_lifetime.monstarplaymusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.monstar_lab_lifetime.monstarplaymusic.R
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.view.MusicManager
import com.monstar_lab_lifetime.monstarplaymusic.viewmodel.MusicViewModel

class MusicService : Service() {
    private lateinit var mMusicViewModel: MusicViewModel
    private var mMusicManager: MusicManager? = null
    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder(this)
    }

    fun getMusicManager() = mMusicManager
    fun getModel() = mMusicViewModel
    override fun onCreate() {
        super.onCreate()
        mMusicViewModel = MusicViewModel()
        mMusicManager = MusicManager()
        registerChanel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    fun playMusic(item: Music) {
        mMusicManager?.setData(this, item.uri!!)
        mMusicManager?.play()
        createNotificationMusic(item.nameMusic)
    }
    fun pauseMusic(item:Music){
        mMusicManager?.pause()
    }
    fun continuePlayMusic(item: Music){
        mMusicManager?.continuePlay()
    }
    fun stopMusic(item: Music){
        mMusicManager?.stop()
    }

    class MyBinder : Binder {
        var getService: MusicService
        constructor(musicService: MusicService) {
            this.getService = musicService
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
        val notification = NotificationCompat.Builder(
            this,
            "MusicService"
        )
        notification.setContentTitle("Music Offline")
        notification.setContentText(item)
        notification.setSmallIcon(R.drawable.ic_baseline_library_music_24)
        notification.setLargeIcon(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.aodai
            )
        )
        startForeground(10, notification.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        mMusicManager?.release()
    }
}