package com.android.project.ui.trends

import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.project.R
import com.android.project.adapter.CommonTrendsAdapter
import com.android.project.adapter.HotTopicAdapter
import com.android.project.adapter.MeetUpAdapter
import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.ui.common.CommonListPlayFragment
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.util.Constant
import com.android.project.util.getVideoDescription
import com.android.project.util.getVideoPlayUrl
import kotlinx.android.synthetic.main.item_all_trends_header.view.*

/**
 * Created by xuzhb on 2022/3/29
 * Desc:综合
 */
class AllTrendsFragment : CommonListPlayFragment<AllTrendsViewModel>() {

    companion object {
        fun newInstance() = AllTrendsFragment()
    }

    override fun getAdapter() = CommonTrendsAdapter()

    override fun getFirstPageUrl() = UrlConstant.REC

    override fun observerListDataChange() {
        viewModel.discoveryData.observe(this, {
            initHeader(it)
        })
        super.observerListDataChange()
    }

    override fun initListener() {
        super.initListener()
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val item = mAdapter.getItem(position)
            when (item.itemType) {
                Constant.ITEM_TYPE_VIDEO -> VideoDetailActivity.start(mContext, item)
                Constant.ITEM_TYPE_PHOTO -> showToast("待实现")
            }
        }
    }

    override fun convertData(response: MutableList<ItemBean>?): MutableList<ItemBean>? {
        val list: MutableList<ItemBean> = mutableListOf()
        response?.forEach {
            when (it.data.content?.type) {
                Constant.TYPE_VIDEO -> {
                    it.itemType = Constant.ITEM_TYPE_VIDEO
                    if (getVideoPlayUrl(it).isNotBlank() && getVideoDescription(it).isNotBlank()) {
                        list.add(it)
                    }
                }
                Constant.TYPE_UGC_PICTURE -> {
                    it.itemType = Constant.ITEM_TYPE_PHOTO
                    list.add(it)
                }
            }
        }
        return list
    }

    private fun initHeader(list: MutableList<ItemBean>?) {
        val headerView = LayoutInflater.from(mContext).inflate(R.layout.item_all_trends_header, null)
        if (!list.isNullOrEmpty()) {
            val upList: MutableList<ItemBean> = mutableListOf()
            val topicList: MutableList<ItemBean> = mutableListOf()
            list.forEach {
                if (it.type == Constant.TYPE_VIDEO_COLLECTION_WITH_BRIEF) {
                    upList.add(it)
                }
                if (it.data.dataType == Constant.TYPE_TAG_BRIEF_CARD) {
                    if (topicList.size < 9) {
                        topicList.add(it)
                    }
                }
            }
            headerView.meet_rv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            headerView.meet_rv.adapter = MeetUpAdapter().apply {
                setNewInstance(upList)
            }
            headerView.topic_rv.layoutManager = GridLayoutManager(mContext, 3, LinearLayoutManager.HORIZONTAL, false)
            headerView.topic_rv.adapter = HotTopicAdapter().apply {
                setNewInstance(topicList)
            }
            mAdapter.setHeaderView(headerView)
        } else {
            mAdapter.removeAllHeaderView()
        }
    }

}