package com.monstar_lab_lifetime.monstarplaymusic.viewmodel

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.view.HomeActivity
import java.util.Collections.list

class MusicViewModel : ViewModel() {
    private  var homeActivity: HomeActivity?=null
    var currentTT: String = ""
    var listMusic = MutableLiveData<MutableList<Music>>()
    var listt = mutableListOf<Music>()
    var cursor: Cursor? = null
    fun getListMusicOffLine() {
        var uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        cursor = homeActivity?.contentResolver?.query(uri, null, null, null, null)
        if (cursor != null && cursor!!.moveToFirst()) {
            var title = cursor!!.getColumnIndex(MediaStore.Audio.Media.TITLE)
            var songArtist = cursor!!.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            do {
                currentTT = cursor!!.getString(title)
                var currentArtist = cursor!!.getString(songArtist)
                listt.add(Music(1, currentTT, currentArtist))
                listMusic.value = listt
            } while (cursor!!.moveToNext())
        }

    }
    /*ArrayList<HashMap<String,String>> getPlayList(String rootPath) {
            ArrayList<HashMap<String,String>> fileList = new ArrayList<>();


            try {
                File rootFolder = new File(rootPath);
                File[] files = rootFolder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
                for (File file : files) {
                    if (file.isDirectory()) {
                        if (getPlayList(file.getAbsolutePath()) != null) {
                            fileList.addAll(getPlayList(file.getAbsolutePath()));
                        } else {
                            break;
                        }
                    } else if (file.getName().endsWith(".mp3")) {
                        HashMap<String, String> song = new HashMap<>();
                        song.put("file_path", file.getAbsolutePath());
                        song.put("file_name", file.getName());
                        fileList.add(song);
                    }
                }
                return fileList;
            } catch (Exception e) {
                return null;
            }
        }*/
}