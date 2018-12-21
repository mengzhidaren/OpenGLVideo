package com.camera.yyl.imagevideo

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.camera.yyl.R
import com.camera.yyl.camera.CameraFboRender
import com.camera.yyl.camera.OnSurfaceCreateListener
import com.camera.yyl.egl.Renderer
import com.yyl.demo.utils.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ImgVideoRender(val context: Context) :Renderer {

    private val vertexData = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )
    //FBO 纹理坐标系 顶点1在左下角
    private val textureData = floatArrayOf(
        0f, 0f,
        1f, 0f,
        0f, 1f,
        1f, 1f
    )


    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer

    private var program: Int = 0
    private var vPosition: Int = 0 //顶点坐标
    private var fPosition: Int = 0 //纹理坐标

    private var fboTextureId: Int = 0


    private var vboId: Int = 0
    private var fboId: Int = 0



    var imgSrc = 0
    private var imgTextureId: Int = 0


    var onRenderCreateListener:OnRenderCreateListener?=null

    private val videoFboRender = ImgVideoFboRender(context)

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




    override fun onSurfaceCreated() {
        videoFboRender.onSurfaceCreated()
        val vertexSource = readRawTxt(context, R.raw.vertex_shader_screen)
        val fragmentSource = readRawTxt(context, R.raw.fragment_shader_screen)
        program = createProgram(vertexSource, fragmentSource)
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        fPosition = GLES20.glGetAttribLocation(program, "f_Position")

    }

    override fun onSurfaceChanged(width: Int, height: Int) {

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


//---------------------FBO（帧缓冲对象）---------------------
        val fbo = IntArray(1)
        GLES20.glGenBuffers(1, fbo, 0)
        fboId = fbo[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)

        //新建一个画布纹理(画板)
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        fboTextureId = textureIds[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId)
        //环绕
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            width,
            height,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        );
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            fboTextureId,
            0
        )//这里的fboTextureId只是一个画板
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            _i("FBO绑定--失败")
        } else {
            _i("FBO绑定--success")
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        onRenderCreateListener?.onRenderCreate(fboTextureId)

        GLES20.glViewport(0, 0, width, height)
        videoFboRender.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame() {
        //创建图片纹理
        imgTextureId = loadTexture(imgSrc, context)
        _i("imgTextureId=$imgTextureId")

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1f,0f, 0f, 1f)
        GLES20.glUseProgram(program)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
            0)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
            vertexData.size * 4)

        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        //删除图片
        val ids = intArrayOf(imgTextureId)
        GLES20.glDeleteTextures(1, ids, 0)


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        videoFboRender.onDrawFrame(fboTextureId)
    }



    interface OnRenderCreateListener{
        fun onRenderCreate(fboTexture:Int)
    }
}