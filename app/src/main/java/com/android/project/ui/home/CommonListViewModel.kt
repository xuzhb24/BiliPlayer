package com.android.project.ui.home

import com.android.base.BaseListViewModel
import com.android.project.entity.ItemBean
import com.android.project.server.ApiHelper

/**
 * Created by xuzhb on 2022/1/20
 * Desc:通用的列表类型ViewModel，只需访问一个列表接口
 */
class CommonListViewModel : BaseListViewModel<ItemBean>() {

    override fun loadData(nextPageUrl: String, showLoadLayout: Boolean, showLoadingDialog: Boolean) {
        launch({ ApiHelper.accessUrl(nextPageUrl) }, successData, errorData)
    }

}