package com.android.widget.recyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.universal.R
import com.chad.library.adapter.base.loadmore.BaseLoadMoreView
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2020/7/29
 * Desc:自定义加载更多的脚布局
 */
class CustomLoadMoreView : BaseLoadMoreView() {

    override fun getLoadComplete(holder: BaseViewHolder): View = holder.getView(R.id.complete_fl)

    override fun getLoadEndView(holder: BaseViewHolder): View = holder.getView(R.id.end_fl)

    override fun getLoadFailView(holder: BaseViewHolder): View = holder.getView(R.id.fail_fl)

    override fun getLoadingView(holder: BaseViewHolder): View = holder.getView(R.id.loading_ll)

    override fun getRootView(parent: ViewGroup): View =
        LayoutInflater.from(parent.context).inflate(R.layout.view_load_more, parent, false);

}