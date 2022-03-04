package com.android.project.ui.home

import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.util.FilterUtil

/**
 * Created by xuzhb on 2021/12/30
 * Desc:总榜
 */
class TotalRankFragment : CommonListVideoFragment() {

    companion object {
        fun newInstance() = TotalRankFragment()
    }

    override fun getFirstPageUrl() = UrlConstant.HISTORICAL

    override fun convertData(response: MutableList<ItemBean>?): MutableList<ItemBean>? {
        return FilterUtil.filterVideoList(response)
    }

}