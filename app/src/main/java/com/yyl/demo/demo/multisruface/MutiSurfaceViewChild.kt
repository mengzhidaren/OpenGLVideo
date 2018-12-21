package com.yyl.demo.demo.multisruface

import android.content.Context
import android.util.AttributeSet
import com.yyl.demo.view.YGLSurfaceView

class MutiSurfaceViewChild : YGLSurfaceView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)



    val renderSurface= Render7MutiSurfaceChild(context)
    init {
        render=renderSurface
    }



}