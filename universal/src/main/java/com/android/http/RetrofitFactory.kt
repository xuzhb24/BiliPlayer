package com.android.http

import com.android.base.BaseApplication
import com.android.http.interceptor.HttpCacheInterceptor
import com.android.http.interceptor.HttpLogInterceptor
import com.android.util.FileUtil
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

/**
 * Created by xuzhb on 2019/8/8
 * Desc:Retrofit配置类：创建Service
 */
class RetrofitFactory {

    companion object {
        //单例对象
        val instance = SingleTonHolder.holder
    }

    //静态内部类单例模式
    private object SingleTonHolder {
        val holder = RetrofitFactory()
    }

    fun <T> createService(
        clazz: Class<T>,
        baseUrl: String,
        factory: Converter.Factory = GsonConverterFactory.create(),
        interceptor: Interceptor? = null,
        timeout: Long = 30L,    //默认超时时长
        cache: Boolean = false  //是否进行缓存
    ): T {
        val builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(factory)
        if (interceptor != null) {
            builder.client(getOkHttpBuilder(timeout, cache).addInterceptor(interceptor).build())
        } else {
            builder.client(getOkHttpBuilder(timeout, cache).build())
        }
        return builder.build().create(clazz)
    }

    /**
     * 设置OkHttpClient.Builder
     *
     * @param timeout 接口超时时长
     * @param cache   是否进行缓存
     */
    private fun getOkHttpBuilder(timeout: Long, cache: Boolean): OkHttpClient.Builder {
        //添加一个log拦截器,打印所有的log
//        val httpLoggingInterceptor = HttpLoggingInterceptor()
        val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLogInterceptor())
        //可以设置请求过滤的水平,body,basic,headers
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val builder = OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
//            .retryOnConnectionFailure(true)  //设置出现错误进行重新连接
            .cookieJar(CookieJarManage.instance)  //cookie持久化
        if (cache) {
            val cacheDir = File(BaseApplication.instance.cacheDir, "httpCache")  //设置缓存路径
            val cacheSize: Long = 10 * 1024 * 1024  //设置缓存大小为10M
            builder.cache(Cache(cacheDir, cacheSize))
                .addInterceptor(HttpCacheInterceptor())
        }
        if (Config.DEBUG) {
            builder
//                .sslSocketFactory(
//                    SSLSocketFactoryUtil.getPassAnySSLSocketFactory(),
//                    SSLSocketFactoryUtil.getTrustAllManager()
//                )  //不校验证书
                .hostnameVerifier(HostnameVerifier { p0, p1 -> true })  //不校验服务器返回的信息
                .addInterceptor(httpLoggingInterceptor) //打印日志，以便调试
        }
        return builder
    }

    //清除缓存
    fun clearCache(): Boolean {
        val cacheDir = File(BaseApplication.instance.cacheDir, "httpCache")
        return FileUtil.deleteDirectory(cacheDir)
    }

}