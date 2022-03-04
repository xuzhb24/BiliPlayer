package com.android.base

/**
 * Created by xuzhb on 2021/8/7
 */
interface IBaseListView<T> : IBaseView {

    /**
     * 是否是首次加载
     */
    fun isFirstLoad(): Boolean

    /**
     * 显示数据
     */
    fun showData(nextPageUrl: String, list: MutableList<T>?)

}