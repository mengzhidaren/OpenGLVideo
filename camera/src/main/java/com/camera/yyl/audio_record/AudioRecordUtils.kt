package com.camera.yyl.audio_record

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.yyl.demo.utils._i

class AudioRecordUtils {
    private val bufferSizeInBytes: Int = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private fun getAudioRecord() = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        44100,
        AudioFormat.CHANNEL_IN_STEREO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSizeInBytes
    )

    private var _isRun = false
    private var readSize = 0

    var onAudioRecordLisener: OnAudioRecordLisener? = null


    fun startRecord() {

        object : Thread() {
            override fun run() {
                super.run()
                _i("startRecord")
                _isRun = true
                val audioRecord = getAudioRecord()
                audioRecord.startRecording()
                val audiodata = ByteArray(bufferSizeInBytes)
                while (_isRun) {
                    // val audiodata = ByteArray(bufferSizeInBytes)  //在这里有内存抖动
                    readSize = audioRecord.read(audiodata, 0, bufferSizeInBytes)
                    onAudioRecordLisener?.onAudioRecordByte(audiodata, readSize)
                }
                audioRecord.stop()
                audioRecord.release()
                _isRun=false
                _i("stopRecord")
            }
        }.start()
    }
    fun isRecord()=_isRun
    fun stopRecord() {
        _isRun = false
    }

    interface OnAudioRecordLisener {
        fun onAudioRecordByte(audioData: ByteArray, readSize: Int)
    }
}

