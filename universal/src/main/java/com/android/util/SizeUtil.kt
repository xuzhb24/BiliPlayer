package com.android.util

import android.content.Context
import com.android.base.BaseApplication

/**
 * Created by xuzhb on 2019/8/1
 * Desc:尺寸信息单位转换
 */
object SizeUtil {

    fun px2dp(pxValue: Float, context: Context = BaseApplication.instance): Float {
        val density: Float = context.resources.displayMetrics.density
        return pxValue / density + 0.5f
    }

    fun dp2px(dpValue: Float, context: Context = BaseApplication.instance): Float {
        val density: Float = context.resources.displayMetrics.density
        return dpValue * density + 0.5f
    }

    fun px2sp(pxValue: Float, context: Context = BaseApplication.instance): Float {
        val scaledDensity: Float = context.resources.displayMetrics.scaledDensity
        return pxValue / scaledDensity + 0.5f
    }

    fun sp2px(spValue: Float, context: Context = BaseApplication.instance): Float {
        val scaledDensity: Float = context.resources.displayMetrics.scaledDensity
        return spValue * scaledDensity + 0.5f
    }

    fun px2dpInt(pxValue: Float, context: Context = BaseApplication.instance): Int {
        return px2dp(pxValue, context).toInt()
    }

    fun dp2pxInt(dpValue: Float, context: Context = BaseApplication.instance): Int {
        return dp2px(dpValue, context).toInt()
    }

    fun px2spInt(pxValue: Float, context: Context = BaseApplication.instance): Int {
        return px2sp(pxValue, context).toInt()
    }

    fun sp2pxInt(spValue: Float, context: Context = BaseApplication.instance): Int {
        return sp2px(spValue, context).toInt()
    }

}