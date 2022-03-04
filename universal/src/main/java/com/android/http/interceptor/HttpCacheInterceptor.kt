package com.android.http.interceptor

import com.android.util.LogUtil
import com.android.util.NetworkUtil
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Created by xuzhb on 2020/8/9
 * Desc:网络缓存拦截器，只支持GET请求
 * 参考：https://www.jianshu.com/p/7aa8f3443e05
 * https://www.jianshu.com/p/dbda0bb8d541
 * https://www.jianshu.com/p/ea2055db3dd3
 */
class HttpCacheInterceptor(
    private val maxAge: Int = 0,  //缓存过期时间，单位是秒，默认不使用缓存
    private val maxStale: Int = 3 * 24 * 60 * 60  //缓存过期时间，在请求头设置有效，在响应头设置无效，默认缓存3天
) : Interceptor {

    companion object {
        private const val TAG = "HttpCacheInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val isConnected = NetworkUtil.isConnected()
        LogUtil.w(TAG, "网络是否连接：${isConnected}")
        if (!isConnected) {  //无网络的情况
            LogUtil.w(TAG, "从缓存获取数据，max-stale=$maxStale")
            val control = CacheControl.Builder()
                .onlyIfCached()
                .maxStale(maxStale, TimeUnit.SECONDS)
                .build()
            request = request.newBuilder()
                .cacheControl(control)  //从缓存读取
                .build()
        }
        var response = chain.proceed(request)
        if (isConnected) {  //有网络的情况
            LogUtil.w(TAG, "max-age=$maxAge")
            response = response.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", "public, max-age=" + maxAge)  //如果想要不缓存，maxAge直接设置为0
                .build()
        }
        return response
    }
}