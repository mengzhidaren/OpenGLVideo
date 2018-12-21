package com.camera.yyl.encodec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Surface
import com.yyl.demo.utils._i
import java.io.IOException
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGLContext
import android.media.MediaMuxer
import com.camera.yyl.egl.EGLSurfaceBase
import com.camera.yyl.egl.EGLThread
import com.camera.yyl.egl.EGLThread.Companion.RENDERMODE_CONTINUOUSLY
import com.camera.yyl.egl.Renderer


@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
open class BaseMediaEncoder : EGLSurfaceBase {
    private val tag = "BaseMediaEncoder"

    var surface: Surface? = null  //视频输入画布
    var baseEglContext: EGLContext? = null
    var render: Renderer? = null
    var renderMode = RENDERMODE_CONTINUOUSLY
    override fun baseSurface() = surface
    override fun baseEglContext() = baseEglContext
    override fun baseRender() = render
    override fun baseRenderMode() = renderMode


    var onMediaInfoListener: OnMediaInfoListener? = null

    var videoCodec: MediaCodec? = null
    var audioCodec: MediaCodec? = null
    var mediaMuxer: MediaMuxer? = null

    var videoExit = true
    var audioExit = true
    var muxerStart = false
    private var audioPts: Long = 0
    private var sampleRate: Int = 0
    var width = 0
    var height = 0

    private var eglThread: EGLThread? = null
    var videoEncodecThread: VideoEncodecThread? = null
    var audioEncodecThread: AudioEncodecThread? = null

    fun initEncodec(
        baseEglContext: EGLContext?,
        savePath: String,
        width: Int,
        height: Int,
        sampleRate: Int,
        channelCount: Int
    ) {
        this.baseEglContext = baseEglContext
        this.width = width
        this.height = height
        initMediaEncodec(savePath, width, height, sampleRate, channelCount)
    }


    fun startRecord(): Boolean {
        surface?.apply {
            baseEglContext?.apply {
                videoExit = false
                audioExit = false
                muxerStart = false
                eglThread = EGLThread(WeakReference(this@BaseMediaEncoder))
                videoEncodecThread = VideoEncodecThread(WeakReference(this@BaseMediaEncoder))
                audioEncodecThread = AudioEncodecThread(WeakReference(this@BaseMediaEncoder))
                eglThread?.width = width
                eglThread?.height = height
                eglThread?.isChange = true
                eglThread?.start()
                videoEncodecThread?.start()
                audioEncodecThread?.start()
                _i(tag, "startRecord  success")
                return true
            }
        }
        return false
    }

    fun stopRecord() {
        audioEncodecThread?.exit()
        videoEncodecThread?.exit()
        eglThread?.onDestroy()
        videoEncodecThread = null
        audioEncodecThread = null
        eglThread = null
    }

    //给音频喂数据
    fun putPCMData(buffer: ByteArray?, size: Int) {
        if (buffer != null && size > 0 && !audioExit) {
            val inputBufferindex = audioCodec?.dequeueInputBuffer(0) ?: -1
            if (inputBufferindex >= 0) {
                val byteBuffer = audioCodec!!.inputBuffers[inputBufferindex]
                byteBuffer.clear()
                byteBuffer.put(buffer)
                val pts = getAudioPts(size, sampleRate)
                audioCodec?.queueInputBuffer(inputBufferindex, 0, size, pts, 0)
            }
        }
    }

    private fun getAudioPts(size: Int, sampleRate: Int): Long {
        audioPts += (1.0 * size / (sampleRate * 2 * 2) * 1000000.0).toLong()
        return audioPts
    }

    /**
     *    mediaMuxer  必须有pcm数据  不然会闪退
     */
    fun stopMuxer() {
        if (audioExit && videoExit) {
            synchronized(this) {
                if (mediaMuxer != null) {
                    mediaMuxer?.stop()
                    mediaMuxer?.release()
                    mediaMuxer = null
                }
            }
        }

    }

    private fun initMediaEncodec(savePath: String, width: Int, height: Int, sampleRate: Int, channelCount: Int) {
        try {
            mediaMuxer = MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            initVideoEncoder(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
            initAudioEncoder(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun initVideoEncoder(mimeType: String, width: Int, height: Int) {
        val mediaFormat = MediaFormat.createVideoFormat(mimeType, width, height)
        mediaFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )   //从Surface上获取图像
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4)  //比特率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)  //帧率
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)  //每秒关键帧
        try {
            videoCodec = MediaCodec.createEncoderByType(mimeType)
            videoCodec?.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            surface = videoCodec?.createInputSurface()
            _i(tag, "initVideoEncoder  success")
        } catch (e: IOException) {
            e.printStackTrace()
            videoCodec = null
        }
    }

    private fun initAudioEncoder(mimeType: String, sampleRate: Int, channelCount: Int) {
        this.sampleRate = sampleRate
        val mediaFormat = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000)
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096)
        try {
            audioCodec = MediaCodec.createEncoderByType(mimeType)
            audioCodec?.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        } catch (e: IOException) {
            e.printStackTrace()
            audioCodec = null
        }
    }
}

interface OnMediaInfoListener {
    fun onMediaTime(times: Long)
}

//视频编码线程
class VideoEncodecThread(private val baseRender: WeakReference<BaseMediaEncoder>) : Thread() {
    private val tag = "VideoEncodecThread"
    private var mediaMuxer: MediaMuxer = baseRender.get()?.mediaMuxer!!
    private var videoCodec: MediaCodec = baseRender.get()?.videoCodec!!
    private var bufferInfo = MediaCodec.BufferInfo()

    private var isExit = false
    private var pts: Long = 0
    var videoTrackIndex = -1

    private fun release() {
        videoCodec.stop()
        videoCodec.release()
        baseRender.get()?.videoExit = true
        baseRender.get()?.stopMuxer()
        _i(tag, "MediaMuxer.release() 录制完成")
    }

    override fun run() {
        super.run()
        _i(tag, "视频编码线程 开始录制")
        videoCodec.start()
        while (true) {
            if (isExit) {
                release()
                break
            }
            //出队  输出到mediaBufferInfo
            var outputBufferIndex = videoCodec.dequeueOutputBuffer(bufferInfo, 0)
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                videoTrackIndex = mediaMuxer.addTrack(videoCodec.outputFormat)//视频轨道  大部分都是 0
                if (baseRender.get()?.audioEncodecThread?.audioTrackIndex != -1) {
                    mediaMuxer.start()
                    baseRender.get()?.muxerStart = true
                }

            } else {
                while (outputBufferIndex >= 0) {
                    if (baseRender.get()?.muxerStart == true) {
                        val outputBuffer = videoCodec.outputBuffers[outputBufferIndex]
                        outputBuffer?.position(bufferInfo.offset)
                        outputBuffer?.limit(bufferInfo.offset + bufferInfo.size)

                        if (pts == 0L) {
                            pts = bufferInfo.presentationTimeUs
                        }
                        bufferInfo.presentationTimeUs = bufferInfo.presentationTimeUs - pts
                        mediaMuxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
                        //通知已经录制的时间
                        baseRender.get()
                            ?.onMediaInfoListener
                            ?.onMediaTime(bufferInfo.presentationTimeUs / (1000 * 1000))
                    }
                    //释放Buffer
                    videoCodec.releaseOutputBuffer(outputBufferIndex, false)
                    //出队
                    outputBufferIndex = videoCodec.dequeueOutputBuffer(bufferInfo, 0)
                }
            }


        }

    }

    fun exit() {
        isExit = true
    }

}

//音频编码线程
class AudioEncodecThread(private val baseRender: WeakReference<BaseMediaEncoder>) : Thread() {
    private val tag = "AudioEncodecThread"
    private var mediaMuxer: MediaMuxer = baseRender.get()?.mediaMuxer!!
    private var audioCodec: MediaCodec = baseRender.get()?.audioCodec!!
    private var bufferInfo = MediaCodec.BufferInfo()

    private var isExit = false
    private var pts: Long = 0
    var audioTrackIndex = -1

    override fun run() {
        super.run()
        _i(tag, "音频编码线程 开始录制")
        audioCodec.start()
        while (true) {
            if (isExit) {
                audioCodec.stop()
                audioCodec.release()
                baseRender.get()?.audioExit = true
                baseRender.get()?.stopMuxer()
                _i(tag, "MediaMuxer.release() 录制完成")
                break
            }
            //出队  输出到mediaBufferInfo
            var outputBufferIndex = audioCodec.dequeueOutputBuffer(bufferInfo, 0)
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                audioTrackIndex = mediaMuxer.addTrack(audioCodec.outputFormat)//音频轨道  大部分都是 1
                if (baseRender.get()?.videoEncodecThread?.videoTrackIndex != -1) {
                    mediaMuxer.start()
                    baseRender.get()?.muxerStart = true
                }
            } else {
                while (outputBufferIndex >= 0) {
                    if (baseRender.get()?.muxerStart == true) {
                        val outputBuffer = audioCodec.outputBuffers[outputBufferIndex]
                        outputBuffer?.position(bufferInfo.offset)
                        outputBuffer?.limit(bufferInfo.offset + bufferInfo.size)

                        if (pts == 0L) {
                            pts = bufferInfo.presentationTimeUs
                        }
                        bufferInfo.presentationTimeUs = bufferInfo.presentationTimeUs - pts
                        mediaMuxer.writeSampleData(audioTrackIndex, outputBuffer, bufferInfo)
                    }
                    //通知已经录制的时间
//                    baseRender.get()
//                        ?.onMediaInfoListener
//                        ?.onMediaTime(bufferInfo.presentationTimeUs / (1000 * 1000))
                    //释放Buffer
                    audioCodec.releaseOutputBuffer(outputBufferIndex, false)
                    //出队
                    outputBufferIndex = audioCodec.dequeueOutputBuffer(bufferInfo, 0)
                }
            }


        }

    }

    fun exit() {
        isExit = true
    }

}

