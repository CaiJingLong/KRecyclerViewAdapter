package com.github.caijinglong.refresh.library

import android.util.Log

/**
 * Created by cai on 2018/2/7.
 */
class KLog private constructor() {

    companion object {
        private const val TAG = "KLog"
        @JvmStatic
        var LOG = true

        @JvmStatic
        fun info(msg: Any?) {
            if (LOG) {
                Log.i(TAG, msg.toString())
            }
        }
    }
}