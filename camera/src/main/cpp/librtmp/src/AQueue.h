//
// Created by Eericxu on 2018-12-17.
//

#ifndef OPENGLVIDEO_AQUEUE_H
#define OPENGLVIDEO_AQUEUE_H

#include "pthread.h"
#include "queue"


extern "C"{
#include "../rtmp.h"
};



class AQueue {
public:
    std::queue<RTMPPacket*> queuePacket;
    pthread_mutex_t mutexPacket;
    pthread_cond_t condPacket;

public:
    AQueue();
    ~AQueue();

    int putRtmpPacket(RTMPPacket *packet);
    RTMPPacket* getRtmpPacket();

    void clearQueue();
    void notifyQueue();


};


#endif //OPENGLVIDEO_AQUEUE_H
