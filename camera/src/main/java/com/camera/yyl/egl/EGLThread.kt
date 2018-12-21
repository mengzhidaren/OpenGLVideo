package com.camera.yyl.egl

import com.yyl.demo.utils._i
import com.yyl.demo.view.EGLHelper
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGLContext
/**
 * EGL线程   所有的纹理和坐标 必须在这个线程下创建 和 绘制
 *
 * baseRender 封装 绘制 方法和流程
 */
class EGLThread(private val eglSurface: WeakReference<EGLSurfaceBase>) : Thread() {
    companion object {
        //手工刷新
        const val RENDERMODE_WHEN_DIRTY: Int = 0
        //自动刷新 1000/60
        const val RENDERMODE_CONTINUOUSLY: Int = 1
    }


    private val tag = "EGLThread"
    private val lock = Object()
    private var isStart = false
    private var isExit = false
    private var isCreate = false

    var isChange = false
    var width = 0
    var height = 0

    var eglContext: EGLContext? = null

    override fun run() {
        isCreate=true
        val eglHelper = EGLHelper()
        eglHelper.initEgl(eglSurface.get()?.baseSurface(), eglSurface.get()?.baseEglContext())
        eglContext = eglHelper.mEglContext
        while (true) {
            if (isExit) {
                release(eglHelper)
                break
            }
            if (isStart) {
                if (eglSurface.get()?.baseRenderMode() == RENDERMODE_WHEN_DIRTY) {
                    synchronized(lock) {
                        lock.wait()
                    }
                } else if (eglSurface.get()?.baseRenderMode() == RENDERMODE_CONTINUOUSLY) {
                    Thread.sleep(1000 / 60)
                } else {
                    throw RuntimeException("renderMode  null")
                }
            }
            if (isExit) {
                continue
            }
            onCreate()
            onChange(width, height)
            onDraw(eglHelper)
            isStart = true

        }

    }


    private fun onCreate() {
        if (isCreate) {
            eglSurface.get()?.baseRender()?.apply {
                isCreate = false
                _i(tag, "onSurfaceCreated")
                onSurfaceCreated()
            }
        }
    }

    private fun onChange(width: Int, height: Int) {
        if (isChange) {
            eglSurface.get()?.baseRender()?.apply {
                isChange = false
                _i(tag, "onChange        width=$width height=$height")
                onSurfaceChanged(width, height)
            }
        }
    }

    private fun onDraw(eglHelper: EGLHelper) {
        eglSurface.get()?.baseRender()?.apply {
            if (!isStart) {//像是android的bug  init第一次绘制不出来 要刷新两次
                onDrawFrame()
//                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//                GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
            }
            onDrawFrame()
            eglHelper.swapBuffers()
        }

    }

    private fun release(eglHelper: EGLHelper) {
        _i(tag, "release")
        isCreate=false
        eglContext = null
        eglHelper.destoryEgl()
        eglSurface.clear()
    }

    fun requestRender() {
        if (isStart) {
            synchronized(lock) {
                lock.notifyAll()
            }
        }
    }

    fun onDestroy() {
        _i(tag, "onDestory")
        isExit = true
        requestRender()
    }
}