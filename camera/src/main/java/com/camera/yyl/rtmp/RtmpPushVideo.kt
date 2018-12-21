package com.camera.yyl.rtmp

class RtmpPushVideo private constructor() : RtmpVideoJNI() {

    companion object {
        val instance = RtmpPushVideo()
    }

    var rtmpConnectListenner:RtmpConnectListenner?=null





    override fun onConnectEvent(action: Int) {
        rtmpConnectListenner?.onConnectEvent(action)
    }
}