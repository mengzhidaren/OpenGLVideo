package com.yyl.demo.demo.multisruface

import android.content.Context
import android.opengl.GLES20
import com.camera.yyl.egl.Renderer
import com.yyl.demo.R
import com.yyl.demo.utils.createProgram
import com.yyl.demo.utils.readRawTxt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Render7MutiSurfaceChild(val context: Context) : Renderer {

    val vertexData = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
    private val textureData = floatArrayOf(
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    )

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private var program: Int = 0
    private var vPosition: Int = 0
    private var tPosition: Int = 0
    private var sTexture: Int = 0

    private var vboId: Int = 0


    var textureId=0

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)//内存大小4个顶点坐标
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData)
        vertexBuffer.position(0)//把内存索引移动到最前面

        textureBuffer = ByteBuffer.allocateDirect(textureData.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData)
        textureBuffer.position(0)//把内存索引移动置最前面
    }


    override  fun onSurfaceCreated() {
        val vertexSource = readRawTxt(context, R.raw.demo_vertex)
        val fragmentSource = readRawTxt(context, R.raw.demo_texture)
        program = createProgram(vertexSource, fragmentSource)
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        tPosition = GLES20.glGetAttribLocation(program, "t_Position")
        sTexture = GLES20.glGetUniformLocation(program, "sTexture")

//---------------------VBO（顶点缓冲）---------------------
        val vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)
        vboId = vbo[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER, vertexData.size * 4 + textureData.size * 4,
                null,
                GLES20.GL_STATIC_DRAW
        )
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.size * 4, vertexBuffer)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4, textureData.size * 4, textureBuffer)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    override   fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glUseProgram(program)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)
        GLES20.glEnableVertexAttribArray(tPosition)
        GLES20.glVertexAttribPointer(tPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.size * 4)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }


}