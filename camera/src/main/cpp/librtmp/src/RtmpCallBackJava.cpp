//
// Created by Eericxu on 2018-12-20.
//

#include "RtmpCallBackJava.h"

RtmpCallBackJava::RtmpCallBackJava(JNIEnv *jniEnv, JavaVM *javaVM, jobject *jobj) {

    this->jniEnv = jniEnv;
    this->javaVM = javaVM;
    this->jobj = jniEnv->NewGlobalRef(*jobj);

    jclass jclz = jniEnv->GetObjectClass(this->jobj);


    this->jmethod_connect_event = jniEnv->GetMethodID(jclz, "onConnectEvent", "(I)V");
}

RtmpCallBackJava::~RtmpCallBackJava() {

}

void RtmpCallBackJava::onConnectEvent(int threadType, int event) {
    if(threadType==THREAD_CHILD){
        JNIEnv *jniEnv1;//用局布的 JNIEnv  变量
        if(javaVM->AttachCurrentThread(&jniEnv1,0)!=JNI_OK){
            return;
        }
        jniEnv1->CallVoidMethod(jobj,jmethod_connect_event,event);
        javaVM->DetachCurrentThread();
    }else{
        jniEnv->CallVoidMethod(jobj,jmethod_connect_event,event);
    }

}
