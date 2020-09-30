package com.monstar_lab_lifetime.monstarplaymusic.viewmodel

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.monstar_lab_lifetime.monstarplaymusic.adapter.MusicAdapter
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.view.HomeActivity
import java.util.Collections.list

class MusicViewModel : ViewModel(){
    private var homeActivity: HomeActivity? = null
    var currentTT: String = ""
    var listMusic = MutableLiveData<MutableList<Music>>()
    var listt = mutableListOf<Music>()
    var cursor: Cursor? = null
    fun getListMusicOffLine() {
        var uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var cursor = homeActivity?.contentResolver?.query(uri, null, null, null, null)
        if (cursor != null && cursor!!.moveToFirst()) {
            var id = cursor!!.getColumnIndex(MediaStore.Audio.Media._ID)
            var title = cursor!!.getColumnIndex(MediaStore.Audio.Media.TITLE)
            var songArtist = cursor!!.getColumnIndex(MediaStore.Audio.Media.ARTIST)

            do {
                val idd = cursor!!.getString(id)
                val currentTT = cursor!!.getString(title)
                val currentArtist = cursor!!.getString(songArtist)
                listt.add(Music(idd, 1, currentTT, currentArtist))
                listMusic.value=listt
                //(homeBinding.rcyListMusic.adapter as MusicAdapter).setListMusic(list)
            } while (cursor!!.moveToNext())
        }
    }
}