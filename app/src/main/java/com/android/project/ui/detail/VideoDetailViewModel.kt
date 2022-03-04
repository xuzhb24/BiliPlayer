package com.android.project.ui.detail

import com.android.base.BaseViewModelWithData
import com.android.http.model.BaseListResponse
import com.android.project.entity.ItemBean
import com.android.project.server.ApiHelper

/**
 * Created by xuzhb on 2022/2/18
 * Desc:
 */
class VideoDetailViewModel : BaseViewModelWithData<BaseListResponse<ItemBean>>() {

    //获取视频相关推荐
    fun releatedVideo(id: String) {
        launchMain({ ApiHelper.related(id) })
    }

}