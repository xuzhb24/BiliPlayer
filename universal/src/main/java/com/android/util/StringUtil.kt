package com.android.util

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.*
import android.view.View

/**
 * Created by xuzhb on 2019/10/28
 * Desc:字符串工具类
 */
object StringUtil {

    //纯Kotlin判断是否是空字符，相当于Android中的TextUtils.isEmpty
    fun isEmpty(s: String?): Boolean {
        s?.let {
            for (i in 0..it.length - 1) {
                if (!Character.isWhitespace(it[i])) {
                    return false
                }
            }
        }
        return true
    }

    //设置同一个字符串不同字体大小和颜色
    fun createTextSpan(
        content: String,
        startIndex: Int,
        endIndex: Int,
        textColor: Int,
        textSize: Int,
        textStyle: Int = Typeface.NORMAL,
        showUnderLine: Boolean = false,         //是否设置下划线
        listener: ((v: View) -> Unit)? = null  //设置点击事件
    ): SpannableString {
        var endIndex = endIndex
        if (endIndex > content.length) {
            endIndex = content.length
        }
        val spannableString = SpannableString(content)
        with(spannableString) {
            //Spanned.SPAN_INCLUSIVE_EXCLUSIVE：从起始下标到终止下标，包括起始下标，不包括终止下标
            //Spanned.SPAN_INCLUSIVE_INCLUSIVE：从起始下标到终止下标，同时包括起始下标和终止下标
            //Spanned.SPAN_EXCLUSIVE_EXCLUSIVE：从起始下标到终止下标，但都不包括起始下标和终止下标
            //Spanned.SPAN_EXCLUSIVE_INCLUSIVE：从起始下标到终止下标，不包括起始下标，包括终止下标

            //设置不同字体颜色
            setSpan(ForegroundColorSpan(textColor), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            //设置不同字体大小
            setSpan(AbsoluteSizeSpan(textSize), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            //设置不同的textStyle
            setSpan(StyleSpan(textStyle), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            //是否设置下划线
            if (showUnderLine) {
                setSpan(UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            //设置点击事件
            listener?.let {
                setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        it.invoke(widget)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = textColor  //设置字体颜色
                        ds.isUnderlineText = showUnderLine  //是否显示下划线
                    }

                }, startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
        return spannableString
    }

}