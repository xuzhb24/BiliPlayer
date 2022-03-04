package com.android.video.widget

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import com.android.video.R
import com.android.video.databinding.ViewLoadingAnimBinding

/**
 * Created by xuzhb on 2022/1/24
 * Desc:加载动画
 */
class LoadingAnimView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var mDrawable: AnimationDrawable
    private var binding: ViewLoadingAnimBinding = ViewLoadingAnimBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.loadingIv.background = ContextCompat.getDrawable(context, R.drawable.animation_progress_loading)
        mDrawable = binding.loadingIv.background as AnimationDrawable
        binding.loadingTv.text = "正在缓冲..."
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mDrawable.start()
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mDrawable.stop()
    }

}