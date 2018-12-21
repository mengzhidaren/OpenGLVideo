package com.yyl.demo.view

import android.content.Context
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.camera.yyl.egl.EGLSurfaceBase
import com.camera.yyl.egl.EGLThread
import com.camera.yyl.egl.Renderer
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGLContext

abstract class YGLSurfaceView : SurfaceView, EGLSurfaceBase, SurfaceHolder.Callback {


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    var surface: Surface? = null  //视频输入画布

    private var baseEglContext: EGLContext? = null
    var render: Renderer? = null
    var renderMode = EGLThread.RENDERMODE_CONTINUOUSLY
    override fun baseSurface() = surface
    override fun baseEglContext() = baseEglContext
    override fun baseRender() = render
    override fun baseRenderMode() = renderMode
    abstract fun getFboTextureId(): Int

    init {
        holder.addCallback(this)
        renderMode = EGLThread.RENDERMODE_CONTINUOUSLY
    }


    var isCreate = false


    private lateinit var eglThread: EGLThread

    //内部初始化生成的环境
    fun eglContext() = eglThread.eglContext


    fun setSurfaceAndEglContext(surface: Surface?, eglContext: EGLContext?) {
        this.surface = surface
        this.baseEglContext = eglContext
    }

    fun requestRender() {
        if (isCreate) {
            eglThread.requestRender()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (surface == null) {
            surface = holder?.surface
        }
        eglThread = EGLThread(WeakReference(this))
        eglThread.start()
        isCreate = true
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        eglThread.width = width
        eglThread.height = height
        eglThread.isChange = true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        isCreate = false
        eglThread.onDestroy()
        surface = null
        baseEglContext = null
    }


}

