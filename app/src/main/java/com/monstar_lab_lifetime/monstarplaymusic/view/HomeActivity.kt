package com.monstar_lab_lifetime.monstarplaymusic.view

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.monstar_lab_lifetime.monstarplaymusic.R
import com.monstar_lab_lifetime.monstarplaymusic.Interface.OnClickItem
import com.monstar_lab_lifetime.monstarplaymusic.adapter.MusicAdapter
import com.monstar_lab_lifetime.monstarplaymusic.databinding.ActivityHomeBinding
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.service.MusicService
import com.monstar_lab_lifetime.monstarplaymusic.viewmodel.MusicViewModel


class HomeActivity : AppCompatActivity(),OnClickItem {
    private lateinit var homeBinding: ActivityHomeBinding

    companion object {
        const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    }
    private lateinit var musicViewModel: MusicViewModel
//    private var requestCode = 1000
//    private var musicService: MusicService? = null
//    private lateinit var conn: ServiceConnection
    private var list = mutableListOf<Music>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        homeBinding.rcyListMusic.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = MusicAdapter(this@HomeActivity)
        }
        musicViewModel = MusicViewModel()
        homeBinding.lifecycleOwner = this

        requestRead()
        //  Toast.makeText(this,musicViewModel.currentTT.toString(),Toast.LENGTH_SHORT).show()

    }

    fun requestRead() = if (ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
        )
    } else {
        get()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                get()
            } else {
                // Permission Denied
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    fun get() {
        var uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var cursor = this?.contentResolver?.query(uri, null, null, null, null)
        if (cursor != null && cursor!!.moveToFirst()) {
            var id=cursor!!.getColumnIndex(MediaStore.Audio.Media._ID)
            var title = cursor!!.getColumnIndex(MediaStore.Audio.Media.TITLE)
            var songArtist = cursor!!.getColumnIndex(MediaStore.Audio.Media.ARTIST)
//            var uri = cursor!!.getColumnIndex(
//                MediaStore.Audio.Media.getContentUri(MediaStore.Audio.Media.DATA).toString()
//            )
            do {
                val idd=cursor!!.getString(id)
                val currentTT = cursor!!.getString(title)
                val currentArtist = cursor!!.getString(songArtist)
                list.add(Music(idd,1, currentTT, currentArtist))
                (homeBinding.rcyListMusic.adapter as MusicAdapter).setListMusic(list)
            } while (cursor!!.moveToNext())
        }


    }

//    private fun getListMusicOff() {
//       // musicViewModel.getListMusicOffLine()
//        musicViewModel.listMusic.observe(this, Observer<MutableList<Music>> {
//            it?.let {
//                (homeBinding.rcyListMusic.adapter as MusicAdapter).setListMusic(it)
//            }
//            Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
//            //(homeBinding.rcyListMusic.adapter as MusicAdapter).setListMusic(it)
//        })
//    }

    override fun onStart() {
        super.onStart()

    }

    override fun clickItem(music: Music, position: Int) {
        startService(Intent(this, MusicService::class.java))
    }
}