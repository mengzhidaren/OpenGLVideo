package com.camera.yyl.imagevideo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.camera.yyl.R
import com.camera.yyl.encodec.MediaRecord
import com.ywl5320.libmusic.WlMusic
import com.ywl5320.listener.OnShowPcmDataListener
import kotlinx.android.synthetic.main.activity_image_video.*

class ImageVideoActivity : AppCompatActivity() {
    var imgChange = false

    val muisc = WlMusic.getInstance()

    //音乐文件地址
    val pcmPath = Environment.getExternalStorageDirectory().absolutePath + "/the_girl.m4a"
    //保存地址
    val filePath = Environment.getExternalStorageDirectory().absolutePath + "/yylVideoImg.mp4"
    var mediaRecord: MediaRecord? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_video)


        change.setOnClickListener {

            imgChange = !imgChange
            imgVideoView.setCurrentImg(if (imgChange) R.drawable.demo1 else R.drawable.demo2)
        }
        record.setOnClickListener {
            muisc.source = pcmPath
            muisc.prePared()
        }

        muisc.setOnPreparedListener {
            muisc.playCutAudio(0, 60)
        }
        muisc.setOnShowPcmDataListener(object : OnShowPcmDataListener {
            override fun onPcmData(pcmdata: ByteArray?, size: Int, clock: Long) {
                mediaRecord?.putPCMData(pcmdata, size)
            }

            override fun onPcmInfo(samplerate: Int, bit: Int, channels: Int) {
                mediaRecord = MediaRecord(this@ImageVideoActivity, imgVideoView.getFboTextureId())
                mediaRecord?.initEncodec(imgVideoView.eglContext(), filePath, 720, 500, samplerate, channels)
                mediaRecord?.startRecord()
                startImgs()
            }
        })

//        startImgs()
        muisc.setCallBackPcmData(true)

        imgVideoView.setCurrentImg(R.drawable.demo1)
    }


    private fun startImgs() {
        Thread(Runnable {
            for (i in 1..20) {
                val index = i % 3 + 1
                val imgsrc = resources.getIdentifier("demo$index", "drawable", "com.camera.yyl")
                imgVideoView.setCurrentImg(imgsrc)
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
            mediaRecord?.apply {
                muisc.stop()
                stopRecord()

            }
        }).start()
    }

}
