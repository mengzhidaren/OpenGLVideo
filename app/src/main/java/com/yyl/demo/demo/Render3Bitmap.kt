package com.yyl.demo.demo

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.camera.yyl.egl.Renderer
import com.yyl.demo.R
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
 *
 *
 */
class Render3Bitmap(val context: Context) : Renderer {

    // 顶点坐标(两个三角形组合  顶点1,2,3   顶点2,3,4 )  控制窗口范围
    val vertexData = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
    //纹理坐标(两个三角形组合  顶点1,2,3   顶点2,3,4 )  控制显示纹理范围
    private val textureData = floatArrayOf(
            //平铺
            0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f
            //裁剪   只显示 左上 1/4 的图片
//            0f, 0.5f,
//            0.5f, 0.5f,
//            0f, 0f,
//            0.5f, 0f

    )

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private var program: Int = 0
    private var vPosition: Int = 0
    private var tPosition: Int = 0
    private var sTexture: Int = 0

    private var textureId: Int = 0

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
        //1.shader创建
        val vertexSource = readRawTxt(context, R.raw.demo_vertex)
        val fragmentSource = readRawTxt(context, R.raw.demo_texture)
        program = createProgram(vertexSource, fragmentSource)
        //得到着色器中的属性
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        tPosition = GLES20.glGetAttribLocation(program, "t_Position")
        sTexture = GLES20.glGetUniformLocation(program, "sTexture")


//        2、创建和绑定纹理：
        val textureIds= IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        textureId=textureIds[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        //激活第0个TEXTURE
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //链接TEXTURE
        GLES20.glUniform1i(sTexture,0)

//        3、设置环绕和过滤方式
//        环绕（超出纹理坐标范围）：（s==x t==y GL_REPEAT 重复）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
//        过滤（纹理像素映射到坐标点）：（缩小、放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

//        4、设置图片（bitmap）
        val bitmap=BitmapFactory.decodeResource(context.resources,R.drawable.demo)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        //解开绑定
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame() {
        //第一步  清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //绘制红色
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        //10.使用源程序
        GLES20.glUseProgram(program)

        //---绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        //11.使顶点属性数组有效：
        GLES20.glEnableVertexAttribArray(vPosition)
        //12.为顶点属性赋值：
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexBuffer)
        //11.使顶点属性数组有效：
        GLES20.glEnableVertexAttribArray(tPosition)
        //12.为顶点属性赋值：
        GLES20.glVertexAttribPointer(tPosition, 2, GLES20.GL_FLOAT, false, 8,
                textureBuffer)

        //13.绘制图形： 从第first个顶点开始   绘制count个顶点     如果是3个顶点就是三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)


        //----解开绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

    }


}