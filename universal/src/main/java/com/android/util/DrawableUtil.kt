package com.android.util

import android.graphics.drawable.GradientDrawable

/**
 * Created by xuzhb on 2019/10/20
 * Desc:代码创建Drawable
 */
object DrawableUtil {

    fun createSolidShape(corner: Float, color: Int): GradientDrawable {
        val shape = GradientDrawable()
        return shape.apply {
            cornerRadius = corner  //等价于<corners android:radius="" />
            setColor(color)  //等价于<solid android:color="" />
        }
    }

    fun createStrokeShape(
        corner: Float,
        strokeWidth: Int,
        strokeColor: Int,
        dashWidth: Float = 0f,
        dashGap: Float = 0f
    ): GradientDrawable {
        val shape = GradientDrawable()
        return shape.apply {
            cornerRadius = corner
            setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
        }
    }

    fun createSolidStrokeShape(
        corner: Float,
        color: Int,
        strokeWidth: Int,
        strokeColor: Int,
        dashWidth: Float = 0f,
        dashGap: Float = 0f
    ): GradientDrawable {
        val shape = GradientDrawable()
        return shape.apply {
            cornerRadius = corner
            setColor(color)
            setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
        }
    }

}