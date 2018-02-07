package com.github.caijinglong.refresh.library

import android.content.Context
import android.graphics.Color
import android.support.v4.widget.CircularProgressDrawable
import android.util.AttributeSet
import android.widget.ImageView

/**
 * Created by cjl on 2018/2/7.
 */
class KProgressBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    var progressDrawable: CircularProgressDrawable

    init {
        progressDrawable = CircularProgressDrawable(getContext())
        progressDrawable.setStyle(CircularProgressDrawable.DEFAULT)
        progressDrawable.setColorSchemeColors(Color.parseColor("#009944"))
        setImageDrawable(progressDrawable)
    }

    fun show() {
        progressDrawable.start()
    }

    fun hide() {
        progressDrawable.stop()
    }

}