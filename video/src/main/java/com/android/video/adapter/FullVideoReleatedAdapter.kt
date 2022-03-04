package com.android.video.adapter

import com.android.util.DateUtil
import com.android.util.LayoutParamsUtil
import com.android.util.SizeUtil
import com.android.util.formatCountStr
import com.android.util.glide.GlideUtil
import com.android.video.R
import com.android.video.entity.VideoBean
import com.android.widget.recyclerView.LoadMoreAdapter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2022/2/22
 */
class FullVideoReleatedAdapter : LoadMoreAdapter<VideoBean>(R.layout.item_ful_videol_releated) {
    override fun convert(holder: BaseViewHolder, item: VideoBean) {
        GlideUtil.showImageFromUrl(
            holder.getView(R.id.cover_iv), item.cover ?: "",
            RoundedCorners(SizeUtil.dp2pxInt(3.5f)),
            R.drawable.ic_video_place_holder, R.drawable.ic_video_place_holder
        )
        holder.setText(R.id.share_tv, formatCountStr(item.shareCount))
            .setText(R.id.comment_tv, formatCountStr(item.replyCount))
            .setText(R.id.duration_tv, DateUtil.getDuration(item.duration ?: 0))
            .setText(R.id.title_tv, item.title)
        LayoutParamsUtil.setMarginLeft(
            holder.getView(R.id.content_cl),
            if (holder.adapterPosition == 0) SizeUtil.dp2px(25f).toInt()
            else SizeUtil.dp2px(8f).toInt()
        )
        LayoutParamsUtil.setMarginRight(
            holder.getView(R.id.content_cl),
            if (holder.adapterPosition == itemCount - 1) SizeUtil.dp2px(25f).toInt()
            else SizeUtil.dp2px(8f).toInt()
        )
    }
}