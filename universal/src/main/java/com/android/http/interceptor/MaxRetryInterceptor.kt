package com.android.http.interceptor

import com.android.util.LogUtil
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by xuzhb on 2020/8/9
 * Desc:限制最多重试次数的拦截器
 */
class MaxRetryInterceptor(private val maxRetryCount: Int) : Interceptor {

    companion object {
        private const val TAG = "MaxRetryInterceptor"
    }

    private var retryCount = 1

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        LogUtil.w(TAG, "retry count:$retryCount")
        var response = chain.proceed(request)  //默认请求1次
        while (!response.isSuccessful && retryCount < maxRetryCount) {
            retryCount++
            LogUtil.w(TAG, "retry count:$retryCount")
            response = chain.proceed(request)  //重试
        }
        return response
    }
}