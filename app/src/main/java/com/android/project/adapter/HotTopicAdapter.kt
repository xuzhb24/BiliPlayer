package com.android.project.adapter

import com.android.project.R
import com.android.project.entity.ItemBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2022/4/5
 * Desc:动态-综合-热门话题
 */
class HotTopicAdapter : BaseQuickAdapter<ItemBean, BaseViewHolder>(R.layout.item_hot_topic) {
    override fun convert(holder: BaseViewHolder, item: ItemBean) {
        holder.setText(R.id.topic_tv, item.data.description ?: "")
    }
}