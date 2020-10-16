package com.monstar_lab_lifetime.monstarplaymusic.view

import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.monstar_lab_lifetime.monstarplaymusic.Interface.OnClickItem
import com.monstar_lab_lifetime.monstarplaymusic.R
import com.monstar_lab_lifetime.monstarplaymusic.adapter.MusicAdapter
import com.monstar_lab_lifetime.monstarplaymusic.databinding.ActivityHomeBinding
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.service.MusicService
import com.monstar_lab_lifetime.monstarplaymusic.viewmodel.MusicViewModel
import kotlinx.android.synthetic.main.activity_home.*
import java.io.IOException
import java.text.SimpleDateFormat


class HomeActivity : AppCompatActivity(), OnClickItem, View.OnClickListener {
    private var homeBinding: ActivityHomeBinding? = null
    private var mMusicService: MusicService? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mMusicManager: MusicManager? = null
    private lateinit var mConnection: ServiceConnection
    private var mListPlay = mutableListOf<Music>()
    private var isCheckBoundService: Boolean = false
    private var isCheckMusicRunning: Boolean = false
    private var mMusic: Music? = null
    private var mPosition: Int = 0
    private var mCount = 0
    private var mTimeMusicIsRunning = 0
    private lateinit var musicViewModel: MusicViewModel
    private lateinit var intentFil: IntentFilter

    companion object {
        const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService()
        homeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        homeBinding!!.rcyListMusic.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = MusicAdapter(this@HomeActivity)
        }
        musicViewModel = MusicViewModel()
        homeBinding?.lifecycleOwner = this
        requestReadListMusicOffline()
        clicks()
        intentFil = IntentFilter()
        intentFil.addAction(MusicService.ACTION_CLOSE)
        intentFil.addAction(MusicService.ACTION_NEXT)
        intentFil.addAction(MusicService.ACTION_PLAY)
        intentFil.addAction(MusicService.ACTION_PREVIOUS)
        registerReceiver(broadcastReceiver, intentFil)
        createConnection()

    }


    private var broadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            var ac = intent?.action
            if (mMusicService?.getMusicManager()?.mMediaPlayer?.isPlaying == true) {
                btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
            when (ac) {
                MusicService.ACTION_CLOSE -> {
                    val notificationManager = getSystemService(
                        NOTIFICATION_SERVICE
                    ) as NotificationManager
                    // notificationManager.cancel(10)
                    notificationManager.deleteNotificationChannel("MusicService")

                    stopSV()
                    btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    if (isCheckBoundService) {
                        unbindService(mConnection)
                        //stopSV()
                    }
                }
                MusicService.ACTION_NEXT -> {
                    if (mMusicService?.getMusicManager()?.isPlaying() == true) {
                        if (mPosition < mListPlay.size - 1) {
                            mPosition += 1
                            btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                            tv_nameMusicShow.text = mListPlay[mPosition].nameMusic
                            tv_nameSingerShow.text = mListPlay[mPosition].nameSinger
                            mMusicService?.playMusic(mListPlay[mPosition])

                        } else {
                            Toast.makeText(
                                this@HomeActivity,
                                "Không thể next bài",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(this@HomeActivity, "Không thể next bài", Toast.LENGTH_LONG)
                            .show()
                    }

                }
                MusicService.ACTION_PLAY -> {
//                    if (mMusicService?.getMusicManager()?.isPlaying()==true){
//
//                    }
                    Log.d("noti", mMusicService?.getMusicManager()?.isPlaying().toString())
                    mMusicService?.getMusicManager()?.mMediaPlayer?.let {
                        if (it.isPlaying) {
                            btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                            mMusicService?.let { it ->
                                mMusic?.let { itt ->
                                    it.pauseMusic(itt)
                                }
                            }
                        } else {
                            btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                            mMusicService?.let { it ->
                                mMusic?.let { itt ->
                                    it.continuePlayMusic(itt)
                                }

                            }

                        }
                    }
                }
                MusicService.ACTION_PREVIOUS -> {
                    mMusicService?.getMusicManager()?.mMediaPlayer?.let {
                        if (it.isPlaying) {
                            if (mListPlay.size > mPosition && mPosition >= 1) {
                                mPosition -= 1
                                btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                                tv_nameMusicShow.text = mListPlay[mPosition]!!.nameMusic
                                tv_nameSingerShow.text = mListPlay[mPosition]!!.nameSinger
                                mMusicService?.playMusic(mListPlay[mPosition])
                            } else {
                                Toast.makeText(
                                    this@HomeActivity,
                                    "Không thể back bài",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@HomeActivity,
                                "Không thể back bài",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }


    private fun stopSV() {
        val intent = Intent()
        intent.setClass(applicationContext, MusicService::class.java)
        stopService(intent)
    }

    private fun startService() {
        val intent = Intent()
        intent.setClass(applicationContext, MusicService::class.java)
        startService(intent)
    }

    private fun clicks() {
        btn_play.setOnClickListener(this)
        btn_next.setOnClickListener(this)
        btn_previous.setOnClickListener(this)
    }

    private fun requestReadListMusicOffline() = if (ContextCompat.checkSelfPermission(
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
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
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
        //this.mMusic = music
        this.mPosition = position

        mMusicService?.playMusic(music)
        if (mMusicService?.getMusicManager()?.isPlaying() == true) {
            btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
        }

        isCheckMusicRunning = true
        initSeekBar()
        runSeekBar()
        loopMusic()

    }

    private fun loopMusic(){
        mMusicService?.getMusicManager()?.mMediaPlayer?.setOnCompletionListener(object :MediaPlayer.OnCompletionListener{
            override fun onCompletion(mp: MediaPlayer?) {
                mMusicService?.playMusic(mMusic!!)
            }

        })
    }
    private fun showPlayOnTimeCountDown(music: Music) {
        // show_play.visibility = View.VISIBLE
        //btn_showPlay.visibility = View.GONE
        tv_nameMusicShow.text = music.nameMusic
        tv_nameSingerShow.text = music.nameSinger
        val countDownTimer = object : CountDownTimer(8000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var text = millisUntilFinished / 1000
                if (text.toInt() == 0) {
                    show_play.visibility = View.VISIBLE
                    btn_showPlay.visibility = View.VISIBLE
                }
            }

            override fun onFinish() {
            }
        }
        // countDownTimer.start()
//        btn_showPlay.setOnClickListener {
//          //  countDownTimer.start()
//            show_play.visibility = View.VISIBLE
//            btn_showPlay.visibility = View.GONE
//        }
    }

    private fun runSeekBar() {
        seekBar_time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mMusicService?.getMusicManager()?.mMediaPlayer?.seekTo(seekBar!!.progress)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }

    private fun initSeekBar() {
        seekBar_time.max = mTimeMusicIsRunning
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    mMusicService?.getMusicManager()?.mMediaPlayer?.let {
                        seekBar_time!!.progress = it.currentPosition
                        val fm = SimpleDateFormat("mm:ss")
                        val time = fm.format(it.currentPosition)
                        tv_time.text = time
                        handler.postDelayed(this, 1000)
                    }

                } catch (ex: IOException) {
                    seekBar_time.progress = 0
                }
            }

        }, 0)
    }

    private fun createConnection() {
        mConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                isCheckBoundService = false
            }

            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                mMusicService = (service as MusicService.MyBinder).getService
                isCheckBoundService = true
                mMusicService?.musicFromService?.observe(this@HomeActivity, Observer {
                    mMusic = it
                    tv_nameMusicShow.text = it.nameMusic
                    tv_nameSingerShow.text = it.nameSinger
                    val fm = SimpleDateFormat("mm:ss")
                    val time = fm.format(it.duration.toInt())
                    tv_total_time.text = time
                    mTimeMusicIsRunning = it.duration.toInt()
                    if (mMusicService?.getMusicManager()?.mMediaPlayer?.isPlaying == true) {
                        btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                    }
                })
                initSeekBar()
                runSeekBar()
            }
        }
        val intent = Intent()
        intent.setClass(this, MusicService::class.java)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_play -> {

                mMusicService?.getMusicManager()?.mMediaPlayer?.let { it ->

                    if (it.isPlaying) {
                        btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                        mMusicService?.let { it ->
                            mMusic?.let { itt ->
                                it.pauseMusic(itt)
                            }
                        }
                    } else {
                        btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                        mMusicService?.let { it ->
                            mMusic?.let { itt ->
                                it.continuePlayMusic(itt)
                            }
                        }

                    }
                }
            }

            R.id.btn_next -> {
                mMusicService?.getMusicManager()?.mMediaPlayer?.let {
                    if (it.isPlaying) {
                        btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                        if (mPosition < mListPlay.size - 1) {
                            mPosition += 1

                            tv_nameMusicShow.text = mListPlay[mPosition].nameMusic
                            tv_nameSingerShow.text = mListPlay[mPosition].nameSinger
                            mMusicService?.playMusic(mListPlay[mPosition])
                        } else {
                            Toast.makeText(
                                this@HomeActivity,
                                "Không thể next bài",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@HomeActivity,
                            "Không thể next bài",
                            Toast.LENGTH_LONG
                        ).show()
                    }


                }
            }
            R.id.btn_previous -> {
                mMusicService?.getMusicManager()?.mMediaPlayer?.let {
                    if (it.isPlaying) {
                        if (mListPlay.size > mPosition && mPosition >= 1) {
                            mPosition -= 1
                            btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                            tv_nameMusicShow.text = mListPlay[mPosition]!!.nameMusic
                            tv_nameSingerShow.text = mListPlay[mPosition]!!.nameSinger
                            mMusicService?.playMusic(mListPlay[mPosition])
                        } else {
                            Toast.makeText(this, "Không thể back bài", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "Không thể back bài", Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
    }
}