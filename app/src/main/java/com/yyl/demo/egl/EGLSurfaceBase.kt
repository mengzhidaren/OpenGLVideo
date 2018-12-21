package com.camera.yyl.egl

import android.view.Surface
import javax.microedition.khronos.egl.EGLContext

interface EGLSurfaceBase {
    fun baseSurface():Surface?
    //外部传入的环境
    fun baseEglContext():EGLContext?
    fun baseRender():Renderer?
    fun baseRenderMode():Int





}
