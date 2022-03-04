package com.android.project.ui.main

import android.os.Bundle
import com.android.base.CommonBaseFragment
import com.android.project.databinding.FragmentTrendsBinding

/**
 * Created by xuzhb on 2021/8/13
 * Desc:动态
 */
class TrendsFragment : CommonBaseFragment<FragmentTrendsBinding>() {

    companion object {
        fun newInstance() = TrendsFragment()
    }

    override fun handleView(savedInstanceState: Bundle?) {
    }

    override fun initListener() {
    }

}