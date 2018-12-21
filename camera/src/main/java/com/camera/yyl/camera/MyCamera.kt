package com.camera.yyl.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.graphics.ImageFormat
import com.yyl.demo.utils.getScreenHeight
import com.yyl.demo.utils.getScreenWidth
import java.io.IOException


class MyCamera(val context: Context) {
    companion object {
        const val camera_back = Camera.CameraInfo.CAMERA_FACING_BACK
        const val camera_front = Camera.CameraInfo.CAMERA_FACING_FRONT
    }
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    init {
        screenWidth= getScreenWidth(context)
        screenHeight= getScreenHeight(context)
    }


    private val cameraId = Camera.CameraInfo.CAMERA_FACING_BACK
    private var camera: Camera? = null

    private var surfaceTexture: SurfaceTexture? = null

    fun initCamera(surfaceTexture: SurfaceTexture?, cameraId: Int) {
        this.surfaceTexture = surfaceTexture
        setCamera(cameraId)
    }

    private fun setCamera(cameraId: Int) {
        try {
            camera = Camera.open(cameraId)
            camera?.setPreviewTexture(surfaceTexture)
            val parameters = camera?.parameters!!

            parameters.flashMode = "off"
            parameters.previewFormat = ImageFormat.NV21

            var size = getFitSize(parameters.supportedPictureSizes)
            parameters.setPictureSize(size.width, size.height)

            size = getFitSize(parameters.supportedPreviewSizes)
            parameters.setPreviewSize(size.width, size.height)
//            parameters.setPictureSize(
//                parameters.supportedPictureSizes[0].width,
//                parameters.supportedPictureSizes[0].height
//            )
//            parameters.setPreviewSize(
//                parameters.supportedPreviewSizes[0].width,
//                parameters.supportedPreviewSizes[0].height
//            )

            camera?.parameters = parameters
            camera?.startPreview()

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun stopPreview() {
        camera?.apply {
            startPreview()
            release()
        }
        camera = null
    }

    fun changeCamera(cameraId: Int) {
        camera?.stopPreview()
        setCamera(cameraId)
    }


    private fun getFitSize(sizes: List<Camera.Size>): Camera.Size {
        if (screenWidth < screenHeight) {
            val t = screenHeight
            screenHeight = screenWidth
            screenWidth = t
        }

        for (size in sizes) {
            if (1.0f * size.width / size.height == 1.0f * screenWidth / screenHeight) {
                return size
            }
        }
        return sizes[0]
    }

}