package com.android.project.util

import com.android.project.entity.ItemBean
import com.android.video.entity.VideoBean

/**
 * Created by xuzhb on 2022/2/22
 * Desc:
 */
object ConvertEntityUtil {

    fun item2VideoBean(itemBean: ItemBean?): VideoBean? {
        if (itemBean == null) {
            return null
        }
        if (getVideoPlayUrl(itemBean).isNullOrBlank()) {
            return null
        }
        return VideoBean(
            getVideoId(itemBean),
            getVideoTitle(itemBean),
            getVideoCover(itemBean),
            getVideoDuration(itemBean),
            getVideoPlayUrl(itemBean),
            getVideoCollectionCount(itemBean),
            getVideoReplyCount(itemBean),
            getVideoShareCount(itemBean),
            getVideoAuthor(itemBean)?.name ?: getFirstTag(itemBean)?.name ?: "",
            getVideoAuthor(itemBean)?.icon ?: getFirstTag(itemBean)?.headerImage ?: "",
            itemBean.data.content?.data?.width,
            itemBean.data.content?.data?.height,
        )
    }

    fun item2VideoBeanList(list: MutableList<ItemBean>?): MutableList<VideoBean> {
        val result: MutableList<VideoBean> = mutableListOf()
        list?.forEach {
            item2VideoBean(it)?.let {
                result.add(it)
            }
        }
        return result
    }

}