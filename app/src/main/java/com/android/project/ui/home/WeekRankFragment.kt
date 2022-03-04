package com.android.project.ui.home

import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.util.FilterUtil

/**
 * Created by xuzhb on 2021/12/30
 * Desc:周榜
 */
class WeekRankFragment : CommonListVideoFragment() {

    companion object {
        fun newInstance() = WeekRankFragment()
    }

    override fun getFirstPageUrl() = UrlConstant.WEAKLY

    override fun convertData(response: MutableList<ItemBean>?): MutableList<ItemBean>? {
        return FilterUtil.filterVideoList(response)
    }

}