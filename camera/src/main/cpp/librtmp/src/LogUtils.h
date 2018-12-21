//
// Created by Eericxu on 2018-12-15.
//
#pragma once
#ifndef OPENGLVIDEO_LOGUTILS_H
#define OPENGLVIDEO_LOGUTILS_H
#include <android/log.h>

#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_DEBUG,"yyl",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"yyl",FORMAT,##__VA_ARGS__);


#endif //OPENGLVIDEO_LOGUTILS_H
