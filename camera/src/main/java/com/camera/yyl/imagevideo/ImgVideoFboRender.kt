package com.camera.yyl.imagevideo

import android.content.Context
import android.opengl.GLES20
import com.camera.yyl.R
import com.yyl.demo.utils.createProgram
import com.yyl.demo.utils.readRawTxt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ImgVideoFboRender(val context: Context) {
    val vertexData = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f


    )
    //FBO 纹理坐标系 顶点1在左下角
    private val textureData = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )


    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private var program: Int = 0

    private var vPosition: Int = 0 //顶点坐标
    private var fPosition: Int = 0 //纹理坐标

    private var vboId: Int = 0


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


    fun onSurfaceCreated() {

        val vertexSource = readRawTxt(context, R.raw.vertex_shader_screen)
        val fragmentSource = readRawTxt(context, R.raw.fragment_shader_screen)
        program = createProgram(vertexSource, fragmentSource)
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        fPosition = GLES20.glGetAttribLocation(program, "f_Position")


//---------------------VBO（顶点缓冲）---------------------
        val vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)
        vboId = vbo[0]

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER, vertexData.size * 4 + textureData.size * 4, null, GLES20.GL_STATIC_DRAW
        )
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.size * 4, vertexBuffer)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4, textureData.size * 4, textureBuffer)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    }

    fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    fun onDrawFrame(fboTextureId: Int) {


        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)

        GLES20.glUseProgram(program)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.size * 4)

        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        //绘制完 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }
}