package com.yyl.demo.utils

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


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