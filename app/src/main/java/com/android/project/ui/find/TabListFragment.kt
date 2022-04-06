package com.android.project.ui.find

import android.content.Context
import android.os.Bundle
import com.android.base.BaseListFragment
import com.android.project.adapter.TabListAdapter
import com.android.project.entity.ItemBean
import com.android.project.ui.common.CommonListViewModel
import com.android.universal.databinding.LayoutListBinding

/**
 * Created by xuzhb on 2022/4/6
 * Desc:发现-主题
 */
class TabListFragment : BaseListFragment<ItemBean, LayoutListBinding, CommonListViewModel>() {

    companion object {
        private const val EXTRA_API_URL = "EXTRA_API_URL"

        fun newInstance(apiUrl: String) = TabListFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_API_URL, apiUrl)
            }
        }
    }

    private var mApiUrl = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mApiUrl = arguments?.getString(EXTRA_API_URL) ?: ""
    }

    override fun getAdapter() = TabListAdapter()

    override fun getFirstPageUrl() = mApiUrl

    override fun handleView(savedInstanceState: Bundle?) {
    }

    override fun initListener() {
    }

}