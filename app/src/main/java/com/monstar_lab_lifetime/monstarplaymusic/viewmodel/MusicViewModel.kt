package com.monstar_lab_lifetime.monstarplaymusic.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.monstar_lab_lifetime.monstarplaymusic.model.Music

class MusicViewModel : ViewModel() {
    var listMusic = MutableLiveData<MutableList<Music>>()
    private var mListMusicInViewModel = mutableListOf<Music>()
    fun getListMusicOffLine(contentResolver: ContentResolver) {

        val uriExternal : Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor=contentResolver.query(uriExternal, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val uri=cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val id = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val duration=cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            do {
                val idd = cursor.getString(id)
                val currentTT = cursor.getString(title)
                val currentArtist = cursor.getString(songArtist)
                val uri=cursor.getString(uri)
                val durationMusic=cursor.getString(duration)
                mListMusicInViewModel.add(
                    Music(
                        idd,
                        1,
                        currentTT,
                        currentArtist,
                        uri,
                        durationMusic
                    )
                )
            } while (cursor.moveToNext())
            listMusic.value = mListMusicInViewModel
        }
    }
}