package com.android.project.ui.home

import androidx.lifecycle.MutableLiveData
import com.android.project.entity.ItemBean
import com.android.project.server.ApiHelper
import com.android.project.server.UrlConstant
import com.android.project.ui.common.CommonListViewModel
import com.android.project.util.Constant
import kotlinx.coroutines.async

/**
 * Created by xuzhb on 2022/1/18
 * Desc:
 */
class RecommendViewModel : CommonListViewModel() {

    var bannerData = MutableLiveData<MutableList<ItemBean.ItemX>>()

    override fun loadData(nextPageUrl: String, showLoadLayout: Boolean, showLoadingDialog: Boolean) {
        launch({
            if (nextPageUrl == UrlConstant.FEED) {  //首次加载
                val listDataAsync = async { ApiHelper.accessUrl(nextPageUrl) }  //异步请求加快响应速度
                val bannerList: MutableList<ItemBean.ItemX> = mutableListOf()
                ApiHelper.discovery().itemList?.let {
                    for (item in it) {
                        if (item.data.dataType == Constant.TYPE_HORIZONTALSCROLLCARD) {
                            for (itemx in item.data.itemList!!) {
                                if (itemx.type == Constant.TYPE_BANNER2) {
                                    bannerList.add(itemx)
                                }
                            }
                        }
                        break
                    }
                }
                bannerList.shuffle()
                val listData = listDataAsync.await()
                if (listData.isSuccess()) {
                    bannerData.value = if (bannerList.size > 2) bannerList.subList(0, 3) else bannerList
                }
                listData
            } else ApiHelper.accessUrl(nextPageUrl)
        }, successData, errorData)
    }

}