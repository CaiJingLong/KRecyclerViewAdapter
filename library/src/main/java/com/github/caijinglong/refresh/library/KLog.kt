package com.github.caijinglong.refresh.library

import android.util.Log

/**
 * Created by cai on 2018/2/7.
 * 日志帮助类
 */
class KLog private constructor() {

    companion object {
        private const val TAG = "KLog"
        @JvmStatic
        var LOG = false

        @JvmStatic
        fun info(msg: Any?) {
            if (LOG) {
                Log.i(TAG, msg.toString())
            }
        }
    }
}