package com.android.project.ui.main

import com.android.base.BaseViewModelWithData
import com.android.project.entity.TabBean
import com.android.project.server.ApiHelper

/**
 * Created by xuzhb on 2022/4/6
 * Desc:
 */
class FindViewModel : BaseViewModelWithData<TabBean>() {

    fun tabList() {
        launchMain({ ApiHelper.tabList() })
    }

}