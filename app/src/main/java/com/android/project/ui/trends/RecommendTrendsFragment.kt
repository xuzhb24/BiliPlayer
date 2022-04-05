package com.android.project.ui.trends

import android.os.Bundle
import android.view.LayoutInflater
import com.android.base.BaseListFragment
import com.android.project.R
import com.android.project.adapter.RecommendTrendsAdapter
import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.ui.common.CommonListViewModel
import com.android.project.util.Constant
import com.android.universal.databinding.LayoutListBinding
import kotlinx.android.synthetic.main.item_show_title.view.*

/**
 * Created by xuzhb on 2022/3/29
 * Desc:推荐
 */
class RecommendTrendsFragment : BaseListFragment<ItemBean, LayoutListBinding, CommonListViewModel>() {

    companion object {
        fun newInstance() = RecommendTrendsFragment()
    }

    override fun getAdapter() = RecommendTrendsAdapter()

    override fun getFirstPageUrl() = UrlConstant.FOLLOW

    override fun handleView(savedInstanceState: Bundle?) {

    }

    override fun initListener() {
        disposeScroll()
        mAdapter.setOnItemClickListener { adapter, view, position ->

        }
    }

    override fun convertData(response: MutableList<ItemBean>?): MutableList<ItemBean>? {
        val list: MutableList<ItemBean> = mutableListOf()
        response?.forEach {
            if (it.type == Constant.TYPE_VIDEO_COLLECTION_WITH_BRIEF) {
                list.add(it)
            }
        }
        return list
    }

    override fun showData(nextPageUrl: String, list: MutableList<ItemBean>?) {
        if (isFirstLoad()) {
            val headerView = LayoutInflater.from(mContext).inflate(R.layout.item_show_title, null)
            headerView.title_tv.text = "猜你喜欢的UP主"
            headerView.title_tv.textSize = 16f
            mAdapter.setHeaderView(headerView)
        }
        super.showData(nextPageUrl, list)
    }

}