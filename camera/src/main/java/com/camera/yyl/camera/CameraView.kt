package com.camera.yyl.camera

import android.content.Context
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.AttributeSet
import com.yyl.demo.view.YGLSurfaceView
import android.view.Surface
import android.view.WindowManager
import com.yyl.demo.utils._i


class CameraView : YGLSurfaceView {


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)


    private val cameraRender = CameraRender(context)
    private val camera = MyCamera(context)
    private var cameraId = Camera.CameraInfo.CAMERA_FACING_BACK
    //    var cameraId= Camera.CameraInfo.CAMERA_FACING_FRONT
    private var textureId: Int = -1

    init {
        render = cameraRender
        previewAngle(context)
        cameraRender.surfaceCreateListener = object : OnSurfaceCreateListener {
            override fun onSurfaceCreate(surfaceTexture: SurfaceTexture?, fboTextureId: Int) {
                camera.initCamera(surfaceTexture, cameraId)
                textureId = fboTextureId
            }
        }
    }

    override fun getFboTextureId() = textureId

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        previewAngle(context)
    }


    fun onDestory() {
        camera.stopPreview()
    }


    fun previewAngle(context: Context) {
        val angle = (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.rotation ?: 0
        cameraRender.resetMatrix()
        _i("previewAngle   angle=$angle")
        when (angle) {
            Surface.ROTATION_0 -> {
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraRender.setAngle(90f, 0f, 0f, 1f)
                    cameraRender.setAngle(180f, 1f, 0f, 0f)
                } else {
                    cameraRender.setAngle(90f, 0f, 0f, 1f);
                }

            }
            Surface.ROTATION_90 -> {
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraRender.setAngle(180f, 0f, 0f, 1f)
                    cameraRender.setAngle(180f, 0f, 1f, 0f)
                } else {
                    cameraRender.setAngle(90f, 0f, 0f, 1f)
                }
            }
            Surface.ROTATION_180 -> {
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraRender.setAngle(90f, 0.0f, 0f, 1f)
                    cameraRender.setAngle(180f, 0.0f, 1f, 0f)
                } else {
                    cameraRender.setAngle(-90f, 0f, 0f, 1f)
                }
            }
            Surface.ROTATION_270 -> {
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraRender.setAngle(180f, 0.0f, 1f, 0f)
                } else {
                    cameraRender.setAngle(0f, 0f, 0f, 1f)
                }
            }

        }
    }

}