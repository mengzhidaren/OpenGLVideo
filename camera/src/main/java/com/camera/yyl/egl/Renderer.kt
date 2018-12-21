package com.camera.yyl.egl

interface Renderer {

    fun onSurfaceCreated()
    fun onSurfaceChanged(width: Int, height: Int)
    fun onDrawFrame()
}