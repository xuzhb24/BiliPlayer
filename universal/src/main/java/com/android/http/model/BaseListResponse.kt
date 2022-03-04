package com.android.http.model

import java.io.Serializable

/**
 * Created by xuzhb on 2019/8/8
 * Desc:bean基类，列表类型
 */
class BaseListResponse<T>(
    val adExist: Boolean,
    val count: Int,
    val itemList: MutableList<T>?,
    val nextPageUrl: String?,
    val total: Int
) : Serializable {
    fun isSuccess(): Boolean = !itemList.isNullOrEmpty()
    fun getMessage(): String = ""
}