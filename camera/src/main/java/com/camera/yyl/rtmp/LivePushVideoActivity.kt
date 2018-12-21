package com.camera.yyl.rtmp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.camera.yyl.R
import com.camera.yyl.rtmp.encodec.OnMediaInfoListener
import com.camera.yyl.rtmp.encodec.RtmpRecord
import com.yyl.demo.utils._i
import kotlinx.android.synthetic.main.activity_live_push_video.*

class LivePushVideoActivity : AppCompatActivity() {

    var rtmpRecord: RtmpRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_push_video)
        pushVideo.setOnClickListener {
            startRtmp()
        }
        RtmpPushVideo.instance.rtmpConnectListenner = object : RtmpConnectListenner {
            override fun onConnectEvent(action: Int) {

                when (action) {
                    RtmpConnectListenner.ready -> {
                        _i("onConnectEvent ready")
                        startRecord()
                    }
                    RtmpConnectListenner.success -> {
                        _i("onConnectEvent success")
                    }
                    RtmpConnectListenner.error -> {
                        _i("onConnectEvent error")
                    }
                }

            }
        }


    }

    fun startRecord(){
        rtmpRecord = RtmpRecord(this@LivePushVideoActivity, cameraViewRtmp.getFboTextureId())
        rtmpRecord?.initEncodec(cameraViewRtmp.eglContext(), 720, 1280)
        rtmpRecord?.startRecord()
        rtmpRecord?.onMediaInfoListener=object :OnMediaInfoListener{
            override fun onMediaTime(times: Long) {
            }

            override fun onSPSPPSInfo(sps: ByteArray, pps: ByteArray) {
                RtmpPushVideo.instance.onPushSPSPPS(sps,pps)
            }

            override fun onVideoInfo(data: ByteArray, keyFrame: Boolean) {
                RtmpPushVideo.instance.onPushVideoInfoData(data,keyFrame)
            }

            override fun onAudioInfo(data: ByteArray) {
                RtmpPushVideo.instance.onPushAudioInfoData(data)
            }
        }
    }


    var start = false

    fun startRtmp() {
        start = !start
        if (start) {
            RtmpPushVideo.instance.startPush("http://www.baidu.com")
        } else {
            rtmpRecord?.stopRecord()
            rtmpRecord = null
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        cameraViewRtmp.onDestory()
    }
}
