package com.android.base

import androidx.lifecycle.MutableLiveData
import com.android.base.liveDataEntity.ErrorResponse
import kotlinx.coroutines.CoroutineScope

/**
 * Created by xuzhb on 2021/8/7
 * Desc:基类ViewModel的进一步封装，携带数据结构，包含了主接口的请求方法，
 * 可以通过successData处理请求成功的情况，通过errorData处理请求失败的情况
 */
abstract class BaseViewModelWithData<T> : BaseViewModel() {

    var successData = MutableLiveData<T>()
    var errorData = MutableLiveData<ErrorResponse<T>>()

    //请求主接口
    fun launchMain(
        block: suspend CoroutineScope.() -> T,  //请求接口方法，T表示data实体泛型
        showLoadLayout: Boolean = true,         //是否显示加载状态布局
        showLoadingDialog: Boolean = false,     //是否显示加载弹窗
        message: String = "加载中",             //加载弹窗的提示文本
        cancelable: Boolean = true              //加载弹窗是否可以点击取消
    ) {
        launch(block, successData, errorData, showLoadLayout, showLoadingDialog, message, cancelable, true)
    }

}