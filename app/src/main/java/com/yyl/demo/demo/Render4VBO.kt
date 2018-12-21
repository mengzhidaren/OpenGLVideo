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
 *1、VBO： Vertex Buffer object（顶点缓冲）
2、为什么要用VBO?
不使用VBO时，我们每次绘制（ glDrawArrays ）图形时都是从本地内存处获取顶点数据然后传输给OpenGL来绘制，
这样就会频繁的操作CPU->GPU增大开销，从而降低效率。
使用VBO，我们就能把顶点数据缓存到GPU开辟的一段内存中，然后使用时不必再从本地获取，而是直接从显存中获取，这样就能提升绘制的效率。
 */
class Render4VBO(val context: Context) : Renderer {

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


    override fun onSurfaceCreated() {
        //1.shader创建
        val vertexSource = readRawTxt(context, R.raw.demo_vertex)
        val fragmentSource = readRawTxt(context, R.raw.demo_texture)
        program = createProgram(vertexSource, fragmentSource)
        //得到着色器中的属性
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        tPosition = GLES20.glGetAttribLocation(program, "t_Position")
        sTexture = GLES20.glGetUniformLocation(program, "sTexture")

        //1创建VBO（顶点缓冲）
        val vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)
        vboId = vbo[0]
        //2绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        //3分配VBO需要的缓存大小   (vertexData.size * 4 + textureData.size * 4)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4 + textureData.size * 4, null, GLES20.GL_STATIC_DRAW)
        //4为VBO设置顶点数据的值        (在显卡内存中的偏移量0   从0开始)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.size * 4, vertexBuffer)
         //                           (在显卡内存中的偏移量vertexData.size * 4    从vertexData位置后面开始就是textureData)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4, textureData.size * 4, textureBuffer)
        //5解开绑定
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)


//        2、创建和绑定纹理：
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        textureId = textureIds[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        //激活第0个TEXTURE
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //链接TEXTURE
        GLES20.glUniform1i(sTexture, 0)

//        3、设置环绕和过滤方式
//        环绕（超出纹理坐标范围）：（s==x t==y GL_REPEAT 重复）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
//        过滤（纹理像素映射到坐标点）：（缩小、放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

//        4、设置图片（bitmap）
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.demo)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        //解开绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }
    /**使用VBO*/
    override fun onDrawFrame() {
        //第一步  清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //绘制红色
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        //10.使用源程序
        GLES20.glUseProgram(program)


        //----绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        //----1绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)


        //11.使顶点属性数组有效：
        GLES20.glEnableVertexAttribArray(vPosition)
        //12.为顶点属性赋值：
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                0)

        //11.使顶点属性数组有效：
        GLES20.glEnableVertexAttribArray(tPosition)
        //12.为顶点属性赋值：
        GLES20.glVertexAttribPointer(tPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.size * 4)

        //13.绘制图形： 从第first个顶点开始   绘制count个顶点     如果是3个顶点就是三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)


        //----解开绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        //2解开绑定
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }


}