package com.monstar_lab_lifetime.monstarplaymusic.view

import android.app.Notification
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.monstar_lab_lifetime.monstarplaymusic.R
import com.monstar_lab_lifetime.monstarplaymusic.Interface.OnClickItem
import com.monstar_lab_lifetime.monstarplaymusic.adapter.MusicAdapter
import com.monstar_lab_lifetime.monstarplaymusic.broadcast.NotificationReceiver
import com.monstar_lab_lifetime.monstarplaymusic.databinding.ActivityHomeBinding
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.service.MusicService
import com.monstar_lab_lifetime.monstarplaymusic.viewmodel.MusicViewModel
import kotlinx.android.synthetic.main.activity_home.*
import java.io.IOException


class HomeActivity : AppCompatActivity(), OnClickItem, View.OnClickListener {
    private var homeBinding: ActivityHomeBinding? = null
    private var mMusicService: MusicService? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mMusicManager: MusicManager? = null
    private lateinit var mConnection: ServiceConnection
    private var mListPlay = mutableListOf<Music>()
    private var isCheckService: Boolean = false
    private var isCheckMusicRunning: Boolean = false
    private var mMusic: Music? = null
    private var mPosition: Int = 0
    private var mCount = 0
    private var mTimeMusicIsRunning = 0

    companion object {
        const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    }


    private lateinit var musicViewModel: MusicViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //startStarservice()
        createConnection()
        isCheckMusicRunning = false
        homeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        homeBinding!!.rcyListMusic.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = MusicAdapter(this@HomeActivity)
        }
        musicViewModel = MusicViewModel()
        homeBinding!!.lifecycleOwner = this
        requestRead()
        clicks()
        broad()


    }

    fun broad() {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent!!.extras?.getString("actionname")
                when (action) {
                    MusicService.ACTION_PREVIOUS -> {
                        Toast.makeText(context, "hello", Toast.LENGTH_LONG).show()
                    }
                    MusicService.ACTION_NEXT -> {

                    }
                    MusicService.ACTION_PLAY -> {

                    }
                }
            }


        }
        registerReceiver(broadcastReceiver, IntentFilter("ACTION"))
    }

    private fun startStarservice() {
        val intent = Intent()
        intent.setClass(this, MusicService::class.java)
        startService(intent)
    }

    private fun clicks() {
        btn_play.setOnClickListener(this)
        btn_next.setOnClickListener(this)
        btn_previous.setOnClickListener(this)
    }

    private fun requestRead() = if (ContextCompat.checkSelfPermission(
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
            mListPlay = it
            (homeBinding!!.rcyListMusic.adapter as MusicAdapter).setListMusic(it)
        })
    }

    override fun clickItem(music: Music, position: Int) {
        this.mMusic = music
        initSeekBar()
        this.mPosition = position
        show_play.visibility = View.VISIBLE
        btn_showPlay.visibility = View.GONE
        tv_nameMusicShow.text = music.nameMusic
        tv_nameSingerShow.text = music.nameSinger
        val countDownTimer = object : CountDownTimer(8000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var text = millisUntilFinished / 1000
                if (text.toInt() == 0) {
                    show_play.visibility = View.GONE
                    btn_showPlay.visibility = View.VISIBLE
                }
            }

            override fun onFinish() {
            }
        }
        countDownTimer.start()
        btn_showPlay.setOnClickListener {
            countDownTimer.start()
            show_play.visibility = View.VISIBLE
            btn_showPlay.visibility = View.GONE
        }
        isCheckMusicRunning = true
        mMusicService?.playMusic(music)

        mMusicService?.getMusicManager()?.durationMusic!!.observe(this, Observer {
            this.mTimeMusicIsRunning = it
            var hours: Long = (it.toLong() / 3600000)
            var minute = (it.toLong() - (hours * 3600000)) / 60000
            var second = (it.toLong() - (hours * 3600000) - (minute * 60000)).toString()
            if (second.length < 2) {
                second = "00"
            } else {
                second = second.substring(0, 2)
                tv_total_time.setText(minute.toString() + ":" + second)
            }
        })
        seekBar_time.max=mTimeMusicIsRunning
        seekBar_time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var hours: Long = (progress.toLong() / 3600000)
                var minute = (progress.toLong() - (hours * 3600000)) / 60000
                var second = (progress.toLong() - (hours * 3600000) - (minute * 60000)).toString()

                    tv_time.setText(minute.toString() + ":" + second)


                //Toast.makeText(applicationContext,progress.toString(),Toast.LENGTH_LONG).show()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

//        mMusicService?.getMusicManager()?.currentPosition!!.observe(this, Observer {
//            Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
//        })
        // initSeekBar()
        btn_play.setImageResource(R.drawable.ic_baseline_pause_24)


    }

    private fun initSeekBar() {
        mMusicService?.getMusicManager()?.mMediaPlayer.let {
            seekBar_time.max = mTimeMusicIsRunning
            val handler = Handler()
            handler.postDelayed(object : Runnable {
                override fun run() {
                    try {
                        seekBar_time.progress =
                            mMusicService?.getMusicManager()?.mMediaPlayer!!.currentPosition
                        handler.postDelayed(this, 1000)

                    } catch (ex: IOException) {
                        seekBar_time.progress = 0
                    }
                }

            }, 0)


        }


    }


    private fun createConnection() {
        mConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                isCheckService = false
            }

            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                isCheckService = true
                mMusicService = (service as MusicService.MyBinder).getService
                // homeBinding.data = musicService!!.getModel()
//                mMusic?.let {
//                    //mMusicService?.playMusic(it)
//                }

            }
        }
        val intent = Intent()
        intent.setClass(this, MusicService::class.java)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }


    override fun onDestroy() {
        super.onDestroy()
        unbindService(mConnection)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_play -> {
                mCount++
                if (mCount % 2 == 1) {
                    btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    mMusicService?.pauseMusic(mMusic!!)
                }
                if (mCount % 2 == 0) {
                    btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                    mMusicService?.continuePlayMusic(mMusic!!)
                }


            }

            R.id.btn_next -> {
                mPosition += 1
                //Toast.makeText(this,mPosition.toString(),Toast.LENGTH_LONG).show()
                if (isCheckMusicRunning) {
                    if (mPosition < mListPlay.size) {
                        btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                        tv_nameMusicShow.text = mListPlay[mPosition].nameMusic
                        tv_nameSingerShow.text = mListPlay[mPosition].nameSinger
                        mMusicService?.playMusic(mListPlay[mPosition])

                    } else {
                        Toast.makeText(this, "Không thể next bài", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Không thể next bài", Toast.LENGTH_LONG).show()
                }
            }
            R.id.btn_previous -> {
                mPosition -= 1
                if (isCheckMusicRunning) {
                    if (mListPlay.size > mPosition && mPosition >= 0) {
                        btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                        tv_nameMusicShow.text = mListPlay[mPosition]!!.nameMusic
                        tv_nameSingerShow.text = mListPlay[mPosition]!!.nameSinger
                        mMusicService?.playMusic(mListPlay[mPosition])
                    }

                } else {
                    Toast.makeText(this, "Không thể back bài", Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}