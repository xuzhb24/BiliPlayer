package com.android.base.liveDataEntity

/**
 * Created by xuzhb on 2021/8/5
 * Desc:接口请求失败结果
 * 接口请求失败分为两种情况：一种是接口自己返回失败结果，此时isException为false，
 * 另一种是接口访问异常，如网络无连接导致无法访问，此时isException为true
 */
data class ErrorResponse<T>(
    val isException: Boolean,   //访问接口是否发生异常，true时exception不为空，false时response不为空
    val message: String,        //错误信息
    val exception: Throwable?,  //异常信息，此时isException为true
    val response: T?            //接口返回结果，此时isException为false
)