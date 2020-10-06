package com.monstar_lab_lifetime.monstarplaymusic.view

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.MutableLiveData

class MusicManager : MediaPlayer.OnPreparedListener {
    var mMediaPlayer: MediaPlayer? = null
    var durationMusic = MutableLiveData<Int>()
    fun setData(context: Context, urlMusic: String) {
        mMediaPlayer?.release()
        mMediaPlayer = MediaPlayer()
        //mMediaPlayer?.isLooping
        mMediaPlayer!!.setDataSource(context, Uri.parse(urlMusic))
        mMediaPlayer?.setOnPreparedListener(this)
        mMediaPlayer?.prepareAsync()
    }

    override fun onPrepared(mp: MediaPlayer) {
        durationMusic.value = mp?.duration
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