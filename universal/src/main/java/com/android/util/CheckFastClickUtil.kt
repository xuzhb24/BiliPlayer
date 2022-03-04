package com.android.util

import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by xuzhb on 2020/7/14
 * Desc:快速点击检测工具
 */
object CheckFastClickUtil {

    private var mLastClickTime: Long = 0

    fun isFastClick(interval: Long = 400): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - mLastClickTime > interval) {
            mLastClickTime = currentTime;
            false
        } else {
            true
        }
    }

    //已经连续点击的次数，原子操作，是线程安全的
    private val mClickCount: AtomicInteger = AtomicInteger(0)

    //连续点击事件监听，clickCount：连续点击的次数
    fun setOnMultiClickListener(interval: Long = 400, listener: (clickCount: Int) -> Unit) {
        val currentClickTime = System.currentTimeMillis()
        val delay = currentClickTime - mLastClickTime
        if (delay <= interval) {
            mClickCount.incrementAndGet()
            listener.invoke(mClickCount.get())
        } else {
            mClickCount.set(1)
            listener.invoke(mClickCount.get())
        }
        mLastClickTime = currentClickTime
    }

}