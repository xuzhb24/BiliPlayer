package com.android.base

import androidx.lifecycle.MutableLiveData
import com.android.base.liveDataEntity.ErrorResponse
import com.android.http.model.BaseListResponse
import kotlinx.coroutines.CoroutineScope

/**
 * Created by xuzhb on 2021/8/7
 * Desc:列表数据对应的基类ViewModel
 */
open class BaseListViewModel<T> : BaseViewModel() {

    var successData = MutableLiveData<BaseListResponse<T>>()
    var errorData = MutableLiveData<ErrorResponse<BaseListResponse<T>>>()

    //分页加载
    open fun loadData(
        nextPageUrl: String,
        showLoadLayout: Boolean = true,      //是否显示加载状态布局
        showLoadingDialog: Boolean = false
    ) {
        loadDataFromServer(nextPageUrl)?.let {
            launch(it, successData, errorData, showLoadLayout, showLoadingDialog)
        }
    }

    //从服务器按页加载数据
    open fun loadDataFromServer(nextPageUrl: String): (suspend CoroutineScope.() -> BaseListResponse<T>)? = null

}