package com.android.widget

import android.util.SparseArray
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes

/**
 * Created by xuzhb on 2019/10/22
 * Desc:
 */
@Suppress("UNCHECKED_CAST")
class ViewHolder(private var mView: View) {

    //缓存View
    private var mViewList: SparseArray<View>

    init {
        mViewList = SparseArray()
    }

    //查找View中的控件
    fun <T : View> getView(@IdRes viewId: Int): T? {
        //对已有的view做缓存
        var view: View? = mViewList.get(viewId)
        //使用缓存的方式减少findViewById的次数
        if (view == null) {
            view = mView.findViewById(viewId)
            mViewList.put(viewId, view)
        }
        return view as? T
    }

    //设置文本
    fun setText(@IdRes viewId: Int, text: CharSequence?): ViewHolder {
        val view = getView<TextView>(viewId)
        view?.text = text ?: ""
        return this //链式调用
    }

    //设置文本字体颜色
    fun setTextColor(@IdRes viewId: Int, color: Int): ViewHolder {
        val view = getView<TextView>(viewId)
        view?.setTextColor(color)
        return this
    }

    //设置文本字体大小，单位默认为SP，故设置时只需要传递数值就可以，如setTextSize(R.id.xxx,15f)
    fun setTextSize(@IdRes viewId: Int, textSize: Float): ViewHolder {
        val view = getView<TextView>(viewId)
        view?.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        return this
    }

    //设置图片
    fun setImageResource(@IdRes viewId: Int, resId: Int): ViewHolder {
        val iv = getView<ImageView>(viewId)
        iv?.setImageResource(resId)
        return this
    }

    //显示View
    fun setViewVisible(@IdRes viewId: Int): ViewHolder {
        getView<View>(viewId)?.visibility = View.VISIBLE
        return this
    }

    //隐藏View
    fun setViewGone(@IdRes viewId: Int): ViewHolder {
        getView<View>(viewId)?.visibility = View.GONE
        return this
    }

    //设置View宽度
    fun setViewWidth(@IdRes viewId: Int, width: Int): ViewHolder {
        return setViewParams(viewId, width, -1)
    }

    //设置View高度
    fun setViewHeight(@IdRes viewId: Int, height: Int): ViewHolder {
        return setViewParams(viewId, -1, height)
    }

    //设置View的宽度和高度
    fun setViewParams(@IdRes viewId: Int, width: Int, height: Int): ViewHolder {
        getView<View>(viewId)?.let {
            val params = it.layoutParams as ViewGroup.MarginLayoutParams
            if (width >= 0) {
                params.width = width
            }
            if (height >= 0) {
                params.height = height
            }
            it.layoutParams = params
        }
        return this
    }

    //设置点击事件
    fun setOnClickListener(@IdRes viewId: Int, listener: (v: View) -> Unit): ViewHolder {
        getView<View>(viewId)?.setOnClickListener { v -> listener.invoke(v) }
        return this
    }

    //设置长按事件
    fun setOnLongClickListener(@IdRes viewId: Int, listener: (v: View) -> Boolean): ViewHolder {
        getView<View>(viewId)?.setOnLongClickListener { v -> listener.invoke(v) }
        return this
    }

}