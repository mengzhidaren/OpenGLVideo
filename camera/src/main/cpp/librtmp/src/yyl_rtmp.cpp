//
// Created by Eericxu on 2018-12-17.
//
#include <jni.h>
#include <string>
#include "RtmpPush.h"

RtmpPush *rtmpPush = NULL;
RtmpCallBackJava *rtmpCallBackJava = NULL;
JavaVM *javaVM = NULL;
bool isExit= true;
extern "C"
JNIEXPORT void JNICALL
Java_com_camera_yyl_rtmp_RtmpVideoJNI_initPush(JNIEnv *env, jobject instance, jstring pushUrl_) {
    const char *pushUrl = env->GetStringUTFChars(pushUrl_, 0);
    if (rtmpCallBackJava == NULL) {
        isExit= false;
        rtmpCallBackJava = new RtmpCallBackJava(env, javaVM, &instance);
        rtmpPush = new RtmpPush(pushUrl, rtmpCallBackJava);
        rtmpPush->init();
    }
    env->ReleaseStringUTFChars(pushUrl_, pushUrl);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_camera_yyl_rtmp_RtmpVideoJNI_stopPush(JNIEnv *env, jobject instance) {
    if (rtmpPush != NULL&&!isExit) {
        isExit= true;
        rtmpPush->pushStop();
        delete (rtmpPush);
        delete (rtmpCallBackJava);
        rtmpPush = NULL;
        rtmpCallBackJava = NULL;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_camera_yyl_rtmp_RtmpVideoJNI_pushVideoSPSPPS(JNIEnv *env, jobject instance, jbyteArray sps_, jint sps_len,
                                                      jbyteArray pps_, jint pps_len) {
    jbyte *sps = env->GetByteArrayElements(sps_, NULL);
    jbyte *pps = env->GetByteArrayElements(pps_, NULL);

    if (rtmpPush != NULL&&!isExit) {
        rtmpPush->pushSPSPPS(reinterpret_cast<char *>(sps), sps_len, reinterpret_cast<char *>(pps), pps_len);
    }
    env->ReleaseByteArrayElements(sps_, sps, 0);
    env->ReleaseByteArrayElements(pps_, pps, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_camera_yyl_rtmp_RtmpVideoJNI_pushVideoInfo(JNIEnv *env, jobject instance, jbyteArray data_, jint data_len,
                                                    jboolean isKeyFrame) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    if (rtmpPush != NULL&&!isExit) {
        rtmpPush->pushVideoInfo(reinterpret_cast<char *>(data), data_len, isKeyFrame);
    }

    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_camera_yyl_rtmp_RtmpVideoJNI_pushAudioInfo(JNIEnv *env, jobject instance, jbyteArray data_, jint data_len) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    if (rtmpPush != NULL&&!isExit) {
        rtmpPush->pushAudioInfo(reinterpret_cast<char *>(data), data_len);
    }
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    javaVM = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("yyl  GetEnv  error")
        return -1;
    }
    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    javaVM = NULL;
}