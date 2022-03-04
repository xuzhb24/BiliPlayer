package com.android.project.adapter

import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.util.*
import com.android.util.DateUtil
import com.android.util.formatCountStr
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2022/2/15
 * Desc:视频详情-相关推荐
 */
class VideoReleatedAdapter : BaseQuickAdapter<ItemBean, BaseViewHolder>(R.layout.item_video_releated) {

    override fun convert(holder: BaseViewHolder, item: ItemBean) {
        showImageByCenterCrop(holder.getView(R.id.cover_iv), getVideoCover(item), 3.5f)
        holder.setText(R.id.title_tv, getVideoTitle(item))
            .setText(R.id.up_tv, getVideoAuthor(item)?.name ?: getFirstTag(item)?.name ?: "")
            .setText(R.id.share_tv, formatCountStr(getVideoShareCount(item)))
            .setText(R.id.comment_tv, formatCountStr(getVideoReplyCount(item)))
            .setText(R.id.duration_tv, DateUtil.getDuration(getVideoDuration(item)))
    }

}