package com.monstar_lab_lifetime.monstarplaymusic.view

import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.fasterxml.jackson.databind.util.ISO8601Utils.format
import com.monstar_lab_lifetime.monstarplaymusic.R
import com.monstar_lab_lifetime.monstarplaymusic.Interface.OnClickItem
import com.monstar_lab_lifetime.monstarplaymusic.adapter.MusicAdapter
import com.monstar_lab_lifetime.monstarplaymusic.databinding.ActivityHomeBinding
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.service.MusicService
import com.monstar_lab_lifetime.monstarplaymusic.viewmodel.MusicViewModel
import kotlinx.android.synthetic.main.activity_home.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity(), OnClickItem {
    private lateinit var homeBinding: ActivityHomeBinding
    private var musicService: MusicService? = null
    private var mediaPlayer: MediaPlayer? = null
    private var musicManager: MusicManager? = null
    private lateinit var connection: ServiceConnection
    private var listPlay = mutableListOf<Music>()
    private var isCheckService: Boolean = false
    private var isCheckMusicRunning: Boolean = false

    companion object {
        const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    }

    private lateinit var musicViewModel: MusicViewModel
    private var list = mutableListOf<Music>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startStarservice()
        createConnection()
        isCheckMusicRunning=false
        homeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        homeBinding.rcyListMusic.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = MusicAdapter(this@HomeActivity)
        }
        musicViewModel = MusicViewModel()
        homeBinding.lifecycleOwner = this
        requestRead()
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
        getListOff()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getListOff()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getListOff() {
        musicViewModel.getListMusicOffLine(contentResolver)
        musicViewModel.listMusic.observe(this, Observer<MutableList<Music>> {
            listPlay = it
            (homeBinding.rcyListMusic.adapter as MusicAdapter).setListMusic(it)
        })
    }


    private var m: Music? = null
    override fun clickItem(music: Music, position: Int) {
        this.m = music
        show_play.visibility = View.VISIBLE
        tv_nameMusicShow.text = music.nameMusic
        tv_nameSingerShow.text = music.nameSinger
        var hours: Long = (music.duration.toLong() / 3600000)
        var minute = (music.duration.toLong() - (hours * 3600000)) / 60000
        var second = (music.duration.toLong() - (hours * 3600000) - (minute * 60000)).toString()
        if (second.length < 2) {
            second = "00"
        } else {
            second = second.substring(0, 2)
            tv_total_time.setText(minute.toString() + ":" + second)
        }

        val countDownTimer = object : CountDownTimer(8000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var text = millisUntilFinished / 1000
                if (text.toInt() == 0) {
                    // show_play.visibility = View.GONE
                }
            }

            override fun onFinish() {
            }
        }
        //countDownTimer.start()
        var count = 0

        btn_play.setOnClickListener {
            isCheckMusicRunning = true
            initSeekbar()
            count++
            if (count == 1) {
                btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                musicService?.play(music)
            }
            if (count % 2 == 1 && count > 1) {
                btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                musicService?.tieptuc(music)
            }
            if (count % 2 == 0 && count > 0) {
                btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                musicService?.pause(music)
            }
        }
        var p :Int= position
        btn_next.setOnClickListener {
            p += 1
            if (isCheckMusicRunning == true) {
                if (p < listPlay.size) {
                    btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                    tv_nameMusicShow.text = listPlay[p].nameMusic
                    tv_nameSingerShow.text = listPlay[p].nameSinger
                    musicService?.play(listPlay[p])
                    Toast.makeText(this, p.toString(), Toast.LENGTH_LONG).show()

                }
                if (listPlay.size<p) {
                    Toast.makeText(this, "Đã hết danh sách", Toast.LENGTH_LONG).show()
                }
            }
            if (isCheckMusicRunning==false){
                Toast.makeText(this, "Không thể next bài", Toast.LENGTH_LONG).show()
            }
        }
        p -= 1
      
        btn_previous.setOnClickListener {

            if (isCheckMusicRunning == true) {
                if (-1<p&&p <= listPlay.size) {
                    btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                    tv_nameMusicShow.text = listPlay[p].nameMusic
                    tv_nameSingerShow.text = listPlay[p].nameSinger
                    musicService?.play(listPlay[p])
                    Toast.makeText(this, p.toString(), Toast.LENGTH_LONG).show()
                }
//                if (p==-1){
//
//                    Toast.makeText(this, "Không còn bài để back", Toast.LENGTH_LONG).show()
//                }
            }
            if (isCheckMusicRunning==false){
                Toast.makeText(this, "Không thể back bài", Toast.LENGTH_LONG).show()
            }
        }
        seekBar_time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

    }

    private fun initSeekbar() {
        mediaPlayer?.let {
            seekBar_time.max = it.duration
            val handler = Handler()
            handler.postDelayed(object : Runnable {
                override fun run() {
                    try {
                        seekBar_time.progress = mediaPlayer!!.currentPosition
                        handler.postDelayed(this, 1000)
                    } catch (ex: IOException) {

                        seekBar_time.progress = 0
                    }
                }

            }, 0)
        }


    }


    private fun createConnection() {
        //tao cau connection
        connection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {

                isCheckService = false
            }

            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                isCheckService = true
                musicService = (service as MusicService.MyBinder).getService
               // homeBinding.data = musicService!!.getModel()
                m?.let {
                    musicService?.play(it)
                }

                // register()
            }
        }
        //tao ket noi
        val intent = Intent()
        intent.setClass(this, MusicService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun startStarservice() {
        val intent = Intent()
        intent.setClass(this, MusicService::class.java)
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

}