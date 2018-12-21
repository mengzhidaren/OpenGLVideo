package com.camera.yyl.encodec

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.camera.yyl.egl.EGLThread.Companion.RENDERMODE_CONTINUOUSLY

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class MediaRecord(val context: Context, val textureId:Int):BaseMediaEncoder() {


    val encodecRender=RenderRecord(context,textureId)


    init {
        render=encodecRender
        renderMode= RENDERMODE_CONTINUOUSLY

    }

}