package com.android.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import com.android.universal.R

/**
 * Created by xuzhb on 2020/7/18
 * Desc:加载状态布局
 */
class LoadingLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val STATE_LOADING = 0  //加载中
        private const val STATE_EMPTY = 1    //无数据
        private const val STATE_FAIL = 2     //加载失败
        private const val STATE_HIDE = 3     //隐藏
    }

    var loadingSrc: Drawable? = null  //加载中的图片
        set(value) {
            field = value
            mLoadingIv.setImageDrawable(value)
        }
    var loadingDescText: String = ""  //加载中的文本描述
        set(value) {
            field = value
            setDescText(mDescTv, value)
        }
    var emptySrc: Drawable? = null  //无数据的图片
        set(value) {
            field = value
            mEmptyIv.setImageDrawable(value)
        }
    var emptyDescText: String = ""  //无数据的文本描述
        set(value) {
            field = value
            setDescText(mDescTv, value)
        }
    var emptyActionText: String = ""  //无数据时操作按钮的文本
        set(value) {
            field = value
            setActionText(mActionTv, value)
        }
    var failSrc: Drawable? = null  //加载失败的图片
        set(value) {
            field = value
            mFailIv.setImageDrawable(value)
        }
    var failDescText: String = ""  //加载失败的文本描述
        set(value) {
            field = value
            setDescText(mDescTv, value)
        }
    var failActionText: String = ""  //重试的文本描述
        set(value) {
            field = value
            setActionText(mActionTv, value)
        }
    private var intercept: Boolean = true  //是否拦截点击事件，为true时底下的View无法收到点击事件

    //获取根布局，以便设置布局的LayoutParams
    fun getRootLayout(): LinearLayout = mRootLayout

    private val mRootLayout: LinearLayout
    private val mLoadingIv: ImageView
    private val mEmptyIv: ImageView
    private val mFailIv: ImageView
    private val mDescTv: TextView
    private val mActionTv: TextView

    init {
        val layout = LayoutInflater.from(context).inflate(R.layout.layout_loading, this)
        with(layout) {
            mRootLayout = findViewById(R.id.root_ll)
            mLoadingIv = findViewById(R.id.loading_iv)
            mEmptyIv = findViewById(R.id.empty_iv)
            mFailIv = findViewById(R.id.fail_iv)
            mDescTv = findViewById(R.id.desc_tv)
            mActionTv = findViewById(R.id.action_tv)
        }
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.LoadingLayout)
            loadingSrc = ta.getDrawable(R.styleable.LoadingLayout_loadingSrc) ?: resources.getDrawable(R.drawable.ic_load_loading)
            loadingDescText = ta.getString(R.styleable.LoadingLayout_loadingDescText) ?: ""
            emptySrc = ta.getDrawable(R.styleable.LoadingLayout_emptySrc) ?: resources.getDrawable(R.drawable.ic_load_empty)
            emptyDescText = ta.getString(R.styleable.LoadingLayout_emptyDescText) ?: ""
            emptyActionText = ta.getString(R.styleable.LoadingLayout_emptyActionText) ?: ""
            failSrc = ta.getDrawable(R.styleable.LoadingLayout_failSrc) ?: resources.getDrawable(R.drawable.ic_load_fail)
            failDescText = ta.getString(R.styleable.LoadingLayout_failDescText) ?: ""
            failActionText = ta.getString(R.styleable.LoadingLayout_failActionText) ?: ""
            intercept = ta.getBoolean(R.styleable.LoadingLayout_intercept, true)
            ta.recycle()
        }
        setLoadState(STATE_HIDE)
    }

    //开始加载
    fun loadStart() {
        setLoadState(STATE_LOADING)
    }

    //无数据
    fun loadEmpty() {
        setLoadState(STATE_EMPTY)
    }

    //加载失败
    fun loadFail() {
        setLoadState(STATE_FAIL)
    }

    //加载完成，则隐藏
    fun loadComplete() {
        setLoadState(STATE_HIDE)
    }

    private fun setLoadState(loadState: Int) {
        if (loadState == STATE_HIDE) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            mLoadingIv.clearAnimation()  //取消动画
            mLoadingIv.visibility = View.GONE
            mEmptyIv.visibility = View.GONE
            mFailIv.visibility = View.GONE
            mActionTv.visibility = View.GONE
            when (loadState) {
                STATE_LOADING -> {
                    mLoadingIv.visibility = View.VISIBLE
                    mLoadingIv.setImageDrawable(loadingSrc)
                    val animation = RotateAnimation(
                        0f, 359f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                    )
                    mLoadingIv.startAnimation(animation.apply {
                        duration = 800
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.RESTART
                        interpolator = AccelerateDecelerateInterpolator()
                    })
                    setDescText(mDescTv, loadingDescText)
                }
                STATE_EMPTY -> {
                    mEmptyIv.visibility = View.VISIBLE
                    if (emptySrc == null) {
                        mEmptyIv.visibility = View.GONE
                    } else {
                        mEmptyIv.visibility = View.VISIBLE
                        mEmptyIv.setImageDrawable(emptySrc)
                    }
                    setDescText(mDescTv, emptyDescText)
                    setActionText(mActionTv, emptyActionText)
                    mActionTv.setOnClickListener { mOnEmptyListener?.invoke() }
                }
                STATE_FAIL -> {
                    mFailIv.visibility = View.VISIBLE
                    if (failSrc == null) {
                        mFailIv.visibility = View.GONE
                    } else {
                        mFailIv.visibility = View.VISIBLE
                        mFailIv.setImageDrawable(failSrc)
                    }
                    setDescText(mDescTv, failDescText)
                    setActionText(mActionTv, failActionText)
                    mActionTv.setOnClickListener { mOnFailListener?.invoke() }
                }
            }
        }
    }

    private fun setDescText(tv: TextView, desc: String) {
        if (TextUtils.isEmpty(desc)) {
            tv.visibility = View.GONE
        } else {
            tv.visibility = View.VISIBLE
            tv.text = desc
        }
    }

    private fun setActionText(tv: TextView, text: String) {
        if (TextUtils.isEmpty(text)) {
            tv.visibility = View.GONE
        } else {
            tv.visibility = View.VISIBLE
            tv.text = text
        }
    }

    private var mOnEmptyListener: (() -> Unit)? = null

    //无数据时相应的操作
    fun setOnEmptyListener(listener: () -> Unit) {
        mOnEmptyListener = listener
    }

    private var mOnFailListener: (() -> Unit)? = null

    //加载失败时相应的操作，如点击重试
    fun setOnFailListener(listener: () -> Unit) {
        mOnFailListener = listener
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return intercept  //拦截点击事件
    }

}