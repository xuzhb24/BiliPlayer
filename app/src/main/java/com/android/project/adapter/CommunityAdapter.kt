package com.android.project.adapter

import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.util.*
import com.android.util.DateUtil
import com.android.util.LogUtil
import com.android.util.formatCountStr
import com.android.widget.recyclerView.LoadMoreAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2022/3/16
 * Desc:首页-社区
 */
class CommunityAdapter : LoadMoreAdapter<ItemBean>(R.layout.item_community) {
    override fun convert(holder: BaseViewHolder, item: ItemBean) {
        val coverIv: ImageView = holder.getView(R.id.cover_iv)
        val rootCl: ConstraintLayout = holder.getView(R.id.root_cl)
        val width = item.data.content?.data?.width ?: 0
        val height = item.data.content?.data?.height ?: 0
        LogUtil.i("width x height", "$width x $height")
        if (width < height) {  //竖屏
            ConstraintSet().apply {
                clone(rootCl)
                setDimensionRatio(R.id.cover_iv, "h,$width:$height")
                applyTo(rootCl)
            }
        } else {
            ConstraintSet().apply {
                clone(rootCl)
                setDimensionRatio(R.id.cover_iv, "h,32:20")
                applyTo(rootCl)
            }
        }
        showImageByCenterCrop(coverIv, getVideoCover(item), 3.5f, 3.5f)
        holder.setText(R.id.share_tv, formatCountStr(getVideoShareCount(item)))
            .setText(R.id.comment_tv, formatCountStr(getVideoReplyCount(item)))
            .setText(R.id.duration_tv, DateUtil.getDuration(getVideoDuration(item)))
            .setText(R.id.title_tv, getVideoDescription(item))
            .setText(R.id.up_tv, item.data.content?.data?.owner?.nickname ?: "")
    }
}