package com.camera.yyl

import android.Manifest.permission.*
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.camera.yyl.audio_record.AudioRecordActivity
import com.camera.yyl.camera.CameraActivity
import com.camera.yyl.encodec.VideoRecordActivity
import com.camera.yyl.imagevideo.ImageVideoActivity
import com.camera.yyl.rtmp.LivePushVideoActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val cameraMisson = CAMERA
    val sdCard = WRITE_EXTERNAL_STORAGE
    val audio = RECORD_AUDIO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(cameraMisson, sdCard,audio), 11)
        }
        camera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        record.setOnClickListener {
            startActivity(Intent(this, VideoRecordActivity::class.java))
        }
        imgVideo.setOnClickListener {
            startActivity(Intent(this, ImageVideoActivity::class.java))
        }
        audioRecord.setOnClickListener {
            startActivity(Intent(this, AudioRecordActivity::class.java))
        }
        liveVideo.setOnClickListener {
            startActivity(Intent(this, LivePushVideoActivity::class.java))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }
}
