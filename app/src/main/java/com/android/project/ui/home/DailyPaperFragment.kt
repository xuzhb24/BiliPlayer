package com.android.project.ui.home

import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.util.FilterUtil

/**
 * Created by xuzhb on 2021/12/30
 * Desc:日报
 */
class DailyPaperFragment : CommonListVideoFragment() {

    companion object {
        fun newInstance() = DailyPaperFragment()
    }

    override fun getFirstPageUrl() = UrlConstant.FEED

    override fun convertData(response: MutableList<ItemBean>?): MutableList<ItemBean>? {
        return FilterUtil.filterVideoList(response)
    }

}