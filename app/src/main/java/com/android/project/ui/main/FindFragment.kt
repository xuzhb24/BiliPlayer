package com.android.project.ui.main

import android.os.Bundle
import com.android.base.CommonBaseFragment
import com.android.project.databinding.FragmentFindBinding

/**
 * Created by xuzhb on 2021/12/30
 * Desc:发现
 */
class FindFragment : CommonBaseFragment<FragmentFindBinding>() {

    companion object {
        fun newInstance() = FindFragment()
    }

    override fun handleView(savedInstanceState: Bundle?) {
    }

    override fun initListener() {
    }
}