package com.android.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.appcompat.widget.AppCompatTextView

/**
 * Created by xuzhb on 2020/1/7
 * Desc:TextView文字滚动显示，即跑马灯效果（不需要获取焦点）
 */
class ScrollTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttrs: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttrs) {

    init {
        setSingleLine(true)
        ellipsize = TextUtils.TruncateAt.MARQUEE  //设置跑马灯效果
        marqueeRepeatLimit = -1  //设置循环滚动为无限循环
    }

    override fun isFocused(): Boolean = true

}