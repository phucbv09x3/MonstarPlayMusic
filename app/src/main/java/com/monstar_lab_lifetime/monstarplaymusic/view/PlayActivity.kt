package com.monstar_lab_lifetime.monstarplaymusic.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.monstar_lab_lifetime.monstarplaymusic.R
import com.monstar_lab_lifetime.monstarplaymusic.service.MusicService

class PlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        val inten=intent
        var uri=inten.getStringExtra("Key1")
        startService(Intent(this, MusicService::class.java))
    }
}