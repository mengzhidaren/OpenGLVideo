package com.camera.yyl.encodec

import android.annotation.TargetApi
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import com.camera.yyl.R
import com.ywl5320.libmusic.WlMusic
import com.ywl5320.listener.OnShowPcmDataListener
import com.yyl.demo.utils._i
import kotlinx.android.synthetic.main.activity_video.*

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class VideoRecordActivity : AppCompatActivity() {
    val muisc = WlMusic.getInstance()
    var mediaRecord: MediaRecord? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        muisc.setOnPreparedListener {
            muisc.playCutAudio(39, 60)
        }
        muisc.setOnCompleteListener {
            mediaRecord?.stopRecord()
            mediaRecord = null
            runOnUiThread {
                startRecord.text = "开始录制--"
            }

        }

        muisc.setOnShowPcmDataListener(object : OnShowPcmDataListener {
            override fun onPcmData(pcmdata: ByteArray?, size: Int, clock: Long) {
                mediaRecord?.putPCMData(pcmdata, size)
            }

            override fun onPcmInfo(samplerate: Int, bit: Int, channels: Int) {
                mediaRecord = MediaRecord(this@VideoRecordActivity, cameraView.getFboTextureId())
                mediaRecord?.initEncodec(cameraView.eglContext(), filePath, 720, 1280, samplerate, channels)
                mediaRecord?.onMediaInfoListener=object :OnMediaInfoListener{
                    override fun onMediaTime(times: Long) {
                        _i("onMediaTime   times=$times")
                    }
                }
                mediaRecord?.startRecord()
            }
        })

        muisc.setCallBackPcmData(true)
    }

    val filePath = Environment.getExternalStorageDirectory().absolutePath + "/yylVideo.mp4"
    //音乐文件地址
    val pcmPath = Environment.getExternalStorageDirectory().absolutePath + "/the_girl.m4a"

    fun record(v: View) {
        if (mediaRecord == null) {
            muisc.source=pcmPath
            muisc.prePared()
            startRecord.text = "停止 " + cameraView.getFboTextureId()
        } else {
            muisc.stop()
            mediaRecord?.stopRecord()
            mediaRecord = null
            startRecord.text = "开始录制 00"
        }

    }


}
