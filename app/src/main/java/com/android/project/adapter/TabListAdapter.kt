package com.android.project.adapter

import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.util.showImageByCenterCrop
import com.android.widget.recyclerView.LoadMoreAdapter
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2022/4/6
 * Desc:发现-主题
 */
class TabListAdapter : LoadMoreAdapter<ItemBean>(R.layout.item_tab_list) {
    override fun convert(holder: BaseViewHolder, item: ItemBean) {
        showImageByCenterCrop(holder.getView(R.id.head_iv), item.data.icon ?: "", CircleCrop())
        holder.setText(R.id.title_tv, item.data.title ?: "")
            .setText(R.id.desc_tv, if (!item.data.description.isNullOrBlank()) item.data.description else "暂无简介")
    }
}