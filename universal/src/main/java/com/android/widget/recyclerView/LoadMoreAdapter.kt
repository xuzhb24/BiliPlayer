package com.android.widget.recyclerView

import androidx.annotation.LayoutRes
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2020/7/28
 * Desc:上拉加载更多
 */
abstract class LoadMoreAdapter<T> @JvmOverloads constructor(
    @LayoutRes private val layoutResId: Int,
    data: MutableList<T>? = null
) : BaseQuickAdapter<T, BaseViewHolder>(layoutResId, data), LoadMoreModule