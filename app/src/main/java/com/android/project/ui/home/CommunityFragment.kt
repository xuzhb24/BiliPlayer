package com.android.project.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.base.BaseListFragment
import com.android.project.adapter.CommunityAdapter
import com.android.project.entity.ItemBean
import com.android.project.ui.detail.SwitchDetailActivity
import com.android.project.util.getVideoDescription
import com.android.project.util.getVideoPlayUrl
import com.android.universal.databinding.LayoutListBinding
import com.android.util.LayoutParamsUtil
import com.android.util.SizeUtil

/**
 * Created by xuzhb on 2022/3/16
 * Desc:社区
 */
class CommunityFragment : BaseListFragment<ItemBean, LayoutListBinding, CommonListViewModel>() {

    companion object {
        fun newInstance() = CommunityFragment()
    }

    override fun getAdapter() = CommunityAdapter()

    override fun getFirstPageUrl(): String = "http://baobab.kaiyanapp.com/api/v7/community/tab/rec"

    override fun initAdapter() {
        mRecyclerView?.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        super.initAdapter()
    }

    override fun handleView(savedInstanceState: Bundle?) {
        binding.rootFl.setBackgroundColor(Color.parseColor("#F3F3F5"))
        mLoadingLayout?.setBackgroundColor(Color.parseColor("#F3F3F5"))
        LayoutParamsUtil.setPadding(
            mRecyclerView!!,
            SizeUtil.dp2pxInt(3.5f), 0,
            SizeUtil.dp2pxInt(3.5f), 0
        )
        mAdapter.setHeaderView(View(mContext).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtil.dp2pxInt(3.5f))
        })
    }

    override fun initListener() {
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val item = mAdapter.getItem(position)
            SwitchDetailActivity.start(mContext, item, mAdapter.data)
        }
    }

    override fun convertData(response: MutableList<ItemBean>?): MutableList<ItemBean>? {
        val list: MutableList<ItemBean> = mutableListOf()
        response?.forEach {
            if (getVideoPlayUrl(it).isNotBlank() && getVideoDescription(it).isNotBlank()) {
                list.add(it)
            }
        }
        return list
    }

}