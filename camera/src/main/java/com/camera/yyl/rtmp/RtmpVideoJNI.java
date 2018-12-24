package com.camera.yyl.rtmp;

import android.text.TextUtils;

public abstract class RtmpVideoJNI {


    static {
        System.loadLibrary("audioRecord");
    }

    public void startPush(String pushUrl) {
        if (TextUtils.isEmpty(pushUrl)) return;
        initPush(pushUrl);
    }

    public void onPushSPSPPS(byte[] sps, byte[] pps) {
        if (sps != null && pps != null)
            pushVideoSPSPPS(sps, sps.length, pps, pps.length);
    }

    public void onPushVideoInfoData(byte[] data, boolean isKeyFrame) {
        pushVideoInfo(data, data.length, isKeyFrame);
    }

    public void onPushAudioInfoData(byte[] data) {
        pushAudioInfo(data, data.length);
    }

    private native void initPush(String pushUrl);

    private native void stopPush();

    private native void pushVideoSPSPPS(byte[] sps, int sps_len, byte[] pps, int pps_len);

    private native void pushVideoInfo(byte[] data, int data_len, boolean isKeyFrame);

    private native void pushAudioInfo(byte[] data, int data_len);


    abstract void onConnectEvent(int action);

}
