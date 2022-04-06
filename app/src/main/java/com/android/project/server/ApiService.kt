package com.android.project.server

import com.android.http.model.BaseListResponse
import com.android.project.entity.ItemBean
import com.android.project.entity.ReplyBean
import com.android.project.entity.TabBean
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Created by xuzhb on 2020/11/2
 * Desc:所有的接口地址在这里定义
 */
interface ApiService {

    //直接访问IP地址
    @GET
    suspend fun accessUrl(@Url url: String): BaseListResponse<ItemBean>

    //获取评论
    @GET
    suspend fun accessReply(@Url url: String): BaseListResponse<ReplyBean>

    //发现更多
    @GET("api/v7/index/tab/discovery")
    suspend fun discovery(): BaseListResponse<ItemBean>

    //每日推荐
    @GET("api/v5/index/tab/allRec")
    suspend fun allRec(): BaseListResponse<ItemBean>

    //日报精选
    @GET("api/v5/index/tab/feed")
    suspend fun feed(): BaseListResponse<ItemBean>

    //相关推荐
    @GET("api/v4/video/related")
    suspend fun related(@Query("id") id: String): BaseListResponse<ItemBean>

    //评论
    @GET("api/v2/replies/video")
    suspend fun replies(@Query("videoId") videoId: String): BaseListResponse<ReplyBean>

    //主题
    @GET("api/v7/tag/tabList")
    suspend fun tabList(): TabBean

}