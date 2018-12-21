package com.camera.yyl.audio_record;

import com.camera.yyl.rtmp.RtmpPushVideo;

public class OpenSLESRecord {

    public static final OpenSLESRecord instance=new OpenSLESRecord();



    private OpenSLESRecord() {
        RtmpPushVideo soLoad = RtmpPushVideo.Companion.getInstance();
    }







    public native void startRecord(String path);

    public native void stopRecord();
    public native boolean isRecord();

}
