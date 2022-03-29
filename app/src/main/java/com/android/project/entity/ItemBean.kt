package com.android.project.entity

import com.android.project.util.Constant
import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * Created by xuzhb on 2022/1/7
 * Desc:
 */
/*
{
	"type": "followCard",
	"data": {
		"dataType": "FollowCard",
		"header": {
			"id": 298259,
			"title": "Alex Drone",
			"font": null,
			"subTitle": null,
			"subTitleFont": null,
			"textAlign": "left",
			"cover": null,
			"label": null,
			"actionUrl": "eyepetizer://pgc/detail/591/?title=Alex%20Drone&userType=PGC&tabIndex=1",
			"labelList": null,
			"rightText": null,
			"icon": "http://img.kaiyanapp.com/8649ccd71659b6e2b41bb65f9e70a66c.jpeg?imageMogr2/quality/60/format/jpg",
			"iconType": "round",
			"description": "#旅行 / 收录于 每日编辑精选",
			"time": 1639114919000,
			"showHateVideo": false
		},
		"content": {
			"type": "video",
			"data": {
				"dataType": "VideoBeanForClient",
				"id": 298259,
				"title": "与鹰飞行，俯瞰最好的达吉斯坦",
				"description": "达吉斯坦共和国是一个风景如画的地方，人们就像几百年前一样，住在宏伟的峡谷和高山上，传承他们的传统，与顺从的羊群一起欣赏自然，而永恒的天空主宰——鹰在山顶盘旋。从最好的角度享受一次难忘的达吉斯坦之旅。",
				"library": "DAILY",
				"tags": [{
					"id": 10,
					"name": "跟着开眼看世界",
					"actionUrl": "eyepetizer://tag/10/?title=%E8%B7%9F%E7%9D%80%E5%BC%80%E7%9C%BC%E7%9C%8B%E4%B8%96%E7%95%8C",
					"adTrack": null,
					"desc": "去你想去的地方，发现世界的美",
					"bgPicture": "http://img.kaiyanapp.com/7ea328a893aa1f092b9328a53494a267.png?imageMogr2/quality/60/format/jpg",
					"headerImage": "http://img.kaiyanapp.com/50dab5468ecd2dbe5eb99dab5d608a0a.jpeg?imageMogr2/quality/60/format/jpg",
					"tagRecType": "IMPORTANT",
					"childTagList": null,
					"childTagIdList": null,
					"haveReward": false,
					"ifNewest": false,
					"newestEndTime": null,
					"communityIndex": 14
				}, {
					"id": 1227,
					"name": "俄罗斯",
					"actionUrl": "eyepetizer://tag/1227/?title=%E4%BF%84%E7%BD%97%E6%96%AF",
					"adTrack": null,
					"desc": "这是来自俄罗斯的广告。",
					"bgPicture": "http://img.kaiyanapp.com/95c891855f1c9913acf1d41935cc8f5c.jpeg?imageMogr2/quality/60/format/jpg",
					"headerImage": "http://img.kaiyanapp.com/95c891855f1c9913acf1d41935cc8f5c.jpeg?imageMogr2/quality/60/format/jpg",
					"tagRecType": "NORMAL",
					"childTagList": null,
					"childTagIdList": null,
					"haveReward": false,
					"ifNewest": false,
					"newestEndTime": null,
					"communityIndex": 0
				}, {
					"id": 528,
					"name": "震撼",
					"actionUrl": "eyepetizer://tag/528/?title=%E9%9C%87%E6%92%BC",
					"adTrack": null,
					"desc": null,
					"bgPicture": "http://img.kaiyanapp.com/c043f72eeaf7cf30d5b3bb410fc127e3.png?imageMogr2/quality/60/format/jpg",
					"headerImage": "http://img.kaiyanapp.com/c043f72eeaf7cf30d5b3bb410fc127e3.png?imageMogr2/quality/60/format/jpg",
					"tagRecType": "NORMAL",
					"childTagList": null,
					"childTagIdList": null,
					"haveReward": false,
					"ifNewest": false,
					"newestEndTime": null,
					"communityIndex": 0
				}, {
					"id": 142,
					"name": "浪漫",
					"actionUrl": "eyepetizer://tag/142/?title=%E6%B5%AA%E6%BC%AB",
					"adTrack": null,
					"desc": null,
					"bgPicture": "http://img.kaiyanapp.com/460ed0b9e2912a5a0f64e4ede5e8159c.jpeg?imageMogr2/quality/100",
					"headerImage": "http://img.kaiyanapp.com/775357709cbbb780e2544f28e7a5f2b2.jpeg?imageMogr2/quality/100",
					"tagRecType": "NORMAL",
					"childTagList": null,
					"childTagIdList": null,
					"haveReward": false,
					"ifNewest": false,
					"newestEndTime": null,
					"communityIndex": 0
				}, {
					"id": 1019,
					"name": "旅行",
					"actionUrl": "eyepetizer://tag/1019/?title=%E6%97%85%E8%A1%8C",
					"adTrack": null,
					"desc": "世界这么大，总有你的目的地",
					"bgPicture": "http://img.kaiyanapp.com/67b5aa7b489b33e7894e04d293e9b01f.jpeg?imageMogr2/quality/60/format/jpg",
					"headerImage": "http://img.kaiyanapp.com/67b5aa7b489b33e7894e04d293e9b01f.jpeg?imageMogr2/quality/60/format/jpg",
					"tagRecType": "NORMAL",
					"childTagList": null,
					"childTagIdList": null,
					"haveReward": false,
					"ifNewest": false,
					"newestEndTime": null,
					"communityIndex": 0
				}, {
					"id": 90,
					"name": "航拍",
					"actionUrl": "eyepetizer://tag/90/?title=%E8%88%AA%E6%8B%8D",
					"adTrack": null,
					"desc": null,
					"bgPicture": "http://img.kaiyanapp.com/1ce841d0a9aa7e0ae48e5675a077e8b0.jpeg?imageMogr2/quality/60",
					"headerImage": "http://img.kaiyanapp.com/355eaae8f2436b01202f0cf289b25624.jpeg?imageMogr2/quality/100",
					"tagRecType": "NORMAL",
					"childTagList": null,
					"childTagIdList": null,
					"haveReward": false,
					"ifNewest": false,
					"newestEndTime": null,
					"communityIndex": 0
				}, {
					"id": 52,
					"name": "风光大片",
					"actionUrl": "eyepetizer://tag/52/?title=%E9%A3%8E%E5%85%89%E5%A4%A7%E7%89%87",
					"adTrack": null,
					"desc": "",
					"bgPicture": "http://img.kaiyanapp.com/e484dd6aa22ea3c2e604812b44f8c60c.jpeg?imageMogr2/quality/60/format/jpg",
					"headerImage": "http://img.kaiyanapp.com/f333f225c9ccc78819120f3a888b2e7e.jpeg?imageMogr2/quality/60/format/jpg",
					"tagRecType": "NORMAL",
					"childTagList": null,
					"childTagIdList": null,
					"haveReward": false,
					"ifNewest": false,
					"newestEndTime": null,
					"communityIndex": 0
				}],
				"consumption": {
					"collectionCount": 94,
					"shareCount": 87,
					"replyCount": 1,
					"realCollectionCount": 67
				},
				"resourceType": "video",
				"slogan": null,
				"provider": {
					"name": "定制来源",
					"alias": "CustomSrc",
					"icon": ""
				},
				"category": "旅行",
				"author": {
					"id": 591,
					"icon": "http://img.kaiyanapp.com/8649ccd71659b6e2b41bb65f9e70a66c.jpeg?imageMogr2/quality/60/format/jpg",
					"name": "Alex Drone",
					"description": "个人摄影师，航拍视角带你领略莫斯科的美。",
					"link": "",
					"latestReleaseTime": 1640072241000,
					"videoNum": 23,
					"adTrack": null,
					"follow": {
						"itemType": "author",
						"itemId": 591,
						"followed": false
					},
					"shield": {
						"itemType": "author",
						"itemId": 591,
						"shielded": false
					},
					"approvedNotReadyVideoCount": 0,
					"ifPgc": true,
					"recSort": 0,
					"expert": false
				},
				"cover": {
					"feed": "http://img.kaiyanapp.com/6499b239a997e1ab2233638b3c817b8a.jpeg?imageMogr2/quality/60/format/jpg",
					"detail": "http://img.kaiyanapp.com/6499b239a997e1ab2233638b3c817b8a.jpeg?imageMogr2/quality/60/format/jpg",
					"blurred": "http://img.kaiyanapp.com/06913c7edb362dcb4e50318129a419a7.jpeg?imageMogr2/quality/60/format/jpg",
					"sharing": null,
					"homepage": null
				},
				"playUrl": "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=298259&resourceType=video&editionType=default&source=aliyun&playUrlType=url_oss&udid=",
				"thumbPlayUrl": null,
				"duration": 421,
				"webUrl": {
					"raw": "http://www.eyepetizer.net/detail.html?vid=298259",
					"forWeibo": "https://m.eyepetizer.net/u1/video-detail?video_id=298259&resource_type=video&utm_campaign=routine&utm_medium=share&utm_source=weibo&uid=0"
				},
				"releaseTime": 1639114919000,
				"playInfo": [{
					"height": 480,
					"width": 854,
					"urlList": [{
						"name": "aliyun",
						"url": "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=298259&resourceType=video&editionType=normal&source=aliyun&playUrlType=url_oss&udid=",
						"size": 65093858
					}, {
						"name": "ucloud",
						"url": "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=298259&resourceType=video&editionType=normal&source=ucloud&playUrlType=url_oss&udid=",
						"size": 65093858
					}],
					"name": "标清",
					"type": "normal",
					"url": "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=298259&resourceType=video&editionType=normal&source=aliyun&playUrlType=url_oss&udid="
				}, {
					"height": 720,
					"width": 1280,
					"urlList": [{
						"name": "aliyun",
						"url": "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=298259&resourceType=video&editionType=high&source=aliyun&playUrlType=url_oss&udid=",
						"size": 109250265
					}, {
						"name": "ucloud",
						"url": "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=298259&resourceType=video&editionType=high&source=ucloud&playUrlType=url_oss&udid=",
						"size": 109250265
					}],
					"name": "高清",
					"type": "high",
					"url": "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=298259&resourceType=video&editionType=high&source=aliyun&playUrlType=url_oss&udid="
				}],
				"campaign": null,
				"waterMarks": null,
				"ad": false,
				"adTrack": [],
				"type": "NORMAL",
				"titlePgc": "与鹰飞行，俯瞰最好的达吉斯坦",
				"descriptionPgc": "达吉斯坦共和国是一个风景如画的地方，人们就像几百年前一样，住在宏伟的峡谷和高山上，传承他们的传统，与顺从的羊群一起欣赏自然，而永恒的天空主宰——鹰在山顶盘旋。从最好的角度享受一次难忘的达吉斯坦之旅。",
				"remark": null,
				"ifLimitVideo": false,
				"searchWeight": 0,
				"brandWebsiteInfo": null,
				"videoPosterBean": null,
				"idx": 0,
				"shareAdTrack": null,
				"favoriteAdTrack": null,
				"webAdTrack": null,
				"date": 1641862800000,
				"promotion": null,
				"label": null,
				"labelList": [],
				"descriptionEditor": "达吉斯坦共和国是一个风景如画的地方，人们就像几百年前一样，住在宏伟的峡谷和高山上，传承他们的传统，与顺从的羊群一起欣赏自然，而永恒的天空主宰——鹰在山顶盘旋。从最好的角度享受一次难忘的达吉斯坦之旅。",
				"collected": false,
				"reallyCollected": false,
				"played": false,
				"subtitles": [],
				"lastViewTime": null,
				"playlists": null,
				"src": null,
				"recallSource": null,
				"recall_source": null
			},
			"trackingData": null,
			"tag": null,
			"id": 0,
			"adIndex": -1
		},
		"adTrack": []
	},
	"trackingData": null,
	"tag": null,
	"id": 0,
	"adIndex": -1
}
 */
data class ItemBean(
    val adIndex: Int,
    val `data`: Data,
    val id: Int,
    val tag: Any?,
    val trackingData: Any?,
    val type: String
) : MultiItemEntity {

    data class Data(
        val adTrack: List<Any>,
        val content: Content?,
        val dataType: String,
        val header: Header?,
        val itemList: MutableList<ItemX>?,
        val ad: Boolean?,
        val author: Author?,
        val brandWebsiteInfo: Any?,
        val campaign: Any?,
        val category: String?,
        val collected: Boolean?,
        val consumption: Consumption?,
        val cover: Cover?,
        val date: Long?,
        val description: String?,
        val descriptionEditor: String?,
        val descriptionPgc: String?,
        val duration: Long?,
        val favoriteAdTrack: Any?,
        val id: Int?,
        val idx: Int?,
        val ifLimitVideo: Boolean?,
        val label: Any?,
        val labelList: List<Any>?,
        val lastViewTime: Any?,
        val library: String?,
        val playInfo: List<PlayInfo>?,
        val playUrl: String?,
        val played: Boolean?,
        val playlists: Any?,
        val promotion: Any?,
        val provider: Provider?,
        val reallyCollected: Boolean?,
        val recallSource: Any?,
        val recall_source: Any?,
        val releaseTime: Long?,
        val remark: Any?,
        val resourceType: String?,
        val searchWeight: Int?,
        val shareAdTrack: Any?,
        val slogan: Any?,
        val src: Any?,
        val subtitles: List<Any>?,
        val tags: MutableList<Tag>?,
        val thumbPlayUrl: Any?,
        val title: String?,
        val titlePgc: String?,
        val type: String?,
        val videoPosterBean: Any?,
        val waterMarks: Any?,
        val webAdTrack: Any?,
        val webUrl: WebUrl?,
        val image: String?,
        val actionUrl: String?,
        val text: String?,
        val owner: Owner?,
        val width: Int?,
        val height: Int?
    )

    data class Content(
        val adIndex: Int,
        val `data`: Data,
        val id: Int,
        val tag: Any,
        val trackingData: Any,
        val type: String
    )

    data class Header(
        val actionUrl: String,
        val cover: Any,
        val description: String,
        val font: Any,
        val icon: String,
        val iconType: String,
        val id: Int,
        val label: Any,
        val labelList: Any,
        val rightText: Any,
        val showHateVideo: Boolean,
        val subTitle: Any,
        val subTitleFont: Any,
        val textAlign: String,
        val time: Long,
        val title: String
    )

    data class ItemX(
        val adIndex: Int,
        val `data`: Data,
        val id: Int,
        val tag: Any?,
        val type: String,
        val trackingData: Any?,
    )

    data class Author(
        val adTrack: Any,
        val approvedNotReadyVideoCount: Int,
        val description: String,
        val expert: Boolean,
        val follow: Follow,
        val icon: String?,
        val id: Int,
        val ifPgc: Boolean,
        val latestReleaseTime: Long,
        val link: String,
        val name: String?,
        val recSort: Int,
        val shield: Shield,
        val videoNum: Int
    )

    data class Consumption(
        val collectionCount: Int,
        val realCollectionCount: Int,
        val replyCount: Int,
        val shareCount: Int
    )

    data class Cover(
        val blurred: String,
        val detail: String?,
        val feed: String,
        val homepage: Any,
        val sharing: Any
    )

    data class PlayInfo(
        val height: Int,
        val name: String,
        val type: String,
        val url: String,
        val urlList: List<Url>,
        val width: Int
    )

    data class Provider(
        val alias: String,
        val icon: String,
        val name: String
    )

    data class Tag(
        val actionUrl: String,
        val adTrack: Any,
        val bgPicture: String,
        val childTagIdList: Any,
        val childTagList: Any,
        val communityIndex: Int,
        val desc: String,
        val haveReward: Boolean,
        val headerImage: String,
        val id: Int,
        val ifNewest: Boolean,
        val name: String?,
        val newestEndTime: Any,
        val tagRecType: String
    )

    data class WebUrl(
        val forWeibo: String,
        val raw: String
    )

    data class Follow(
        val followed: Boolean,
        val itemId: Int,
        val itemType: String
    )

    data class Shield(
        val itemId: Int,
        val itemType: String,
        val shielded: Boolean
    )

    data class Url(
        val name: String,
        val size: Int,
        val url: String
    )

    data class Owner(
        val actionUrl: String,
        val area: Any,
        val avatar: String,
        val birthday: Any,
        val city: Any,
        val country: Any,
        val cover: String,
        val description: String,
        val expert: Boolean,
        val followed: Boolean,
        val gender: String,
        val ifPgc: Boolean,
        val job: Any,
        val library: String,
        val limitVideoOpen: Boolean,
        val nickname: String,
        val registDate: Long,
        val releaseDate: Long,
        val uid: Int,
        val university: Any,
        val userType: String
    )

    override var itemType: Int = Constant.ITEM_TYPE_LIST_PLAY

}
