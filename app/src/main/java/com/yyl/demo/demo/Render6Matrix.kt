package com.yyl.demo.demo

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import com.camera.yyl.egl.Renderer
import com.yyl.demo.R
import com.yyl.demo.utils._i
import com.yyl.demo.utils.createProgram
import com.yyl.demo.utils.readRawTxt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


/**
 * 顶点坐标系
 * -1,1 (顶点3)       1,0              1,1(顶点4)
 * <p>
 * -1,0              0,0(中心)        1,0
 * <p>
 * -1,-1(顶点1)      -1,0             1,-1(顶点2)
 *
 *纹理坐标系
 *
 * 0,0(顶点3)     1,0(顶点4)
 * <p>
 * <p>
 * 0,1 (顶点1)    1,1(顶点2)
 *
 *FBO 纹理坐标系
 * 0,1 (顶点3)    1,1(顶点4)
 *
 *
 * 0,0(顶点1)     1,0(顶点2)
 */
class Render6Matrix(val context: Context) : Renderer {
    val vertexData = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
    //FBO 纹理坐标系 顶点1在左下角
    private val textureData = floatArrayOf(
//            0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f
    )

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private var program: Int = 0
    private var vPosition: Int = 0
    private var tPosition: Int = 0
    private var sTexture: Int = 0

    private var umatrix: Int = 0
    private val matrix= FloatArray(16)

    private var textureId: Int = 0
    private var imgTextureId: Int = 0
    private var vboId: Int = 0

    private var fboId: Int = 0
    private val fboRender2=Render5FBOchild(context)
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
        fboRender2.onSurfaceCreated()
        val vertexSource = readRawTxt(context, R.raw.demo_vertex_matrix)
        val fragmentSource = readRawTxt(context, R.raw.demo_texture)
        program = createProgram(vertexSource, fragmentSource)
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        tPosition = GLES20.glGetAttribLocation(program, "t_Position")
        sTexture = GLES20.glGetUniformLocation(program, "sTexture")
        umatrix=GLES20.glGetUniformLocation(program,"u_Matrix")


//---------------------VBO（顶点缓冲）---------------------
        val vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)
        vboId = vbo[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4 + textureData.size * 4, null, GLES20.GL_STATIC_DRAW)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.size * 4, vertexBuffer)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4, textureData.size * 4, textureBuffer)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
//---------------------FBO（帧缓冲对象）---------------------
        val fbo = IntArray(1)
        GLES20.glGenBuffers(1, fbo, 0)
        fboId = fbo[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        textureId = textureIds[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glUniform1i(sTexture, 0)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 720, 1136, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0)//这里的textureId只是一个画板
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            _i("FBO绑定--失败")
        } else {
            _i("FBO绑定--success")
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        imgTextureId = loadTexrute(R.drawable.demo2)

    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        fboRender2.onSurfaceChanged(width, height)
//        2、然后根据图形宽高和屏幕宽高计算（？）的长度
        if(width>height){//横屏
            Matrix.orthoM(matrix, 0, -width / ((height / 702f * 526f)),  width / ((height / 702f * 526f)), -1f, 1f, -1f, 1f);

        }else{
            Matrix.orthoM(matrix, 0, -1f, 1f, - height / ((width / 526f * 702f)),  height / ((width / 526f * 702f)), -1f, 1f);

        }
//        a:
//        正数：逆时针旋转
//        负数：顺时针旋转
//        x、y、z：分别表示相应坐标轴

    //    Matrix.rotateM(matrix, 0, 180f, 1f, 0f, 0f)

    }

    override fun onDrawFrame() {

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)


        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glUseProgram(program)
//        3、使用
        GLES20.glUniformMatrix4fv(umatrix, 1, false, matrix, 0)


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)
        GLES20.glEnableVertexAttribArray(tPosition)
        GLES20.glVertexAttribPointer(tPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.size * 4)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        fboRender2.onDraw(textureId)
    }

    /**
     * 返回有图片数据的纹理
     * */
    fun loadTexrute(src: Int): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        val bitmap = BitmapFactory.decodeResource(context.resources, src)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
//        bitmap.recycle()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureIds[0]
    }

}