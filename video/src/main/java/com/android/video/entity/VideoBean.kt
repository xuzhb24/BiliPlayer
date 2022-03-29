package com.android.video.entity

/**
 * Created by xuzhb on 2022/2/22
 * Desc:
 */
data class VideoBean(
    val id: Int?,               //视频id
    val title: String?,         //视频标题
    val cover: String?,         //封面
    val duration: Long?,        //时长
    val playUrl: String?,       //播放地址
    val collectionCount: Int?,  //收藏个数
    val replyCount: Int?,       //评论个数
    val shareCount: Int?,       //分享个数
    val upName: String?,        //作者名称
    val upIcon: String?,        //作者头像
    val width: Int?,            //视频宽度
    val height: Int?            //视频高度
)