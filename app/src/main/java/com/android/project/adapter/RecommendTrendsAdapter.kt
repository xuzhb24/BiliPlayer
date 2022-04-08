package com.android.project.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.util.showCircleImageByCenterCrop
import com.android.project.util.showImageByCenterCrop
import com.android.util.DateUtil
import com.android.widget.recyclerView.LoadMoreAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2022/3/30
 * Desc:动态-推荐
 */
class RecommendTrendsAdapter : LoadMoreAdapter<ItemBean>(R.layout.item_recommend_trends) {
    override fun convert(holder: BaseViewHolder, item: ItemBean) {
        val header = item.data.header
        showCircleImageByCenterCrop(holder.getView(R.id.head_iv), header?.icon ?: "")
        holder.setText(R.id.name_tv, header?.title ?: "")
            .setText(R.id.desc_tv, if (header?.description.isNullOrBlank()) "暂时没有简介！" else header?.description)
        val videoRv: RecyclerView = holder.getView(R.id.video_rv)
        videoRv.layoutManager = GridLayoutManager(context, 3)
        val result: MutableList<ItemBean.ItemX> = mutableListOf()
        item.data.itemList?.let {
            if (it.size > 3) {
                result.addAll(it.subList(0, 3))
            } else {
                result.addAll(it)
            }
        }
        videoRv.adapter = GridVideoAdapter().apply {
            setNewInstance(result)
            setOnItemClickListener { adapter, view, position ->
                val itemBean = ItemBean(
                    getItem(position).adIndex, getItem(position).data, getItem(position).id,
                    getItem(position).tag, getItem(position).trackingData, getItem(position).type
                )
                VideoDetailActivity.start(context, itemBean)
            }
        }
    }

    private inner class GridVideoAdapter : BaseQuickAdapter<ItemBean.ItemX, BaseViewHolder>(R.layout.item_grid_video) {
        override fun convert(holder: BaseViewHolder, item: ItemBean.ItemX) {
            showImageByCenterCrop(holder.getView(R.id.cover_iv), item.data.cover?.detail ?: "", 2f)
            holder.setText(R.id.duration_tv, DateUtil.getDuration(item.data.duration ?: 0))
                .setText(R.id.title_tv, item.data.title ?: "")
        }
    }
}