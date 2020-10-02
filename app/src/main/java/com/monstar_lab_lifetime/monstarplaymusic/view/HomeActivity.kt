package com.monstar_lab_lifetime.monstarplaymusic.view

import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
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
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity(),OnClickItem {
    private lateinit var homeBinding: ActivityHomeBinding
    private var musicService:MusicService?=null
    private lateinit var connection:ServiceConnection
    private var listPlay= mutableListOf<Music>()
    private var isCheck:Boolean=true
    companion object {
        const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    }
    private lateinit var musicViewModel: MusicViewModel
    private var list = mutableListOf<Music>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCheck=true
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
    private fun getListOff(){
        musicViewModel.getListMusicOffLine(contentResolver)
        musicViewModel.listMusic.observe(this, Observer<MutableList<Music>> {
            listPlay=it
            (homeBinding.rcyListMusic.adapter as MusicAdapter).setListMusic(it)
        })
    }
    fun createConn(){
        connection=object : ServiceConnection{
            override fun onServiceDisconnected(name: ComponentName?) {
            }
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                musicService = (service as MusicService.MyBinder).musicService

                homeBinding.data = musicService!!.getModel()
            }

        }
    }

    override fun clickItem(music: Music, position: Int) {
        isCheck=false
        show_play.visibility= View.VISIBLE
        tv_nameMusicShow.text=music.nameMusic
        tv_nameSingerShow.text=music.nameSinger
        var hours:Long=(music.duration.toLong()/3600000)
        var minute=(music.duration.toLong()-(hours*3600000))/60000
        var second=(music.duration.toLong() - (hours * 3600000) - (minute * 60000)).toString()
        if (second.length<2){
            second="00"
        }
        else{
            second = second.substring(0, 2)
            tv_total_time.setText(minute.toString()+":"+second)
        }

        val countDownTimer=object :CountDownTimer(8000,1000){
            override fun onTick(millisUntilFinished: Long) {
                var text=millisUntilFinished/1000
                if (text.toInt()==0){
                    show_play.visibility=View.GONE
                }
            }
            override fun onFinish() {
            }
        }
        countDownTimer.start()
        var count=0
        btn_play.setOnClickListener {
            count++
            if (count%2==1){
                btn_play.setImageResource(R.drawable.ic_baseline_pause_24)
            }else{
                btn_play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
            isCheck=false
            val intent=Intent(this,MusicService::class.java)
            var bundle=Bundle()
            bundle.putString("uri",music.uri)
            bundle.putString("name",music.nameSinger)
            intent.putExtras(bundle)
            // bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
            startService(intent)
        }
        seekBar_time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                TODO("Not yet implemented")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                TODO("Not yet implemented")
            }

        })


    }


    private fun createConnection() {
        //tao cau connection
        connection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {

            }

            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                musicService =
                    (service as MusicService.MyBinder).musicService

                homeBinding.data = musicService!!.getModel()

            }
        }
        //tao ket noi
        val intent = Intent()
        intent.setClass(this, MusicService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }
    private fun startStarservice(){
        val intent = Intent()
        intent.setClass(this, MusicService::class.java)
        startService(intent)
    }


}