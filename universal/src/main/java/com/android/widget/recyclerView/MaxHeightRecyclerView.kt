package com.android.widget.recyclerView

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.RecyclerView
import com.android.universal.R

/**
 * Created by xuzhb on 2019/10/28
 * Desc:重写RecyclerView，支持maxHeight属性
 */
class MaxHeightRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var maxHeight: Int = 0
        set(value) {
            field = value
            if (maxHeight <= 0) {  //当值小于等于0时，maxHeight属性失效
                requestLayout()    //重新布局
            }
        }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightRecyclerView)
        maxHeight = ta.getLayoutDimension(R.styleable.MaxHeightRecyclerView_maxHeight, 0)
        ta.recycle()
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var heightSpec = heightSpec
        if (maxHeight > 0) {
            heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthSpec, heightSpec)
    }

}