package com.android.project.util

import com.android.project.entity.ItemBean
import com.android.project.entity.ReplyBean

/**
 * Created by xuzhb on 2022/2/16
 * Desc:过滤工具
 */
object FilterUtil {

    fun filterVideoList(response: MutableList<ItemBean>?): MutableList<ItemBean> {
        val list: MutableList<ItemBean> = mutableListOf()
        response?.forEach {
            if (isVideo(it)) {
                list.add(it)
            }
        }
        return list
    }

    //判断是否是视频
    fun isVideo(itemBean: ItemBean): Boolean =
        getVideoPlayUrl(itemBean).isNotBlank() && getVideoTitle(itemBean).isNotBlank()

    fun filterReplyList(response: MutableList<ReplyBean>?): MutableList<ReplyBean> {
        val list: MutableList<ReplyBean> = mutableListOf()
        response?.forEach {
            if (isReply(it)) {
                list.add(it)
            }
        }
        return list
    }

    //是否是评论
    fun isReply(replyBean: ReplyBean): Boolean = replyBean.type == "reply" && replyBean.data?.parentReplyId == 0L

}