//
// Created by Eericxu on 2018-12-17.
//

#include "AQueue.h"

AQueue::AQueue() {
    pthread_mutex_init(&mutexPacket, NULL);
    pthread_cond_init(&condPacket, NULL);
}

AQueue::~AQueue() {
    clearQueue();
    pthread_mutex_destroy(&mutexPacket);
    pthread_cond_destroy(&condPacket);
}

int AQueue::putRtmpPacket(RTMPPacket *packet) {
    pthread_mutex_lock(&mutexPacket);
    queuePacket.push(packet);
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

RTMPPacket *AQueue::getRtmpPacket() {

    pthread_mutex_lock(&mutexPacket);
    RTMPPacket *packet = NULL;
    if (!queuePacket.empty()) {
        packet = queuePacket.front();//取最上面
        queuePacket.pop();//弹出对像
    } else {
        pthread_cond_wait(&condPacket, &mutexPacket);
    }

    pthread_mutex_unlock(&mutexPacket);


    return packet;
}

void AQueue::clearQueue() {
    pthread_mutex_lock(&mutexPacket);


    while (true) {
        if (queuePacket.empty()) {
            break;
        }
        RTMPPacket *packet = queuePacket.front();//取最上面
        queuePacket.pop();//弹出对像
        RTMPPacket_Free(packet);
    }
    pthread_mutex_unlock(&mutexPacket);

}

void AQueue::notifyQueue() {
    pthread_mutex_lock(&mutexPacket);
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);
}
