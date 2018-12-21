package com.yyl.demo.utils

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import android.graphics.Color.parseColor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.nio.ByteBuffer
import android.opengl.GLUtils
import android.graphics.BitmapFactory




fun readRawTxt(context: Context, rawId: Int): String {
    val inputStream = context.resources.openRawResource(rawId)
    val reader = BufferedReader(InputStreamReader(inputStream))
    val sb = StringBuffer()
    var line: String?
    try {
        while (true) {
            line = reader.readLine()
            if (line != null) {
                sb.append(line).append("\n")
            } else {
                break
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            reader.close()
        } catch (e: IOException) {
        }
    }

    return sb.toString()
}
/*加载shader*/
fun loadShader(shaderType: Int, source: String): Int {
    //1.创建shader（着色器：顶点或片元）    tip:如果没有指定版本号这里会失败
    var shader = GLES20.glCreateShader(shaderType)
    if (shader != 0) {
        //2.加载shader源码并编译shader
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val compile = IntArray(1)
        //3.检查是否编译成功：
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compile, 0)
        if (compile[0] != GLES20.GL_TRUE) {
            Log.e("yyl", "shader compile error")
            GLES20.glDeleteShader(shader)
            shader = 0
        }
    }
    return shader
}
/*创建一个渲染程序*/
fun createProgram(vertexSource: String, fragmentSource: String): Int {
    val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
    if (vertexShader == 0) {
        return 0
    }
    val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
    if (fragmentShader == 0) {
        return 0
    }
    //4.创建一个渲染程序
    var program = GLES20.glCreateProgram()
    if (program != 0) {
        //5.将着色器程序添加到渲染程序中：
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        //6.链接源程序：
        GLES20.glLinkProgram(program)
        val linsStatus = IntArray(1)
        //7.检查链接源程序是否成功
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linsStatus, 0)
        if (linsStatus[0] != GLES20.GL_TRUE) {
            Log.e("yyl", "link program error")
            GLES20.glDeleteProgram(program)
            program = 0
        }
    }
    return program
}


fun createTextImage(text: String, textSize: Float, textColor: String, bgColor: String, padding: Float): Bitmap {

    val paint = Paint()
    paint.color = Color.parseColor(textColor)
    paint.textSize = textSize
    paint.style = Paint.Style.FILL
    paint.isAntiAlias = true

    val width = paint.measureText(text, 0, text.length)
    val top = paint.fontMetrics.top
    val bottom = paint.fontMetrics.bottom
    val bm = Bitmap.createBitmap(
        (width + padding * 2).toInt(),
        (bottom - top + padding * 2).toInt(),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bm)

    canvas.drawColor(Color.parseColor(bgColor))
    canvas.drawText(text, padding, -top + padding, paint)
    return bm
}

fun loadBitmapTexture(bitmap: Bitmap): Int {
    val textureIds = IntArray(1)
    GLES20.glGenTextures(1, textureIds, 0)
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

    val bitmapBuffer = ByteBuffer.allocate(bitmap.height * bitmap.width * 4)
    bitmap.copyPixelsToBuffer(bitmapBuffer)
    bitmapBuffer.flip()//移动内存空间 对齐

    GLES20.glTexImage2D(
        GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.width,
        bitmap.height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bitmapBuffer
    )
    return textureIds[0]
}

fun loadTexture(src: Int, context: Context): Int {
    val textureIds = IntArray(1)
    GLES20.glGenTextures(1, textureIds, 0)
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

    var bitmap: Bitmap? = BitmapFactory.decodeResource(context.resources, src)
    bitmap?.let {
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, it, 0)
        it.recycle()
    }
    bitmap = null
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    return textureIds[0]

}