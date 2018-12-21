//
// Created by Eericxu on 2018-12-15.
//

#ifndef OPENGLVIDEO_RECORDBUFFER_H
#define OPENGLVIDEO_RECORDBUFFER_H


class RecordBuffer {
public:
    short **buffer;
    int index = -1;
public:
    RecordBuffer(int bufferSize);

    ~RecordBuffer();

    short *getRecordBuffer();

    short *getNowBuffer();
};


#endif //OPENGLVIDEO_RECORDBUFFER_H
