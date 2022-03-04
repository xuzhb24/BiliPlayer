package com.android.project.ui.home

import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.util.FilterUtil

/**
 * Created by xuzhb on 2021/12/30
 * Desc:月榜
 */
class MonthRankFragment : CommonListVideoFragment() {

    companion object {
        fun newInstance() = MonthRankFragment()
    }

    override fun getFirstPageUrl() = UrlConstant.MONTHLY

    override fun convertData(response: MutableList<ItemBean>?): MutableList<ItemBean>? {
        return FilterUtil.filterVideoList(response)
    }

}