package com.android.util

import android.view.View
import java.util.concurrent.atomic.AtomicInteger

/**
 * Create by xuzhb on 2020/1/22
 * Desc:连续点击事件监听器
 */
abstract class OnMultiClickListener : View.OnClickListener {

    //上次点击的时间
    private var mLastClickTime: Long = 0L
    //已经连续点击的次数，原子操作，是线程安全的
    private val mClickCount: AtomicInteger = AtomicInteger(0)

    override fun onClick(v: View?) {
        val currentClickTime = System.currentTimeMillis()
        val delay = currentClickTime - mLastClickTime
        if (delay <= getClickInterval()) {
            mClickCount.incrementAndGet()
            onMultiClick(v, mClickCount.get())
        } else {
            mClickCount.set(1)
            onMultiClick(v, mClickCount.get())
        }
        mLastClickTime = currentClickTime
    }

    //连续点击的最大时间间隔，如果两次点击的时间间隔超过这个值，则不认为是连续点击，
    //默认是400毫秒，但可在实现时重新指定
    protected open fun getClickInterval(): Int = 400

    //连续点击事件回调，clickCount:连续点击的次数
    abstract fun onMultiClick(v: View?, clickCount: Int)
}