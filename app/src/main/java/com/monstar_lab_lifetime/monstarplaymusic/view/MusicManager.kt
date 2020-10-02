package com.monstar_lab_lifetime.monstarplaymusic.view

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.MutableLiveData

class MusicManager : MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    private var mp: MediaPlayer? = null
    val prepare = MutableLiveData<Int>()

    fun setData(context: Context, urlMusic: String) {
        mp?.release()
        mp = MediaPlayer()
        mp!!.setDataSource(context, Uri.parse(urlMusic))
        mp?.setOnErrorListener(this)
        mp?.setOnPreparedListener(this)
        mp?.prepareAsync()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onPrepared(mp: MediaPlayer) {
        prepare.value = mp.duration
        play()
    }

    fun play(): Boolean {
        if (mp == null) {
            return false
        }
        mp?.start()
        return true
    }

    fun pause(): Boolean {
        if (mp == null) {
            return false
        }
        mp?.pause()
        return true
    }

    fun stop(): Boolean {
        if (mp == null) {
            return false
        }
        mp?.stop()
        return true
    }

    fun release() {
        mp?.release()
    }
}