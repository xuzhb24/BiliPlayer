package com.android.project.adapter

import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.util.showImageByCenterCrop
import com.android.util.LayoutParamsUtil
import com.android.util.SizeUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2022/4/5
 * Desc:动态-视频-我的追番·追剧
 */
class FanjuAdapter : BaseQuickAdapter<ItemBean.ItemX, BaseViewHolder>(R.layout.item_fanju) {
    override fun convert(holder: BaseViewHolder, item: ItemBean.ItemX) {
        showImageByCenterCrop(holder.getView(R.id.content_iv), item.data.image ?: "")
        holder.setText(R.id.desc_tv, item.data.description ?: "")
            .setText(R.id.title_tv, item.data.title ?: "")
        LayoutParamsUtil.setMarginLeft(
            holder.getView(R.id.content_cl),
            if (holder.adapterPosition == 0) SizeUtil.dp2px(13f).toInt()
            else SizeUtil.dp2px(3.5f).toInt()
        )
        LayoutParamsUtil.setMarginRight(
            holder.getView(R.id.content_cl),
            if (holder.adapterPosition == itemCount - 1) SizeUtil.dp2px(13f).toInt()
            else SizeUtil.dp2px(3.5f).toInt()
        )
    }
}