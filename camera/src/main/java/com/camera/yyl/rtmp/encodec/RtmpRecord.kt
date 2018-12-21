package com.camera.yyl.rtmp.encodec

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.camera.yyl.egl.EGLThread.Companion.RENDERMODE_CONTINUOUSLY

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class RtmpRecord(val context: Context, val textureId:Int): BaseRtmpEncoder() {


    val encodecRender= RtmpRenderRecord(context, textureId)


    init {
        render=encodecRender
        renderMode= RENDERMODE_CONTINUOUSLY

    }

}