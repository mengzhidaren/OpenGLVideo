package com.camera.yyl.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import com.camera.yyl.R
import com.camera.yyl.egl.Renderer
import com.yyl.demo.utils.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class CameraRender(val context: Context) : Renderer, SurfaceTexture.OnFrameAvailableListener {


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
    private val matrix = FloatArray(16)

    private var program: Int = 0
    private var vPosition: Int = 0 //顶点坐标
    private var fPosition: Int = 0 //纹理坐标

    private var fboTextureId: Int = 0
    private var cameraTextureId: Int = 0

    private var surfaceTexture: SurfaceTexture? = null

    var surfaceCreateListener: OnSurfaceCreateListener? = null

    private var vboId: Int = 0
    private var fboId: Int = 0
    private var umatrix: Int = 0

    private var screenWidth=0
    private var screenHeight=0
    private var surfaceWidth=0
    private var surfaceHeight=0


    private val cameraFboRender = CameraFboRender(context)

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


        screenWidth= getScreenWidth(context)
        screenHeight= getScreenHeight(context)
    }


    override fun onSurfaceCreated() {
        cameraFboRender.onSurfaceCreated()
        val vertexSource = readRawTxt(context, R.raw.vertex_shader)
        val fragmentSource = readRawTxt(context, R.raw.fragment_shader)
        program = createProgram(vertexSource, fragmentSource)
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        fPosition = GLES20.glGetAttribLocation(program, "f_Position")

        umatrix=GLES20.glGetUniformLocation(program,"u_Matrix")
        //program默认帮定到了sTexture
//        sTexture = GLES20.glGetUniformLocation(program, "sTexture")


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
            screenWidth,
            screenHeight,
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
            _i("init fboTextureId  FBO绑定--失败")
        } else {
            _i("init fboTextureId  FBO绑定--success")
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        //新建一个摄像头的纹理
        val textureIdCameraOES = IntArray(1)
        GLES20.glGenTextures(1, textureIdCameraOES, 0)
        cameraTextureId = textureIdCameraOES[0]
        //绑定成扩展纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureId)
        //环绕
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        surfaceTexture = SurfaceTexture(cameraTextureId)
        surfaceTexture?.setOnFrameAvailableListener(this)
        surfaceCreateListener?.onSurfaceCreate(surfaceTexture,fboTextureId)

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

    }

    fun resetMatrix(){
        Matrix.setIdentityM(matrix,0)
    }

    fun setAngle(angle:Float,x:Float,y:Float,z:Float){
        //        正数：逆时针旋转
//        负数：顺时针旋转
//        x、y、z：分别表示相应坐标轴
        Matrix.rotateM(matrix, 0, angle, x, y, z)
    }
    override fun onSurfaceChanged(width: Int, height: Int) {
        cameraFboRender.onSurfaceChanged(width, height)
        this.surfaceWidth=width
        this.surfaceHeight=height

//        GLES20.glViewport(0, 0, width, height)
//            Matrix.rotateM(matrix, 0, 180f, 1f, 0f, 0f)
    }

    override fun onDrawFrame() {

        surfaceTexture?.updateTexImage()

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)


        GLES20.glUseProgram(program)

        //使用矩阵
        GLES20.glUniformMatrix4fv(umatrix, 1, false, matrix, 0)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)


        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.size * 4)

        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        cameraFboRender.onDrawFrame(fboTextureId)
    }

    //updateTexImage会刷新这里
    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {


    }
}

interface OnSurfaceCreateListener {
    fun onSurfaceCreate(surfaceTexture: SurfaceTexture?,fboTextureId:Int)
}