package com.yyl.demo

import android.content.Context
import android.util.AttributeSet
import com.yyl.demo.demo.Render8Matrix
import com.yyl.demo.view.YGLSurfaceView

class MainSurfaceView: YGLSurfaceView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    init {
//        render=Render2Shader()
//        render=Render3Bitmap(context)
//        render=Render4VBO(context)
//        render=Render5FBO(context)
//        render=Render6Matrix(context)
        render=Render8Matrix(context)
//        renderMode=RENDERMODE_WHEN_DIRTY
    }
}