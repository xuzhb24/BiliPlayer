package com.android.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Created by xuzhb on 2019/9/26
 * Desc:软键盘管理工具类
 */
object KeyboardUtil {

    //弹出软键盘
    fun showSoftInput(context: Context, view: View) {
        with(view) {
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
        }
        val manager = context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        manager?.showSoftInput(view, 0)
    }

    //延迟弹出软键盘，默认延迟200ms
    fun showSoftInputDelay(context: Context, view: View, delay: Long = 200L) {
        Handler().postDelayed({ showSoftInput(context, view) }, delay)
    }

    //收起软件盘
    fun hideSoftInput(context: Context, view: View) {
        val manager = context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        manager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    //复制内容到剪切板
    fun copyToClipboard(context: Context, data: String) {
        val cm = context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(null, data)
        cm.setPrimaryClip(clipData)
    }

}