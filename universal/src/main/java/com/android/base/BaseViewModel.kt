package com.android.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.base.liveDataEntity.DialogConfig
import com.android.base.liveDataEntity.ErrorResponse
import com.android.http.ExceptionUtil
import com.android.util.LogUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created by xuzhb on 2021/8/5
 * Desc:基类ViewModel
 */
open class BaseViewModel : ViewModel() {

    var showLoadLayoutData = MutableLiveData<Boolean>()          //是否显示加载状态布局，前提是当前布局包含加载控件，如果不包含，此参数无效
    var showLoadingDialogData = MutableLiveData<DialogConfig>()  //是否显示加载弹窗
    var loadFinishErrorData = MutableLiveData<Boolean>()         //是否加载失败

    //通过coroutineScope构建器或者由其他协程（如async）启动的协程抛出的异常，不会被try/catch捕获
    //CoroutineExceptionHandler就是用来处理这类未捕获的异常，保证APP不会崩溃
    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        LogUtil.i("exceptionHandler", throwable.message ?: "")
        throwable.printStackTrace()
    }

    //接口请求，加入加载状态布局、加载弹窗、网络错误的处理
    fun <T> launch(
        block: suspend CoroutineScope.() -> T,  //请求接口方法，T表示data实体泛型
        successData: MutableLiveData<T>,        //接口返回的成功结果
        errorData: MutableLiveData<ErrorResponse<T>>,  //接口返回的异常结果
        showLoadLayout: Boolean = true,      //是否显示加载状态布局
        showLoadingDialog: Boolean = false,  //是否显示加载弹窗
        message: String = "加载中",          //加载弹窗的提示文本
        cancelable: Boolean = true,          //加载弹窗是否可以点击取消
        needLoadFinish: Boolean = true       //是否需要集成加载完成的逻辑，配置这个参数是为了解决当一个页面存在多个接口请求时，只让主接口控制加载完成的逻辑
    ) {
        //请求之前
        if (showLoadLayout) {
            showLoadLayoutData.value = true  //显示加载状态布局
        }
        if (showLoadingDialog) {
            showLoadingDialogData.value = DialogConfig(message, cancelable)  //显示加载弹窗
        }
        viewModelScope.launch(exceptionHandler) {
            kotlin.runCatching {
                val t = block()  //开始请求
                successData.value = t
                //请求完成
                if (needLoadFinish) {
                    loadFinishErrorData.value = false
                }
            }.onFailure {
                LogUtil.e("BaseViewModel", it)
                //请求完成
                if (needLoadFinish) {
                    loadFinishErrorData.value = true
                }
                //请求异常
                val msg = ExceptionUtil.convertExceptopn(it)
                errorData.value = ErrorResponse(true, msg, it, null)  //封装失败结果
            }
        }
    }

}