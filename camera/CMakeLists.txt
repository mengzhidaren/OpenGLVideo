# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.4.1)
#关闭 SSL   设置不适用OpenSSL
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")

add_library(
        audioRecord

        SHARED
        #AudioRecord
        src/main/cpp/AudioRecord/AudioRecord.cpp
        src/main/cpp/AudioRecord/RecordBuffer.cpp

        #rtmp
        src/main/cpp/librtmp/amf.c
        src/main/cpp/librtmp/hashswf.c
        src/main/cpp/librtmp/log.c
        src/main/cpp/librtmp/parseurl.c
        src/main/cpp/librtmp/rtmp.c
        #rtmp  main
        src/main/cpp/librtmp/src/AQueue.cpp
        src/main/cpp/librtmp/src/RtmpPush.cpp
        src/main/cpp/librtmp/src/yyl_rtmp.cpp
        src/main/cpp/librtmp/src/RtmpCallBackJava.cpp


)




target_link_libraries(
        audioRecord
        OpenSLES
        log)