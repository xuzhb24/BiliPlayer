package com.android.project.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * Created by xuzhb on 2021/3/30
 */
class PhotoViewPager @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return try {
            super.onInterceptTouchEvent(ev)
        } catch (e: Exception) {
            false
        }
    }

}