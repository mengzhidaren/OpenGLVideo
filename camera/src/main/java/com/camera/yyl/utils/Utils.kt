package com.yyl.demo.utils

import android.content.Context
import android.util.Log

class Utils {
}

fun _i(msg:String){
    _i("yyl",msg)
}

fun _i(tag:String,msg:String){
    Log.i(tag,msg)
}

fun getScreenWidth(context: Context): Int {
    val metric = context.resources.displayMetrics
    return metric.widthPixels
}

fun getScreenHeight(context: Context): Int {
    val metric = context.resources.displayMetrics
    return metric.heightPixels
}