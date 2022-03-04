package com.android.project.ui.home

import android.graphics.Color
import android.os.Bundle
import com.android.base.CommonBaseFragment
import com.android.project.databinding.FragmentHotBinding

/**
 * Created by xuzhb on 2021/12/30
 * Desc:热门
 */
class HotFragment : CommonBaseFragment<FragmentHotBinding>() {

    companion object {
        fun newInstance() = HotFragment()
    }

    override fun handleView(savedInstanceState: Bundle?) {
        mLoadingLayout?.setBackgroundColor(Color.parseColor("#F3F3F5"))
    }

    override fun initListener() {
    }

}