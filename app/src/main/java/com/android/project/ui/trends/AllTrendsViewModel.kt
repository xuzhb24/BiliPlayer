package com.android.project.ui.trends

import androidx.lifecycle.MutableLiveData
import com.android.project.entity.ItemBean
import com.android.project.server.ApiHelper
import com.android.project.server.UrlConstant
import com.android.project.ui.common.CommonListViewModel
import kotlinx.coroutines.async

/**
 * Created by xuzhb on 2022/4/1
 * Desc:
 */
class AllTrendsViewModel : CommonListViewModel() {

    var discoveryData = MutableLiveData<MutableList<ItemBean>>()

    override fun loadData(nextPageUrl: String, showLoadLayout: Boolean, showLoadingDialog: Boolean) {
        launch({
            if (nextPageUrl == UrlConstant.REC) {  //首次加载
                val listDataAsync = async { ApiHelper.accessUrl(nextPageUrl) }  //异步请求加快响应速度
                val discovery = ApiHelper.discovery()
                val follow1 = ApiHelper.accessUrl(UrlConstant.FOLLOW)
                val nextPageUrl = follow1.nextPageUrl
                val follow2 = if (nextPageUrl.isNullOrBlank()) null else ApiHelper.accessUrl(nextPageUrl)
                val discoveryList: MutableList<ItemBean> = mutableListOf()
                discoveryList.addAll(follow1.itemList ?: mutableListOf())
                discoveryList.addAll(follow2?.itemList ?: mutableListOf())
                discoveryList.addAll(discovery.itemList ?: mutableListOf())
                val listData = listDataAsync.await()
                if (listData.isSuccess()) {
                    discoveryData.value = discoveryList
                }
                listData
            } else ApiHelper.accessUrl(nextPageUrl)
        }, successData, errorData)
    }

}