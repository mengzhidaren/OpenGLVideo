
#include <jni.h>
#include <string>

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include "RecordBuffer.h"
#include "LogUtils.h"

SLObjectItf slObjectEngine = NULL;
SLEngineItf engineItf = NULL;

SLObjectItf recordObj = NULL;
SLRecordItf recordItf = NULL;

SLAndroidSimpleBufferQueueItf recorderBufferQueue = NULL;

RecordBuffer *recordBuffer;

FILE *pcmFile = NULL;

bool _stop = false;
bool _isRun = false;


void bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    fwrite(recordBuffer->getNowBuffer(), 1, 4096, pcmFile);
    if (_stop) {
        _isRun= false;
        LOGE("录制完成");
        (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_STOPPED);
        //
        (*recordObj)->Destroy(recordObj);
        recordObj = NULL;
        recordItf = NULL;
        (*slObjectEngine)->Destroy(slObjectEngine);
        slObjectEngine = NULL;
        engineItf = NULL;
        delete (recordBuffer);
    } else {
        LOGE("正在录制");
        (*recorderBufferQueue)->Enqueue(recorderBufferQueue, recordBuffer->getRecordBuffer(), 4096);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_camera_yyl_audio_1record_OpenSLESRecord_startRecord(JNIEnv *env, jobject instance, jstring path_) {

    if (_isRun) {
        return;
    }
    const char *path = env->GetStringUTFChars(path_, 0);
    _stop = false;
    _isRun=true;
    pcmFile = fopen(path, "w");
    recordBuffer = new RecordBuffer(4096);
    //创建 Engine
    slCreateEngine(&slObjectEngine, 0, NULL, 0, NULL, NULL);
    (*slObjectEngine)->Realize(slObjectEngine, SL_BOOLEAN_FALSE);
    (*slObjectEngine)->GetInterface(slObjectEngine, SL_IID_ENGINE, &engineItf);

    //NDK官方demo里的配置
    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE,
                                      SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT,
                                      NULL};
    SLDataSource audioSrc = {&loc_dev, NULL};


    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            2
    };

    //音频格式设置源码  SLDataFormat_PCM
    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1,                 // PCM格式    2声道    采样率441000
            SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,  //16位样本格式
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,    //左右声道
            SL_BYTEORDER_LITTLEENDIAN           //小端对齐   在内存中的对齐方式  (16位内存中小端对齐)
    };

    SLDataSink audioSnk = {&loc_bq, &format_pcm};

    const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};

    //创建录音器
    (*engineItf)->CreateAudioRecorder(engineItf, &recordObj, &audioSrc, &audioSnk, 1, id, req);
    (*recordObj)->Realize(recordObj, SL_BOOLEAN_FALSE);
    (*recordObj)->GetInterface(recordObj, SL_IID_RECORD, &recordItf);

    (*recordObj)->GetInterface(recordObj, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &recorderBufferQueue);


    (*recorderBufferQueue)->Enqueue(recorderBufferQueue, recordBuffer->getRecordBuffer(), 4096);

    (*recorderBufferQueue)->RegisterCallback(recorderBufferQueue, bqRecorderCallback, NULL);

    (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_RECORDING);

    env->ReleaseStringUTFChars(path_, path);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_camera_yyl_audio_1record_OpenSLESRecord_stopRecord(JNIEnv *env, jobject instance) {
    _stop = true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_camera_yyl_audio_1record_OpenSLESRecord_isRecord(JNIEnv *env, jobject instance) {
    return _isRun;
}

