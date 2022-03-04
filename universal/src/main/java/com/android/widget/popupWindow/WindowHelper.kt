package com.android.widget.popupWindow

import android.app.Activity
import androidx.annotation.FloatRange

/**
 * Created by xuzhb on 2019/9/3
 * Desc:Window辅助类，实现背景灰色效果
 */
class WindowHelper(private var mActivity: Activity) {

    //设置外部区域背景透明度，0：完全不透明，1：完全透明
    fun setBackGroundAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        val window = mActivity.window
        val lp = window.attributes
        lp.alpha = alpha
        window.attributes = lp
    }

}