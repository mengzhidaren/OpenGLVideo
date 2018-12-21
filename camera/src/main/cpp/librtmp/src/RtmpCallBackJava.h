//
// Created by Eericxu on 2018-12-20.
//

#ifndef OPENGLVIDEO_RTMPCALLBACKJAVA_H
#define OPENGLVIDEO_RTMPCALLBACKJAVA_H


#include <jni.h>
#define CONNECT_READY 0
#define CONNECT_SUCCESS 1
#define CONNECT_FAIL 2


#define THREAD_MAIN 1
#define THREAD_CHILD 2

class RtmpCallBackJava {
public:
    JNIEnv *jniEnv=NULL;
    JavaVM *javaVM=NULL ;
    jobject jobj;
    jmethodID  jmethod_connect_event;

public:

    RtmpCallBackJava( JNIEnv *jniEnv, JavaVM *javaVM,  jobject *jobj);
    ~RtmpCallBackJava();

    void onConnectEvent(int threadType,int event);




};


#endif //OPENGLVIDEO_RTMPCALLBACKJAVA_H
