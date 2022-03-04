package com.android.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Created by xuzhb on 2021/12/29
 * Desc:View扩展函数
 */
//显示View
fun View?.visible() {
    this?.visibility = View.VISIBLE
}

//隐藏View
fun View?.invisible() {
    this?.visibility = View.INVISIBLE
}

//隐藏View
fun View?.gone() {
    this?.visibility = View.GONE
}

//防止多次点击
fun View?.clickDelay(delayTime: Long = 600, clickAction: (v: View) -> Unit) {
    this?.setOnClickListener {
        if (CheckFastClickUtil.isFastClick(delayTime)) {
            return@setOnClickListener
        }
        clickAction(it)
    }
}

/**  TextView扩展函数  */
//设置TextView的Drawable
fun TextView?.setDrawables(
    context: Context,
    @DrawableRes leftRes: Int = -1,
    @DrawableRes topRes: Int = -1,
    @DrawableRes rightRes: Int = -1,
    @DrawableRes bottomRes: Int = -1,
    padding: Int = -1
) {
    val left = if (leftRes == -1) null else ContextCompat.getDrawable(context, leftRes)
    val top = if (topRes == -1) null else ContextCompat.getDrawable(context, topRes)
    val right = if (rightRes == -1) null else ContextCompat.getDrawable(context, rightRes)
    val bottom = if (bottomRes == -1) null else ContextCompat.getDrawable(context, bottomRes)
    setDrawables(left, top, right, bottom, padding)
}

//设置TextView的Drawable
fun TextView?.setDrawables(
    left: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null,
    padding: Int = -1
) {
    if (left == null && top == null && right == null && bottom == null) {
        return
    }
    left?.setBounds(0, 0, left.minimumWidth, left.minimumHeight)
    top?.setBounds(0, 0, top.minimumWidth, top.minimumHeight)
    right?.setBounds(0, 0, right.minimumWidth, right.minimumHeight)
    bottom?.setBounds(0, 0, bottom.minimumWidth, bottom.minimumHeight)
    this?.setCompoundDrawables(left, top, right, bottom)
    if (padding != -1 && padding > 0) {
        this?.compoundDrawablePadding = padding
    }
}