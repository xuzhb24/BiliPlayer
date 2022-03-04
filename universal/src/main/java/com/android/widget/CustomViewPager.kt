package com.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * Created by xuzhb on 2019/9/7
 * Desc:控制ViewPager是否可以左右滑动
 */
class CustomViewPager @JvmOverloads constructor(
    context: Context, attrs: AttributeSet
) : ViewPager(context, attrs) {

    var isScrollable: Boolean = true //控制ViewPager是否可以左右滑动

    /**
     * dispatchTouchEvent一般情况不做处理，如果修改了默认的返回值，子View都无法收到事件
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 是否拦截
     * 拦截:会走到自己的onTouchEvent方法里面来
     * 不拦截:事件传递给子View
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // return false;//可行,不拦截事件,
        // return true;//不行,子View无法处理事件
        //return super.onInterceptTouchEvent(ev);//不行,会有细微移动
        if (isScrollable) {
            try {
                return super.onInterceptTouchEvent(ev)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    /**
     * 是否消费事件
     * 消费:事件就结束
     * 不消费:往父控件传
     */
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        //return false;// 可行,不消费,传给父控件
        //return true;// 可行,消费,拦截事件
        //super.onTouchEvent(ev); //不行,
        //虽然onInterceptTouchEvent中拦截了,
        //但是如果viewpage里面子控件不是viewgroup,还是会调用这个方法.
        return if (isScrollable) {
            super.onTouchEvent(ev)
        } else {
            true// 可行,消费,拦截事件
        }
    }

    /**
     * 解决点击选项卡切换多页闪烁的问题
     */
    override fun setCurrentItem(item: Int) {
        super.setCurrentItem(item, false)
    }

}
