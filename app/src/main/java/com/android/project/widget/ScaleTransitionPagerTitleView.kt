package com.android.project.widget

import android.content.Context
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView

/**
 * Created by xuzhb on 2020/8/3
 * Desc:带颜色渐变和缩放的指示器标题
 */
class ScaleTransitionPagerTitleView @JvmOverloads constructor(
    context: Context, private val minScale: Float = 0.9f
) : ColorTransitionPagerTitleView(context) {

    override fun onEnter(index: Int, totalCount: Int, enterPercent: Float, leftToRight: Boolean) {
        super.onEnter(index, totalCount, enterPercent, leftToRight)    // 实现颜色渐变
        scaleX = minScale + (1.0f - minScale) * enterPercent
        scaleY = minScale + (1.0f - minScale) * enterPercent
//        pivotY = bottom.toFloat() * minScale  //底部对齐
    }

    override fun onLeave(index: Int, totalCount: Int, leavePercent: Float, leftToRight: Boolean) {
        super.onLeave(index, totalCount, leavePercent, leftToRight)    // 实现颜色渐变
        scaleX = 1.0f + (minScale - 1.0f) * leavePercent
        scaleY = 1.0f + (minScale - 1.0f) * leavePercent
//        pivotY = bottom.toFloat() * minScale  //底部对齐
    }

    override fun onSelected(index: Int, totalCount: Int) {
        super.onSelected(index, totalCount)
        paint.isFakeBoldText = true
    }

    override fun onDeselected(index: Int, totalCount: Int) {
        super.onDeselected(index, totalCount)
        paint.isFakeBoldText = false
    }

}