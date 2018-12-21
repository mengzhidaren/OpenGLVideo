package com.camera.yyl.audio_record

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.camera.yyl.R
import com.yyl.demo.utils._i
import kotlinx.android.synthetic.main.activity_audio_record.*

class AudioRecordActivity : AppCompatActivity() {
    val filePath = Environment.getExternalStorageDirectory().absolutePath + "/yylAudio.pcm"

    val audioRecordUtils=AudioRecordUtils()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)

        audioRecord.setOnClickListener {
            if(audioRecordUtils.isRecord()){
                audioRecordUtils.stopRecord()
            }else{
                audioRecordUtils.startRecord()
            }

        }


        openSLRecord.setOnClickListener {
            if(OpenSLESRecord.instance.isRecord){
                OpenSLESRecord.instance.stopRecord()
            }else{
                OpenSLESRecord.instance.startRecord(filePath)
            }
        }
        audioRecordUtils.onAudioRecordLisener=object :AudioRecordUtils.OnAudioRecordLisener{
            override fun onAudioRecordByte(audioData: ByteArray, readSize: Int) {
                _i("onAudioRecordByte     readSize=$readSize")
            }
        }
    }
}
