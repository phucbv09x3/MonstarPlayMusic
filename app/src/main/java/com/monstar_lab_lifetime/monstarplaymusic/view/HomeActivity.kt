package com.monstar_lab_lifetime.monstarplaymusic.view

import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.monstar_lab_lifetime.monstarplaymusic.R
import com.monstar_lab_lifetime.monstarplaymusic.Interface.OnClickItem
import com.monstar_lab_lifetime.monstarplaymusic.adapter.MusicAdapter
import com.monstar_lab_lifetime.monstarplaymusic.databinding.ActivityHomeBinding
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.service.MusicService
import com.monstar_lab_lifetime.monstarplaymusic.viewmodel.MusicViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_music.*
import java.io.IOException


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

    companion object {
        const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    }

    private lateinit var musicViewModel: MusicViewModel
    private lateinit var intentFil: IntentFilter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startStarservice()
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
        onNewIntent(intent)

        Log.d("cre", "cret")

    }
//
//    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
//        super.onSaveInstanceState(outState, outPersistentState)
//        outState.putString("name", tv_nameMusicShow.text.toString())
//    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mConnection)
        isCheckBoundService = false
        Log.d("destroy", "destroy")
    }

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var ac = intent?.action
            var count = 0
            when (ac) {
                MusicService.ACTION_CLOSE -> {
                    if (isCheckBoundService) {
                        if (mMusicService?.getMusicManager()!!.isPlaying()) {
                            mMusicService?.stopMusic(mMusic!!)
                        }

                        // Toast.makeText(context,intent?.extras?.getString("re"), Toast.LENGTH_SHORT).show()
                        btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    } else {

                    }

                }
                MusicService.ACTION_NEXT -> {

                    if (mMusicService?.getMusicManager()?.mMediaPlayer!!.isPlaying) {
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
//                    mPosition += 1
//                    mMusicService?.playMusic(mListPlay[mPosition])
//                    tv_nameMusicShow.text = mListPlay[mPosition].nameMusic
//                    tv_nameSingerShow.text = mListPlay[mPosition].nameSinger
//                    btn_play.setImageResource(R.drawable.ic_baseline_pause_24)

                }
                MusicService.ACTION_PLAY -> {
                    mMusicService?.getMusicManager()?.mMediaPlayer?.let {
                        if (it.isPlaying) {
                            btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                            mMusicService?.let {
                                it.pauseMusic(mMusic!!)
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
//                    if (mMusicService?.getMusicManager()?.mMediaPlayer!!.isPlaying) {
//                        mMusicService?.pauseMusic(mListPlay[mPosition])
//                        btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
//                    } else {
//                        mMusicService?.continuePlayMusic(mListPlay[mPosition])
//                        tv_nameMusicShow.text = mListPlay[mPosition].nameMusic
//                        tv_nameSingerShow.text = mListPlay[mPosition].nameSinger
//                        btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
//                        Log.d("log", "pause")
//
//                    }
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
//                    mPosition -= 1
//                    if (mPosition == 0) {
//                        mPosition = 0
//                        mMusicService?.playMusic(mListPlay[0])
//                        tv_nameMusicShow.text = mListPlay[0].nameMusic
//                        tv_nameSingerShow.text = mListPlay[0].nameSinger
//                        btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
//                    } else {
//                        mMusicService?.playMusic(mListPlay[mPosition])
//                        tv_nameMusicShow.text = mListPlay[mPosition].nameMusic
//                        tv_nameSingerShow.text = mListPlay[mPosition].nameSinger
//                        btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
//                    }


                }
            }
        }
    }

    private fun startStarservice() {
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
        this.mMusic = music
        this.mPosition = position
        show_play.visibility = View.VISIBLE
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
        mMusicService?.getMusicManager()?.durationMusic?.observe(this, Observer {
            // Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            this.mTimeMusicIsRunning = it
            var minute = it.toLong() / 1000 / 60
            var second = (it.toLong() / 1000) % 60 as Int
            tv_total_time.text = ("$minute:$second")

        })
        mMusicService?.playMusic(music)

//        Log.d("o",mMusicService?.getMusicManager()?.mMediaPlayer?.toString())
//        mMusicService?.getMusicManager()?.mMediaPlayer?.setOnCompletionListener {
//            btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
//            mMusicService?.playMusic(music)
//        }

        isCheckMusicRunning = true
        initSeekBar()
        runSeekBar()
        btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
    }

    private fun runSeekBar() {
        seekBar_time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mMusicService?.getMusicManager()?.mMediaPlayer?.seekTo(progress)
                }
                var hours: Long = (progress.toLong() / 3600000)
                var minute = (progress.toLong() - (hours * 3600000)) / 60000
                var second = (progress.toLong() - (hours * 3600000) - (minute * 60000)).toString()

                if (second.toInt() < 100) {
                    second = "00"
                    tv_time.text = (minute.toString() + ":" + second)
                }
                if (second.toInt() < 10000) {
                    second = (second.toInt() / 1000).toString()
                    second = "0".plus(second)
                    tv_time.text = (minute.toString() + ":" + second)
                }
                if (second.length < 2) {
                    second = "0"
                } else {
                    second = second.substring(0, 2)
                    tv_time.text = (minute.toString() + ":" + second)
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
                        seekBar_time?.progress = it.currentPosition
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
                // homeBinding.data = musicService!!.getModel()
//                mMusic?.let {
//                    //mMusicService?.playMusic(it)
//                }
                mMusicService?.mu?.observe(this@HomeActivity, Observer {
                    tv_nameMusicShow.text = it.nameMusic
                    tv_nameSingerShow.text = it.nameSinger
                    var minute = it.duration.toLong() / 1000 / 60
                    var second = (it.duration.toLong() / 1000) % 60 as Int
                    tv_total_time.text = ("$minute:$second")
                    mMusicService?.getMusicManager()?.mMediaPlayer?.let {
                        if (it.isPlaying) {
                            btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                        } else {
                            btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                        }
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
//        if (mPosition == mListPlay.size) {
//            mPosition -= 1
//        }
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
                if (mMusicService?.getMusicManager()?.mMediaPlayer == null) {

                } else {
                    if (mMusicService?.getMusicManager()?.mMediaPlayer?.isPlaying!!) {
                        if (mPosition < mListPlay.size - 1) {
                            mPosition += 1
                            btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
                            tv_nameMusicShow.text = mListPlay[mPosition].nameMusic
                            tv_nameSingerShow.text = mListPlay[mPosition].nameSinger
                            mMusicService?.playMusic(mListPlay[mPosition])
                            //startForegroundService(intent)

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
//                mMusicService?.getMusicManager()?.mMediaPlayer?.let {
//                    btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
//                    if (it.isPlaying){
//                        if (mPosition < mListPlay.size-1) {
//                            mPosition += 1
//
//                            tv_nameMusicShow.text = mListPlay[mPosition].nameMusic
//                            tv_nameSingerShow.text = mListPlay[mPosition].nameSinger
//                            mMusicService?.playMusic(mListPlay[mPosition])
//
//                        } else {
//                            Toast.makeText(this, "Không thể next bài", Toast.LENGTH_LONG).show()
//                        }
//                    }else {
//                        Toast.makeText(this, "Không thể next bài", Toast.LENGTH_LONG).show()
//                    }
//                }

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