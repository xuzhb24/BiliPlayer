package com.android.widget.picGetterDialog

import android.graphics.Bitmap

/**
 * Created by xuzhb on 2020/1/27
 * Desc:
 */
interface OnPicGetterListener {

    /**
     * 图片获取成功
     *
     * @param bitmap  图片
     * @param picPath 图片路径
     */
    fun onSuccess(bitmap: Bitmap?, picPath: String?)

    /**
     * 图片获取失败
     *
     * @param errorMsg 错误信息
     */
    fun onFailure(errorMsg: String?)


    /**
     * 取消图片获取
     */
    fun onCancel()

}