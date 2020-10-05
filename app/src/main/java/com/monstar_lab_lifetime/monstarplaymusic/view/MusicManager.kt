package com.monstar_lab_lifetime.monstarplaymusic.view

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.MutableLiveData

class MusicManager : MediaPlayer.OnErrorListener,MediaPlayer.OnPreparedListener {
    private var mMediaPlayer: MediaPlayer? = null
    var prepare = MutableLiveData<Int>()

    fun setData(context: Context, urlMusic: String) {
        mMediaPlayer?.release()
        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.isLooping
        mMediaPlayer!!.setDataSource(context, Uri.parse(urlMusic))
        mMediaPlayer?.setOnErrorListener(this)
        mMediaPlayer?.setOnPreparedListener(this)
        mMediaPlayer?.prepareAsync()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onPrepared(mp: MediaPlayer) {
        prepare.value = mp.duration
        play()
    }

    fun play(): Boolean {
        if (mMediaPlayer == null) {
            return false
        }
        mMediaPlayer?.start()
        return true
    }

    fun pause(): Boolean {
        if (mMediaPlayer == null) {
            return false
        }
        mMediaPlayer?.pause()
        return true
    }

    fun continuePlay(): Boolean {
        if (mMediaPlayer == null) {
            return false
        }
        mMediaPlayer?.start()
        return true
    }

    fun stop(): Boolean {
        if (mMediaPlayer == null) {
            return false
        }
        mMediaPlayer?.stop()
        return true
    }

    fun release() {
        mMediaPlayer?.release()
    }
}