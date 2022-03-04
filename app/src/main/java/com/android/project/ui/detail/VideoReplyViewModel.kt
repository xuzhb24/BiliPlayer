package com.android.project.ui.detail

import com.android.base.BaseListViewModel
import com.android.http.model.BaseListResponse
import com.android.project.entity.ReplyBean
import com.android.project.server.ApiHelper
import kotlinx.coroutines.CoroutineScope

/**
 * Created by xuzhb on 2022/2/15
 * Desc:
 */
class VideoReplyViewModel : BaseListViewModel<ReplyBean>() {

    override fun loadDataFromServer(nextPageUrl: String): (suspend CoroutineScope.() -> BaseListResponse<ReplyBean>)? {
        return { ApiHelper.accessReply(nextPageUrl) }
    }

}