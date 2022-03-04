package com.android.util

import android.view.View
import android.view.ViewGroup

/**
 * Created by xuzhb on 2020/3/29
 * Desc:布局参数工具类
 */
object LayoutParamsUtil {

    private const val NOT_SET = -Int.MAX_VALUE

    //设置View的宽度
    fun setWidth(view: View, width: Int) {
        setLayoutParams(view, width, NOT_SET)
    }

    //设置View的高度
    fun setHeight(view: View, height: Int) {
        setLayoutParams(view, NOT_SET, height)
    }

    //设置View的宽高
    fun setLayoutParams(view: View, width: Int, height: Int) {
        val params = getMarginLayoutParams(view)
        if (width != NOT_SET) {
            params.width = width
        }
        if (height != NOT_SET) {
            params.height = height
        }
        view.requestLayout()
    }

    //设置View的leftMargin
    fun setMarginLeft(view: View, leftMargin: Int) {
        setMargin(view, leftMargin, NOT_SET, NOT_SET, NOT_SET)
    }

    //设置View的topMargin
    fun setMarginTop(view: View, topMargin: Int) {
        setMargin(view, NOT_SET, topMargin, NOT_SET, NOT_SET)
    }

    //设置View的rightMargin
    fun setMarginRight(view: View, rightMargin: Int) {
        setMargin(view, NOT_SET, NOT_SET, rightMargin, NOT_SET)
    }

    //设置View的bottomMargin
    fun setMarginBottom(view: View, bottomMargin: Int) {
        setMargin(view, NOT_SET, NOT_SET, NOT_SET, bottomMargin)
    }

    //设置View的Margin
    fun setMargin(view: View, leftMargin: Int, topMargin: Int, rightMargin: Int, bottomMargin: Int) {
        val params = getMarginLayoutParams(view)
        if (leftMargin != NOT_SET) {
            params.leftMargin = leftMargin
        }
        if (topMargin != NOT_SET) {
            params.topMargin = topMargin
        }
        if (rightMargin != NOT_SET) {
            params.rightMargin = rightMargin
        }
        if (bottomMargin != NOT_SET) {
            params.bottomMargin = bottomMargin
        }
        view.requestLayout()
    }

    //增加View的leftMargin
    fun addMarginLeft(view: View, leftMargin: Int) {
        addMargin(view, leftMargin, 0, 0, 0)
    }

    //增加View的topMargin
    fun addMarginTop(view: View, topMargin: Int) {
        addMargin(view, 0, topMargin, 0, 0)
    }

    //增加View的rightMargin
    fun addMarginRight(view: View, rightMargin: Int) {
        addMargin(view, 0, 0, rightMargin, 0)
    }

    //增加View的bottomMargin
    fun addMarginBottom(view: View, bottomMargin: Int) {
        addMargin(view, 0, 0, 0, bottomMargin)
    }

    //增加View的Margin，传负数表示减少View的Margin
    fun addMargin(view: View, leftMargin: Int, topMargin: Int, rightMargin: Int, bottomMargin: Int) {
        val params = getMarginLayoutParams(view)
        params.leftMargin += leftMargin
        params.topMargin += topMargin
        params.rightMargin += rightMargin
        params.bottomMargin += bottomMargin
        view.requestLayout()
    }

    fun getMarginLayoutParams(view: View) = view.layoutParams as ViewGroup.MarginLayoutParams

    //设置View的paddingLeft
    fun setPaddingLeft(view: View, left: Int) {
        setPadding(view, left, NOT_SET, NOT_SET, NOT_SET)
    }

    //设置View的paddingTop
    fun setPaddingTop(view: View, top: Int) {
        setPadding(view, NOT_SET, top, NOT_SET, NOT_SET)
    }

    //设置View的paddingRight
    fun setPaddingRight(view: View, right: Int) {
        setPadding(view, NOT_SET, NOT_SET, right, NOT_SET)
    }

    //设置View的paddingBottom
    fun setPaddingBottom(view: View, bottom: Int) {
        setPadding(view, NOT_SET, NOT_SET, NOT_SET, bottom)
    }

    //设置View的Padding
    fun setPadding(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        view.setPadding(
            if (left != NOT_SET) left else view.paddingLeft,
            if (top != NOT_SET) top else view.paddingTop,
            if (right != NOT_SET) right else view.paddingRight,
            if (bottom != NOT_SET) bottom else view.paddingBottom
        )
    }

    //增加View的PaddingLeft
    fun addPaddingLeft(view: View, left: Int) {
        addPadding(view, left, 0, 0, 0)
    }

    //增加View的PaddingTop
    fun addPaddingTop(view: View, top: Int) {
        addPadding(view, 0, top, 0, 0)
    }

    //增加View的PaddingRight
    fun addPaddingRight(view: View, right: Int) {
        addPadding(view, 0, 0, right, 0)
    }

    //增加View的PaddingBottom
    fun addPaddingBottom(view: View, bottom: Int) {
        addPadding(view, 0, 0, 0, bottom)
    }

    //增加View的Padding，传负数表示减少View的Padding
    fun addPadding(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        view.setPadding(
            view.paddingLeft + left, view.paddingTop + top,
            view.paddingRight + right, view.paddingBottom + bottom
        )
    }

}