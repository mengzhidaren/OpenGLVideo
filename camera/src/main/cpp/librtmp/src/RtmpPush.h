//
// Created by Eericxu on 2018-12-17.
//

#ifndef OPENGLVIDEO_RTMPPUSH_H
#define OPENGLVIDEO_RTMPPUSH_H

#include "string.h"
#include "AQueue.h"
#include <malloc.h>
#include "pthread.h"
#include "LogUtils.h"
#include "RtmpCallBackJava.h"
extern "C"{
#include "../rtmp.h"
};


class RtmpPush {
public:
    RTMP *rtmp=NULL;
    char *url=NULL;
    AQueue *queue=NULL;
    pthread_t push_thread;
    RtmpCallBackJava *callBackJava=NULL;
    bool startPushing= false;
    uint32_t startTime = 0;
public:
    RtmpPush(const char *url,RtmpCallBackJava *callBackJava);
    ~RtmpPush();
    void init();
    void pushSPSPPS(char *sps,int sps_len,char *pps,int pps_len);

    void pushVideoInfo(char *data,int data_len,bool isKeyFrame);
    void pushAudioInfo(char *data,int data_len);
    void pushStop();
};


#endif //OPENGLVIDEO_RTMPPUSH_H
