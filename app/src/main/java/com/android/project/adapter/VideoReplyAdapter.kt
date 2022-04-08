package com.android.project.adapter

import com.android.project.R
import com.android.project.entity.ReplyBean
import com.android.project.util.showCircleImageByCenterCrop
import com.android.util.DateUtil
import com.android.util.formatCountStr
import com.android.widget.recyclerView.LoadMoreAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2022/2/15
 * Desc:视频详情页-评论
 */
class VideoReplyAdapter : LoadMoreAdapter<ReplyBean>(R.layout.item_video_reply) {

    override fun convert(holder: BaseViewHolder, item: ReplyBean) {
        val createTime = item.data?.createTime ?: 0
        val createTimeStr =
            if (DateUtil.long2String(System.currentTimeMillis(), "yyyy") == DateUtil.long2String(createTime, "yyyy")) {
                DateUtil.long2String(createTime, DateUtil.M_D) ?: ""  //今年内
            } else {
                DateUtil.long2String(createTime, DateUtil.Y_M_D) ?: ""  //往年
            }
        showCircleImageByCenterCrop(holder.getView(R.id.head_iv), item.data?.user?.avatar ?: "")
        holder.setText(R.id.name_tv, item.data?.user?.nickname ?: "")
            .setText(R.id.time_tv, createTimeStr)
            .setText(R.id.message_tv, item.data?.message ?: "")
            .setText(R.id.praise_tv, formatCountStr(item.data?.likeCount ?: 0, ""))
    }

}