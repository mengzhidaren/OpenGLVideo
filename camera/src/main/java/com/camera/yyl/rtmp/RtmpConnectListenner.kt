package com.camera.yyl.rtmp

interface RtmpConnectListenner {
    companion object {
        const val ready = 0
        const val success = 1
        const val error = 2
    }


    fun onConnectEvent(action: Int)

}