package com.android.project.ui.main

import android.os.Bundle
import com.android.base.CommonBaseFragment
import com.android.project.databinding.FragmentMineBinding

/**
 * Created by xuzhb on 2021/8/13
 * Desc:我的
 */
class MineFragment : CommonBaseFragment<FragmentMineBinding>() {

    companion object {
        fun newInstance() = MineFragment()
    }

    override fun handleView(savedInstanceState: Bundle?) {
    }

    override fun initListener() {
    }

}