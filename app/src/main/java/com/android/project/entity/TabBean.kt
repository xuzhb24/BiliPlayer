package com.android.project.entity

/**
 * Created by xuzhb on 2022/4/6
 * Desc:
 */
data class TabBean(
    val tabInfo: TabInfo
) {

    data class TabInfo(
        val defaultIdx: Int,
        val tabList: MutableList<Tab>
    )

    data class Tab(
        val adTrack: Any,
        val apiUrl: String,
        val id: Int,
        val name: String,
        val nameType: Int,
        val tabType: Int
    )
}
