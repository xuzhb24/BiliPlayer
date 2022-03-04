package com.android.project.server

import com.android.http.Config
import com.android.http.RetrofitFactory
import com.android.http.model.BaseListResponse
import com.android.project.entity.ItemBean
import com.android.project.entity.ReplyBean
import okhttp3.Interceptor
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by xuzhb on 2020/11/2
 * Desc:接口参数封装
 */
object ApiHelper {

    //直接访问IP地址
    suspend fun accessUrl(url: String): BaseListResponse<ItemBean> {
        return createService(Config.BASE_URL).accessUrl(url)
    }

    //获取评论
    suspend fun accessReply(url: String): BaseListResponse<ReplyBean> {
        return createService(Config.BASE_URL).accessReply(url)
    }

    //发现更多
    suspend fun discovery(): BaseListResponse<ItemBean> {
        return createService(Config.BASE_URL).discovery()
    }

    //每日推荐
    suspend fun allRec(): BaseListResponse<ItemBean> {
        return createService(Config.BASE_URL).allRec()
    }

    //日报精选
    suspend fun feed(): BaseListResponse<ItemBean> {
        return createService(Config.BASE_URL).feed()
    }

    //相关推荐
    suspend fun related(id: String): BaseListResponse<ItemBean> {
        return createService(Config.BASE_URL).related(id)
    }

    //评论
    suspend fun replies(videoId: String): BaseListResponse<ReplyBean> {
        return createService(Config.BASE_URL).replies(videoId)
    }

    private fun createService(
        baseUrl: String,
        factory: Converter.Factory = GsonConverterFactory.create(),
        interceptor: Interceptor? = null,
        timeout: Long = 30L,    //默认超时时长
        cache: Boolean = false  //是否进行缓存
    ): ApiService {
        return RetrofitFactory.instance.createService(
            ApiService::class.java, baseUrl,
            factory, interceptor, timeout, cache
        )
    }

}