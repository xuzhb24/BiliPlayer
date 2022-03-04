package com.android.project.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.project.R
import com.chad.library.adapter.base.loadmore.BaseLoadMoreView
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2022/2/18
 * Desc:视频详情评论页的脚布局
 */
class LoadMoreViewForReply : BaseLoadMoreView() {

    override fun getLoadComplete(holder: BaseViewHolder): View = holder.getView(R.id.complete_fl)

    override fun getLoadEndView(holder: BaseViewHolder): View = holder.getView(R.id.end_fl)

    override fun getLoadFailView(holder: BaseViewHolder): View = holder.getView(R.id.fail_fl)

    override fun getLoadingView(holder: BaseViewHolder): View = holder.getView(R.id.loading_ll)

    override fun getRootView(parent: ViewGroup): View =
        LayoutInflater.from(parent.context).inflate(R.layout.view_load_more_for_reply, parent, false);

}