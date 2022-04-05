package com.android.project.server

/**
 * Created by xuzhb on 2022/1/19
 * Desc:
 */
object UrlConstant {

    //每日推荐
    const val ALLREC = "http://baobab.kaiyanapp.com/api/v5/index/tab/allRec"

    //日报精选
    const val FEED = "http://baobab.kaiyanapp.com/api/v5/index/tab/feed"

    //周榜
    const val WEAKLY = "http://baobab.kaiyanapp.com/api/v4/rankList/videos?strategy=weekly"

    //月榜
    const val MONTHLY = "http://baobab.kaiyanapp.com/api/v4/rankList/videos?strategy=monthly"

    //总榜
    const val HISTORICAL = "http://baobab.kaiyanapp.com/api/v4/rankList/videos?strategy=historical"

    //社区
    const val REC = "http://baobab.kaiyanapp.com/api/v7/community/tab/rec"

    //关注
    const val FOLLOW = "http://baobab.kaiyanapp.com/api/v4/tabs/follow"

    //相关推荐
    const val RELATED = "http://baobab.kaiyanapp.com/api/v4/video/related?id="

    //评论
    const val REPLIES = "http://baobab.kaiyanapp.com/api/v2/replies/video?videoId="

}