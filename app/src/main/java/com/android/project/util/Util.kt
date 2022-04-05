package com.android.project.util

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.util.LogUtil
import com.android.util.SizeUtil
import com.android.util.glide.GlideUtil
import com.android.util.glide.SectionRoundedCorners
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

/**
 * Created by xuzhb on 2022/1/7
 * Desc:工具类扩展，存放单个方法
 */
//显示图片，图片缩放格式为CenterCrop
fun showImageByCenterCrop(
    iv: ImageView,
    url: String,
    radius: Float = 5f,
    @DrawableRes placeResId: Int = R.drawable.ic_place_holder
) {
    showImageByCenterCrop(iv, url, RoundedCorners(SizeUtil.dp2pxInt(radius)), placeResId, placeResId)
}

//显示图片，图片缩放格式为CenterCrop
fun showImageByCenterCrop(
    iv: ImageView,
    url: String,
    topLeftRadius: Float = 0f,
    topRightRadius: Float = 0f,
    bottomLeftRadius: Float = 0f,
    bottomRightRadius: Float = 0f,
    @DrawableRes placeResId: Int = R.drawable.ic_place_holder
) {
    showImageByCenterCrop(
        iv, url,
        SectionRoundedCorners(
            SizeUtil.dp2px(topLeftRadius), SizeUtil.dp2px(topRightRadius),
            SizeUtil.dp2px(bottomLeftRadius), SizeUtil.dp2px(bottomRightRadius)
        ),
        placeResId, placeResId
    )
}

//显示图片，图片缩放格式为CenterCrop
fun showImageByCenterCrop(
    iv: ImageView,
    url: String,
    transformation: BitmapTransformation? = null,
    @DrawableRes placeResId: Int = R.drawable.ic_place_holder,
    @DrawableRes errorResId: Int = R.drawable.ic_place_holder
) {
    GlideUtil.showImageFromUrl(iv, url, transformation, placeResId, errorResId)
}

fun getData(itemBean: ItemBean?): ItemBean.Data? {
    return if (itemBean?.data?.content != null) {
        itemBean.data.content.data
    } else {
        itemBean?.data
    }
}

//获取视频id
fun getVideoId(itemBean: ItemBean?): Int {
    return if (itemBean?.data?.content != null) {
        val id = itemBean.data.content.data.id ?: 0
        LogUtil.i("video_id", "from content:$id")
        id
    } else {
        val id = itemBean?.data?.id ?: 0
        LogUtil.i("video_id", "from data:$id")
        id
    }
}

//获取视频封面
fun getVideoCover(itemBean: ItemBean?): String {
    return if (itemBean?.data?.content != null) {
        val cover = itemBean.data.content.data.cover?.detail ?: ""
        LogUtil.i("video_cover", "from content:$cover")
        cover
    } else {
        val cover = itemBean?.data?.cover?.detail ?: ""
        LogUtil.i("video_cover", "from data:$cover")
        cover
    }
}

//获取视频时长
fun getVideoDuration(itemBean: ItemBean?): Long {
    return if (itemBean?.data?.content != null) {
        val duration = itemBean.data.content.data.duration ?: 0
        LogUtil.i("video_duration", "from content:$duration")
        duration
    } else {
        val duration = itemBean?.data?.duration ?: 0
        LogUtil.i("video_duration", "from data:$duration")
        duration
    }
}

//获取视频发布时间
fun getVideoReleaseTime(itemBean: ItemBean?): Long {
    return if (itemBean?.data?.content != null) {
        val releaseTime = itemBean.data.content.data.releaseTime ?: 0
        LogUtil.i("video_releaseTime", "from content:$releaseTime")
        releaseTime
    } else {
        val releaseTime = itemBean?.data?.releaseTime ?: 0
        LogUtil.i("video_releaseTime", "from data:$releaseTime")
        releaseTime
    }
}

//获取视频播放地址
fun getVideoPlayUrl(itemBean: ItemBean?): String {
    return if (itemBean?.data?.content != null) {
        val playUrl = itemBean.data.content.data.playUrl ?: ""
        LogUtil.i("video_playUrl", "from content:$playUrl")
        playUrl
    } else {
        val playUrl = itemBean?.data?.playUrl ?: ""
        LogUtil.i("video_playUrl", "from data:$playUrl")
        playUrl
    }
}

//获取视频标题
fun getVideoTitle(itemBean: ItemBean?): String {
    return if (itemBean?.data?.content != null) {
        val title = itemBean.data.content.data.title ?: ""
        LogUtil.i("video_title", "from content:$title")
        title
    } else {
        val title = itemBean?.data?.title ?: ""
        LogUtil.i("video_title", "from data:$title")
        title
    }
}

//获取视频描述
fun getVideoDescription(itemBean: ItemBean?): String {
    return if (itemBean?.data?.content != null) {
        val description = itemBean.data.content.data.description ?: ""
        LogUtil.i("video_description", "from content:$description")
        description
    } else {
        val description = itemBean?.data?.description ?: ""
        LogUtil.i("video_description", "from data:$description")
        description
    }
}

//获取视频分享个数
fun getVideoShareCount(itemBean: ItemBean?): Int {
    return if (itemBean?.data?.content != null) {
        val shareCount = itemBean.data.content.data.consumption?.shareCount ?: 0
        LogUtil.i("video_shareCount", "from content:$shareCount")
        shareCount
    } else {
        val shareCount = itemBean?.data?.consumption?.shareCount ?: 0
        LogUtil.i("video_shareCount", "from data:$shareCount")
        shareCount
    }
}

//获取视频收藏个数
fun getVideoCollectionCount(itemBean: ItemBean?): Int {
    return if (itemBean?.data?.content != null) {
        val collectionCount = itemBean.data.content.data.consumption?.collectionCount ?: 0
        LogUtil.i("video_collectionCount", "from content:$collectionCount")
        collectionCount
    } else {
        val collectionCount = itemBean?.data?.consumption?.collectionCount ?: 0
        LogUtil.i("video_collectionCount", "from data:$collectionCount")
        collectionCount
    }
}

//获取视频收藏个数
fun getVideoRealCollectionCount(itemBean: ItemBean?): Int {
    return if (itemBean?.data?.content != null) {
        val realCollectionCount = itemBean.data.content.data.consumption?.realCollectionCount ?: 0
        LogUtil.i("video_realCollectionCount", "from content:$realCollectionCount")
        realCollectionCount
    } else {
        val realCollectionCount = itemBean?.data?.consumption?.realCollectionCount ?: 0
        LogUtil.i("video_realCollectionCount", "from data:$realCollectionCount")
        realCollectionCount
    }
}

//获取视频评论个数
fun getVideoReplyCount(itemBean: ItemBean?): Int {
    return if (itemBean?.data?.content != null) {
        val replyCount = itemBean.data.content.data.consumption?.replyCount ?: 0
        LogUtil.i("video_replyCount", "from content:$replyCount")
        replyCount
    } else {
        val replyCount = itemBean?.data?.consumption?.replyCount ?: 0
        LogUtil.i("video_replyCount", "from data:$replyCount")
        replyCount
    }
}

//获取视频分类
fun getVideoCategory(itemBean: ItemBean?): String {
    return if (itemBean?.data?.content != null) {
        val category = itemBean.data.content.data.category ?: ""
        LogUtil.i("video_category", "from content:$category")
        category
    } else {
        val category = itemBean?.data?.category ?: ""
        LogUtil.i("video_category", "from data:$category")
        category
    }
}

//获取视频作者
fun getVideoAuthor(itemBean: ItemBean?): ItemBean.Author? {
    return if (itemBean?.data?.content != null) {
        val author = itemBean.data.content.data.author
        LogUtil.i("video_author", "from content:${author?.id} ${author?.name}")
        author
    } else {
        val author = itemBean?.data?.author
        LogUtil.i("video_author", "from data:${author?.id} ${author?.name}")
        author
    }
}

//获取视频作者
fun getVideoOwner(itemBean: ItemBean?): ItemBean.Owner? {
    return if (itemBean?.data?.content != null) {
        val owner = itemBean.data.content.data.owner
        LogUtil.i("video_owner", "from content:${owner?.nickname} ${owner?.description}")
        owner
    } else {
        val owner = itemBean?.data?.owner
        LogUtil.i("video_owner", "from data:${owner?.nickname} ${owner?.description}")
        owner
    }
}

//获取关联的第一个标签
fun getFirstTag(itemBean: ItemBean?): ItemBean.Tag? {
    if (itemBean?.data?.content != null) {
        val tags = itemBean.data.content.data.tags
        LogUtil.i("video_tags", "from content:${tags?.size}")
        if (!tags.isNullOrEmpty()) {
            return tags[0]
        }
    } else {
        val tags = itemBean?.data?.tags
        LogUtil.i("video_tags", "from data:${tags?.size}")
        if (!tags.isNullOrEmpty()) {
            return tags[0]
        }
    }
    return null
}

