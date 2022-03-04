package com.android.widget.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.annotation.FloatRange
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.android.universal.R
import com.android.widget.ViewHolder

/**
 * Created by xuzhb on 2019/10/22
 * Desc:基类的Dialog
 */
abstract class BaseDialog : DialogFragment() {

    @LayoutRes
    protected var mLayoutId: Int = -1             //对话框的布局id
    private var mWidth: Int = 0                 //对话框的宽度
    private var mHeight: Int = 0                //对话框的高度
    private var mMargin: Int = 0                //对话框左右边距
    private var mDimAmount: Float = 0.5f        //背景透明度
    private var mAnimationStyle: Int = -1       //对话框出现消失的动画
    private var mCancelable: Boolean = true     //是否可点击取消
    private var mGravity: Int = Gravity.CENTER  //对话框显示的位置，默认正中间

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
        mLayoutId = getLayoutId()  //设置dialog布局
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(mLayoutId, container, false)
        convertView(ViewHolder(view), dialog)  //获取dialog布局的控件
        return view
    }

    override fun onStart() {
        super.onStart()
        initParams()
    }

    //初始化参数
    private fun initParams() {
        dialog?.window?.let {
            val params = it.attributes
            params.dimAmount = mDimAmount
            params.gravity = mGravity
            //设置dialog宽度
            if (mWidth == 0) {
                params.width = context!!.resources.displayMetrics.widthPixels - 2 * mMargin
            } else {
                params.width = mWidth
            }
            //设置dialog高度
            if (mHeight == 0) {
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
            } else {
                params.height = mHeight
            }
            //设置dialog动画
            if (mAnimationStyle != -1) {
                it.setWindowAnimations(mAnimationStyle)
            }
            it.attributes = params
        }
        isCancelable = mCancelable
    }

    //设置宽高
    fun setViewParams(width: Int, height: Int): BaseDialog {
        mWidth = width
        mHeight = height
        return this
    }

    //设置左右的边距
    fun setHorizontalMargin(margin: Int): BaseDialog {
        mMargin = margin;
        return this
    }

    //设置背景昏暗度
    fun setDimAmount(@FloatRange(from = 0.0, to = 1.0) dimAmount: Float): BaseDialog {
        mDimAmount = dimAmount
        return this
    }

    //设置动画
    fun setAnimationStyle(@StyleRes animationStyle: Int): BaseDialog {
        mAnimationStyle = animationStyle
        return this
    }

    //设置Outside是否可点击
    fun setOutsideCancelable(cancelable: Boolean): BaseDialog {
        mCancelable = cancelable
        return this
    }

    //在中间显示
    fun show(manager: FragmentManager) {
        super.show(manager, BaseDialog::class.java.name)
    }

    //在底部显示
    fun showAtBottom(manager: FragmentManager) {
        mGravity = Gravity.BOTTOM
        super.show(manager, BaseDialog::class.java.name)
    }

    //获取dialog的布局Id
    abstract fun getLayoutId(): Int

    //处理dialog布局上的控件
    abstract fun convertView(holder: ViewHolder, dialog: Dialog?)

}