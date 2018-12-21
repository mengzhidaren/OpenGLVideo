//
// Created by Eericxu on 2018-12-15.
//

#include "RecordBuffer.h"

short *RecordBuffer::getRecordBuffer() {
    index++;
    if(index > 1)
    {
        index = 1;
    }

    return buffer[index];
}

short *RecordBuffer::getNowBuffer() {
    return buffer[index];
}

RecordBuffer::RecordBuffer(int bufferSize) {
    buffer=new short *[2];
    for(int i=0;i<2;i++){
        buffer[i]=new short[bufferSize];
    }
}

RecordBuffer::~RecordBuffer() {
    for(int i=0;i<2;i++){
       delete buffer[i];
    }
    delete buffer;
}
