package com.yyl.demo.demo

import android.opengl.GLES20
import com.camera.yyl.egl.Renderer
import com.yyl.demo.utils._i

class Render2Shader : Renderer {


    override fun onSurfaceCreated() {
        _i("onSurfaceCreated")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        _i("onSurfaceChanged   $width   $height")
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame() {
        _i("onDrawFrame")
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
    }
}