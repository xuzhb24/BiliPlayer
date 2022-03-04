package com.android.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.android.base.BaseApplication
import com.android.universal.R

/**
 * Created by xuzhb on 2019/9/26
 * Desc:自定义Toast
 */
object ToastUtil {

    fun showToast(
        text: CharSequence,
        isCenter: Boolean = false,
        longToast: Boolean = false,
        context: Context = BaseApplication.instance
    ) {
        val duration = if (longToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        val toast = Toast.makeText(context.applicationContext, text, duration)
        val layout = LayoutInflater.from(context.applicationContext).inflate(R.layout.layout_toast, null)
        val toastTv: TextView = layout.findViewById(R.id.toast_tv)
        toastTv.text = text
        val toastShape = GradientDrawable()
        toastShape.setColor(Color.parseColor("#AA1F2735"))
        toastShape.cornerRadius = SizeUtil.dp2px(3f)
        layout.background = toastShape
        if (isCenter) {
            toast.setGravity(Gravity.CENTER, 0, 0)
        }
        toast.view = layout
        toast.show()
    }

    fun showToast(
        text: CharSequence,
        gravity: Int = Gravity.CENTER,
        xOffset: Int = 0,
        yOffset: Int = 0,
        longToast: Boolean = false,
        context: Context = BaseApplication.instance
    ) {
        val duration = if (longToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        val toast = Toast.makeText(context.applicationContext, text, duration)
        val layout = LayoutInflater.from(context.applicationContext).inflate(R.layout.layout_toast, null)
        val toastTv: TextView = layout.findViewById(R.id.toast_tv)
        toastTv.text = text
        val toastShape = GradientDrawable()
        toastShape.setColor(Color.parseColor("#AA1F2735"))
        toastShape.cornerRadius = SizeUtil.dp2px(3f)
        layout.background = toastShape
        toast.setGravity(gravity, xOffset, yOffset)
        toast.view = layout
        toast.show()
    }

    fun toast(text: CharSequence, context: Context = BaseApplication.instance) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun longToast(text: CharSequence, context: Context = BaseApplication.instance) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

}