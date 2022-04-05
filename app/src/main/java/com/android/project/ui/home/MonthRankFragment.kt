package com.android.project.ui.home

import com.android.project.adapter.ListVideoAdapter
import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.ui.common.CommonListPlayFragment
import com.android.project.ui.common.CommonListViewModel
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.util.FilterUtil

/**
 * Created by xuzhb on 2021/12/30
 * Desc:月榜
 */
class MonthRankFragment : CommonListPlayFragment<CommonListViewModel>() {

    companion object {
        fun newInstance() = MonthRankFragment()
    }

    override fun getAdapter() = ListVideoAdapter()

    override fun getFirstPageUrl() = UrlConstant.MONTHLY

    override fun initListener() {
        super.initListener()
        mAdapter.setOnItemClickListener { adapter, view, position ->
            VideoDetailActivity.start(mContext, mAdapter.getItem(position))
        }
    }

    override fun convertData(response: MutableList<ItemBean>?): MutableList<ItemBean>? {
        return FilterUtil.filterVideoList(response)
    }

}