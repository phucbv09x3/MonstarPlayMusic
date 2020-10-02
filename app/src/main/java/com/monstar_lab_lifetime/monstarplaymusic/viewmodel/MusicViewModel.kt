package com.monstar_lab_lifetime.monstarplaymusic.viewmodel

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.monstar_lab_lifetime.monstarplaymusic.adapter.MusicAdapter
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.view.HomeActivity
import java.util.Collections.list

class MusicViewModel : ViewModel() {
    private var homeActivity: HomeActivity?=null
    var listMusic = MutableLiveData<MutableList<Music>>()
    var listt = mutableListOf<Music>()
    fun getListMusicOffLine(contentResolver: ContentResolver) {
        var uri : Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var cursor=contentResolver?.query(uri,null,null,null,null)
        if (cursor != null && cursor!!.moveToFirst()) {
            var urii=cursor!!.getColumnIndex(MediaStore.Audio.Media.DATA)
            var id = cursor!!.getColumnIndex(MediaStore.Audio.Media._ID)
            var title = cursor!!.getColumnIndex(MediaStore.Audio.Media.TITLE)
            var songArtist = cursor!!.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            var duration=cursor!!.getColumnIndex(MediaStore.Audio.Media.DURATION)
            do {
                val idd = cursor!!.getString(id)
                val currentTT = cursor!!.getString(title)
                val currentArtist = cursor!!.getString(songArtist)
                val uri=cursor!!.getString(urii)
                val durationMusic=cursor!!.getString(duration)
                listt.add(Music(idd, 1, currentTT, currentArtist,uri,durationMusic))
                listMusic.value = listt
            } while (cursor!!.moveToNext())
        }
    }
}