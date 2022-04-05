package com.android.project.ui.trends

import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.project.R
import com.android.project.adapter.CommonTrendsAdapter
import com.android.project.adapter.FanjuAdapter
import com.android.project.adapter.MeetUpAdapter
import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.ui.common.CommonListPlayFragment
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.util.Constant
import com.android.project.util.getVideoDescription
import com.android.project.util.getVideoPlayUrl
import kotlinx.android.synthetic.main.item_video_trends_header.view.*

/**
 * Created by xuzhb on 2022/3/29
 * Desc:视频
 */
class VideoTrendsFragment : CommonListPlayFragment<AllTrendsViewModel>() {

    companion object {
        fun newInstance() = VideoTrendsFragment()
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
            VideoDetailActivity.start(mContext, mAdapter.getItem(position))
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
//                Constant.TYPE_UGC_PICTURE -> {
//                    it.itemType = Constant.ITEM_TYPE_PHOTO
//                    list.add(it)
//                }
            }
        }
        return list
    }

    private fun initHeader(list: MutableList<ItemBean>?) {
        val headerView = LayoutInflater.from(mContext).inflate(R.layout.item_video_trends_header, null)
        if (!list.isNullOrEmpty()) {
            val upList: MutableList<ItemBean> = mutableListOf()
            val fanjuList: MutableList<ItemBean.ItemX> = mutableListOf()
            list.forEach {
                if (it.type == Constant.TYPE_VIDEO_COLLECTION_WITH_BRIEF) {
                    upList.add(it)
                }
                if (it.type == Constant.TYPE_COLUMN_CARD_LIST) {
                    it.data.itemList?.let { fanjuList.addAll(it) }
                }
            }
            headerView.meet_rv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            headerView.meet_rv.adapter = MeetUpAdapter().apply {
                setNewInstance(upList)
            }
            headerView.fanju_rv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            headerView.fanju_rv.adapter = FanjuAdapter().apply {
                setNewInstance(fanjuList)
            }
            mAdapter.setHeaderView(headerView)
        } else {
            mAdapter.removeAllHeaderView()
        }
    }

}