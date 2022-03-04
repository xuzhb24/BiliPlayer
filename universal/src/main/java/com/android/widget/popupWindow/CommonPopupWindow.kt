package com.android.widget.popupWindow

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.annotation.FloatRange
import com.android.widget.ViewHolder

/**
 * Created by xuzhb on 2019/9/3
 * Desc:通用的PopupWindow
 */
class CommonPopupWindow constructor(context: Context) : PopupWindow() {

    private var mWindowHelper: WindowHelper? = null

    init {
        if (context is Activity) {
            mWindowHelper = WindowHelper(context)
        }
    }

    override fun dismiss() {
        super.dismiss()
        mWindowHelper?.setBackGroundAlpha(1.0f)
    }

    //默认静态内部类，如果加上inner，即inner class则是非静态内部类
    class Builder(private var mContext: Context) {

        private var mLayoutId: Int = -1          //弹窗的布局id
        private var mWidth: Int = 0              //弹窗的宽度
        private var mHeight: Int = 0             //弹窗的高度
        private var mAlpha: Float = 1.0f        //背景透明度
        private var mAnimationStyle: Int = -1   //动画
        private var mTouchable: Boolean = true  //是否可点击
        private var mBackgroundDrawable: Drawable = ColorDrawable(0x00000000)  //背景drawable
        private var mOnViewListener: ((holder: ViewHolder, popupWindow: PopupWindow) -> Unit)? = null

        //通过布局id设置弹窗布局的View
        fun setContentView(layoutId: Int): Builder {
            mLayoutId = layoutId
            return this
        }

        //设置宽高
        fun setViewParams(width: Int, height: Int): Builder {
            mWidth = width
            mHeight = height
            return this
        }

        //设置外部区域背景透明度，0：完全不透明，1：完全透明
        fun setBackGroundAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Builder {
            mAlpha = alpha
            return this
        }

        //设置显示和消失动画
        fun setAnimationStyle(animationStyle: Int): Builder {
            mAnimationStyle = animationStyle
            return this
        }

        //设置外部区域是否可点击取消对话框
        fun setOutsideTouchable(touchable: Boolean): Builder {
            mTouchable = touchable
            return this
        }

        //设置弹窗背景
        fun setBackgroundDrawable(drawable: Drawable): Builder {
            mBackgroundDrawable = drawable
            return this
        }

        //设置事件监听
        fun setOnViewListener(listener: (holder: ViewHolder, popupWindow: PopupWindow) -> Unit): Builder {
            mOnViewListener = listener
            return this
        }

        fun build(): CommonPopupWindow {
            val popupWindow = CommonPopupWindow(mContext)
            with(popupWindow) {
                //设置contentView
                if (mLayoutId != -1) {
                    val view = LayoutInflater.from(mContext).inflate(mLayoutId, null)
                    //因为PopupWindow在显示前无法获取准确的宽高值(getWidth和getHeight可能会返回0或-2），
                    //通过提前测量contentView的宽高就可以通过getMeasuredWidth和getMeasuredHeight获取contentView的宽高
                    view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                    contentView = view
                } else {
                    throw NullPointerException("The contentView of PopupWindow is null")
                }
                //设置宽高，没有设置宽高的话默认为ViewGroup.LayoutParams.WRAP_CONTENT
                if (mWidth == 0) {
                    width = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    width = mWidth
                }
                if (mHeight == 0) {
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    height = mHeight
                }
                mWindowHelper?.setBackGroundAlpha(mAlpha)  //设置外部区域的透明度
                //设置弹窗显示和消失的动画效果
                if (mAnimationStyle != -1) {
                    animationStyle = mAnimationStyle
                }
                //设置弹窗背景，如果contentView对应的View已经设置android:background可能会覆盖弹窗背景
                setBackgroundDrawable(mBackgroundDrawable)
                //设置点击外部区域是否可取消弹窗
                isOutsideTouchable = mTouchable
                isFocusable = mTouchable
                //设置contentView上控件的事件监听
                mOnViewListener?.invoke(ViewHolder(contentView), this)
            }
            return popupWindow
        }

    }

}