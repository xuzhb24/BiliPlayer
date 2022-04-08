package com.android.project.adapter

import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.util.showCircleImageByCenterCrop
import com.android.util.LayoutParamsUtil
import com.android.util.SizeUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2022/4/5
 * Desc:动态-综合/视频-最常访问
 */
class MeetUpAdapter : BaseQuickAdapter<ItemBean, BaseViewHolder>(R.layout.item_meet_up) {
    override fun convert(holder: BaseViewHolder, item: ItemBean) {
        val header = item.data.header
        showCircleImageByCenterCrop(holder.getView(R.id.head_iv), header?.icon ?: "")
        holder.setText(R.id.name_tv, header?.title ?: "")
        LayoutParamsUtil.setMarginLeft(
            holder.getView(R.id.content_ll),
            if (holder.adapterPosition == 0) SizeUtil.dp2px(13f).toInt()
            else SizeUtil.dp2px(10f).toInt()
        )
        LayoutParamsUtil.setMarginRight(
            holder.getView(R.id.content_ll),
            if (holder.adapterPosition == itemCount - 1) SizeUtil.dp2px(13f).toInt()
            else SizeUtil.dp2px(10f).toInt()
        )
    }
}