package com.android.project.ui.main

import android.os.Bundle
import com.android.base.CommonBaseFragment
import com.android.project.databinding.FragmentHomeBinding

/**
 * Created by xuzhb on 2021/8/13
 * Desc:首页
 */
class HomeFragment : CommonBaseFragment<FragmentHomeBinding>() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun handleView(savedInstanceState: Bundle?) {
    }

    override fun initListener() {
    }

}