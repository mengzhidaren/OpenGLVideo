package com.camera.yyl.imagevideo

import android.content.Context
import android.util.AttributeSet
import com.camera.yyl.egl.EGLThread
import com.yyl.demo.view.YGLSurfaceView

class ImgVideoView : YGLSurfaceView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    private val imgRender = ImgVideoRender(context)
    private var fbotextureid: Int = -1

    init {
        render = imgRender
        renderMode = EGLThread.RENDERMODE_WHEN_DIRTY
        imgRender.onRenderCreateListener = object : ImgVideoRender.OnRenderCreateListener {
            override fun onRenderCreate(fboTexture: Int) {
                fbotextureid = fboTexture
            }
        }
    }

    fun setCurrentImg(imgSrc: Int) {
        imgRender.imgSrc = imgSrc
        requestRender()
    }


    override fun getFboTextureId()=fbotextureid
}