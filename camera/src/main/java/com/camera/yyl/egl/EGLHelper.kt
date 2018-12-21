package com.yyl.demo.view

import android.opengl.EGL14
import android.view.Surface
import javax.microedition.khronos.egl.*

class EGLHelper {

    private lateinit var mEgl:EGL10

    private var mEglDisplay: EGLDisplay? = null
    private var mEglSurface: EGLSurface? = null

    var mEglContext: EGLContext? = null

    //窗口属性
    private val attrbutes = intArrayOf(
        EGL10.EGL_RED_SIZE, 8,
        EGL10.EGL_GREEN_SIZE, 8,
        EGL10.EGL_BLUE_SIZE, 8,
        EGL10.EGL_ALPHA_SIZE, 8,
        EGL10.EGL_DEPTH_SIZE, 8,
        EGL10.EGL_STENCIL_SIZE, 8,
        EGL10.EGL_RENDERABLE_TYPE,//版本
        4,
        EGL10.EGL_NONE  //结尾
    )


    fun initEgl(surface: Surface?, eglContext: EGLContext?) {

        //1  mEgl init
        mEgl= EGLContext.getEGL() as EGL10
        //2. mEglDisplay init
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw RuntimeException("eglGetDisplay failed")
        }

        //3. version
        val version = IntArray(2)
        if (!mEgl.eglInitialize(mEglDisplay, version)) {
            throw RuntimeException("eglInitialize failed")
        }
        //4.

        val num_config = IntArray(1)
        if (!mEgl.eglChooseConfig(mEglDisplay, attrbutes, null, 1, num_config)) {
            throw IllegalArgumentException("eglChooseConfig failed")
        }
        val numConfigs = num_config[0]
        if (numConfigs <= 0) {
            throw IllegalArgumentException("No configs match configSpec")
        }
        //5.
        val configs = arrayOfNulls<EGLConfig>(numConfigs)
        if (!mEgl.eglChooseConfig(mEglDisplay, attrbutes, configs, numConfigs, num_config)) {
            throw IllegalArgumentException("eglChooseConfig#2 failed")
        }
        val attribList = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
        //6.
        mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], eglContext ?: EGL10.EGL_NO_CONTEXT, attribList)
        //7.
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, configs[0], surface, null)
        //8.
        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw RuntimeException("MakeCurrent failed")
        }
    }

    //刷新buffer 交换buffer
    fun swapBuffers() {
        mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)
    }

    fun destoryEgl() {
        mEgl.eglMakeCurrent(
            mEglDisplay, EGL10.EGL_NO_SURFACE,
            EGL10.EGL_NO_SURFACE,
            EGL10.EGL_NO_CONTEXT
        )
        mEgl.eglDestroySurface(mEglDisplay,mEglSurface)
        mEglSurface = null
        mEgl.eglDestroyContext(mEglDisplay,mEglContext)
        mEglContext=null
        mEgl.eglTerminate(mEglDisplay)
        mEglDisplay=null

    }
}