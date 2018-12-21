package com.camera.yyl.camera

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.camera.yyl.R
import com.yyl.demo.utils.createProgram
import com.yyl.demo.utils.createTextImage
import com.yyl.demo.utils.readRawTxt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import android.R.attr.bitmap
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import com.yyl.demo.utils.loadBitmapTexture

//预览的render
class CameraFboRender(val context: Context) {


    val vertexData = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f,
        //水印坐标 占位 由图片大小替换
        0f, 0f,
        0f, 0f,
        0f, 0f,
        0f, 0f


    )
    //FBO 纹理坐标系 顶点1在左下角
    private val textureData = floatArrayOf(
        0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f
    )


    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private var program: Int = 0
    private var vPosition: Int = 0 //顶点坐标
    private var fPosition: Int = 0 //纹理坐标
    private var sTexture: Int = 0

    private var vboId: Int = 0
    private var bitmapTextureId: Int = 0

    val textBitmap = createTextImage("yuyunlong", 50f, "#ffccff", "#000000", 0f)


    init {
        val r = 1.0f * textBitmap.width / textBitmap.height
        val w = r * 0.1f
        vertexData[8] = 0.8f - w
        vertexData[9] = -0.8f

        vertexData[10] = 0.8f
        vertexData[11] = -0.8f

        vertexData[12] = 0.8f - w
        vertexData[13] = -0.7f

        vertexData[14] = 0.8f
        vertexData[15] = -0.7f

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
        //开启ARGB的 透明   不开启就是黑色
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        val vertexSource = readRawTxt(context, R.raw.vertex_shader_screen)
        val fragmentSource = readRawTxt(context, R.raw.fragment_shader_screen)
        program = createProgram(vertexSource, fragmentSource)
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        fPosition = GLES20.glGetAttribLocation(program, "f_Position")
        //program默认帮定到了sTexture
        sTexture = GLES20.glGetUniformLocation(program, "sTexture")


//---------------------VBO（顶点缓冲）---------------------
        val vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)
        vboId = vbo[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexData.size * 4 + textureData.size * 4,
            null,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.size * 4, vertexBuffer)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4, textureData.size * 4, textureBuffer)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        //注意 ：  GLES创建对像和线程有关
        bitmapTextureId = loadBitmapTexture(textBitmap)
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


        //开始绘制bitmap
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bitmapTextureId)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 32)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.size * 4)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)


        //绘制完 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

}
