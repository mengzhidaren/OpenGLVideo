package com.camera.yyl.rtmp.encodec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import com.camera.yyl.audio_record.AudioRecordUtils
import com.camera.yyl.egl.EGLSurfaceBase
import com.camera.yyl.egl.EGLThread
import com.camera.yyl.egl.EGLThread.Companion.RENDERMODE_CONTINUOUSLY
import com.camera.yyl.egl.Renderer
import com.yyl.demo.utils._i
import java.io.IOException
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGLContext


open class BaseRtmpEncoder : EGLSurfaceBase {
    private val tag = "BaseMediaEncoder"

    var surface: Surface? = null  //视频输入画布
    var baseEglContext: EGLContext? = null
    var render: Renderer? = null
    var renderMode = RENDERMODE_CONTINUOUSLY
    override fun baseSurface() = surface
    override fun baseEglContext() = baseEglContext
    override fun baseRender() = render
    override fun baseRenderMode() = renderMode

    private val audioRecordUtils by lazy {
        val audio = AudioRecordUtils()
        audio.onAudioRecordLisener = object : AudioRecordUtils.OnAudioRecordLisener {
            override fun onAudioRecordByte(audioData: ByteArray, readSize: Int) {
                putPCMData(audioData, readSize)
            }
        }
        audio
    }


    var onMediaInfoListener: OnMediaInfoListener? = null
    var videoExit = true
    var audioExit = true
    var videoCodec: MediaCodec? = null
    var audioCodec: MediaCodec? = null

    private var audioPts: Long = 0
    private var sampleRate: Int = 0
    var width = 0
    var height = 0

    private var eglThread: EGLThread? = null
    var videoEncodecThread: RtmpVideoEncodecThread? = null
    var audioEncodecThread: RtmpAudioEncodecThread? = null

    fun initEncodec(baseEglContext: EGLContext?, width: Int, height: Int) {
        this.baseEglContext = baseEglContext
        this.width = width
        this.height = height
        initVideoEncoder(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        initAudioEncoder(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2)
    }


    fun startRecord(): Boolean {
        surface?.apply {
            baseEglContext?.apply {
                videoExit = false
                audioExit = false
                eglThread = EGLThread(WeakReference(this@BaseRtmpEncoder))
                videoEncodecThread =
                        RtmpVideoEncodecThread(WeakReference(this@BaseRtmpEncoder))
                audioEncodecThread =
                        RtmpAudioEncodecThread(WeakReference(this@BaseRtmpEncoder))
                eglThread?.width = width
                eglThread?.height = height
                eglThread?.isChange = true
                eglThread?.start()
                videoEncodecThread?.start()
                audioEncodecThread?.start()
                audioRecordUtils.startRecord()
                _i(tag, "startRecord  success")
                return true
            }
        }
        return false
    }

    fun stopRecord() {
        audioRecordUtils.stopRecord()
        audioEncodecThread?.exit()
        videoEncodecThread?.exit()
        eglThread?.onDestroy()
        videoEncodecThread = null
        audioEncodecThread = null
        eglThread = null
    }


    //给音频喂数据
    fun putPCMData(buffer: ByteArray, size: Int) {
        if (audioRecordUtils.isRecord() && size > 0) {
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
    ////通知已经录制的时间
    fun onMediaTime(times: Long)

    ////推送头信息
    fun onSPSPPSInfo(sps: ByteArray, pps: ByteArray)

    //推送 H264 每帧信息
    fun onVideoInfo(data: ByteArray, keyFrame: Boolean)

    //推送 acc 每帧信息
    fun onAudioInfo(data: ByteArray)

}

//视频编码线程
class RtmpVideoEncodecThread(private val baseRender: WeakReference<BaseRtmpEncoder>) : Thread() {
    private val tag = "VideoEncodecThread"
    private var videoCodec: MediaCodec = baseRender.get()?.videoCodec!!
    private var bufferInfo = MediaCodec.BufferInfo()

    private var isExit = false
    private var pts: Long = 0
    private lateinit var sps: ByteArray
    private lateinit var pps: ByteArray

    //是否是关键帧
    private var isKeyFrame = false

    private fun release() {
        videoCodec.stop()
        videoCodec.release()
        baseRender.get()?.videoExit = true
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
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {//获取SPS、PPS
                val spsb = videoCodec.outputFormat.getByteBuffer("csd-0")
                sps = ByteArray(spsb.remaining())
                spsb.get(sps, 0, sps.size)

                val ppsb = videoCodec.outputFormat.getByteBuffer("csd-1")
                pps = ByteArray(ppsb.remaining())
                ppsb.get(pps, 0, pps.size)


                _i(tag, "\n sps=${byteToHex(sps)}   \n pps=${byteToHex(pps)} ")
            } else {
                while (outputBufferIndex >= 0) {
                    val outputBuffer = videoCodec.outputBuffers[outputBufferIndex]

                    outputBuffer?.let {
                        it.position(bufferInfo.offset)
                        it.limit(bufferInfo.offset + bufferInfo.size)

                        val data = ByteArray(it.remaining())
                        it.get(data, 0, data.size)
                        _i(tag, "video frame data=${byteToHex(data)} ")
                        if (pts == 0L) {
                            pts = bufferInfo.presentationTimeUs
                        }
                        bufferInfo.presentationTimeUs = bufferInfo.presentationTimeUs - pts

                        baseRender.get()?.onMediaInfoListener?.apply {
                            if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                                isKeyFrame = true
                                onSPSPPSInfo(sps, pps)
                            }
                            onVideoInfo(data, isKeyFrame)//
                            onMediaTime(bufferInfo.presentationTimeUs / (1000 * 1000))//通知已经录制的时间
                        }
                        it
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
class RtmpAudioEncodecThread(private val baseRender: WeakReference<BaseRtmpEncoder>) : Thread() {
    private val tag = "AudioEncodecThread"
    private var audioCodec: MediaCodec = baseRender.get()?.audioCodec!!
    private var bufferInfo = MediaCodec.BufferInfo()

    private var isExit = false
    private var pts: Long = 0

    override fun run() {
        super.run()
        _i(tag, "音频编码线程 开始录制")
        audioCodec.start()
        while (true) {
            if (isExit) {
                audioCodec.stop()
                audioCodec.release()
                baseRender.get()?.audioExit = true
                _i(tag, "MediaMuxer.release() 录制完成")
                break
            }
            //出队  输出到mediaBufferInfo
            var outputBufferIndex = audioCodec.dequeueOutputBuffer(bufferInfo, 0)
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

            } else {
                while (outputBufferIndex >= 0) {
                    val outputBuffer = audioCodec.outputBuffers[outputBufferIndex]

                    outputBuffer?.let {
                        it.position(bufferInfo.offset)
                        it.limit(bufferInfo.offset + bufferInfo.size)
                        val data = ByteArray(it.remaining())
                        it.get(data, 0, data.size)
                        baseRender.get()?.onMediaInfoListener?.onAudioInfo(data)

                        _i(tag, "audio frame data=${byteToHex(data)} ")
                    }

                    if (pts == 0L) {
                        pts = bufferInfo.presentationTimeUs
                    }
                    bufferInfo.presentationTimeUs = bufferInfo.presentationTimeUs - pts


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

fun byteToHex(bytes: ByteArray): String {
    val stringBuffer = StringBuffer()
    for (i in bytes.indices) {
        val hex = Integer.toHexString(bytes[i].toInt())
        if (hex.length == 1) {
            stringBuffer.append("0$hex")
        } else {
            stringBuffer.append(hex)
        }
        if (i > 20) {
            break
        }
    }
    return stringBuffer.toString()
}