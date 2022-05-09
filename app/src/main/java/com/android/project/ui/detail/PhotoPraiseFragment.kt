package com.android.project.ui.detail

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.android.base.CommonBaseFragment
import com.android.project.R
import com.android.universal.databinding.LayoutListBinding
import com.android.util.LayoutParamsUtil
import com.android.util.SizeUtil

/**
 * Created by xuzhb on 2022/4/20
 * Desc:图片详情页-赞
 */
class PhotoPraiseFragment : CommonBaseFragment<LayoutListBinding>() {

    companion object {
        fun newInstance() = PhotoPraiseFragment()
    }

    override fun handleView(savedInstanceState: Bundle?) {
        mSwipeRefreshLayout?.isEnabled = false
        mLoadingLayout?.let {
            it.intercept = false
            it.loadEmpty()
            it.emptySrc = ContextCompat.getDrawable(mContext, R.drawable.ic_load_empty_photo)
            it.emptyDescText = "还没有人点赞\n给ta点第一个赞吧~"
            it.findViewById<TextView>(R.id.desc_tv).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            it.getRootLayout().gravity = Gravity.CENTER_HORIZONTAL
            LayoutParamsUtil.setPaddingTop(it.getRootLayout(), SizeUtil.dp2pxInt(30f))
        }
    }

    override fun initListener() {
    }

}