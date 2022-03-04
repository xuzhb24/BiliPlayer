package com.android.project.entity

/**
 * Created by xuzhb on 2022/2/15
 * Desc:评论
 */
//{
//	"type": "reply",
//	"data": {
//		"dataType": "ReplyBeanForClient",
//		"id": 1323124094824087552,
//		"videoId": 103758,
//		"videoTitle": "国际红十字会：战争的规则",
//		"parentReplyId": 0,
//		"rootReplyId": 1323124094824087552,
//		"sequence": 14,
//		"message": "滑稽至极",
//		"replyStatus": "PUBLISHED",
//		"createTime": 1604292341000,
//		"user": {
//			"uid": 302138436,
//			"nickname": "三两天不睡觉",
//			"avatar": "http://thirdwx.qlogo.cn/mmopen/vi_32/vWFt70ADeBaCqYLpiaDxF6ofTtYStfPqokM7TaR6JuQ2xSyCTg5PFC24ibCoEoSLBeCibxqWNiaVXvNVdQTFMIjciag/132",
//			"userType": "NORMAL",
//			"ifPgc": false,
//			"description": "",
//			"area": null,
//			"gender": null,
//			"registDate": 1540252193000,
//			"releaseDate": null,
//			"cover": null,
//			"actionUrl": "eyepetizer://pgc/detail/302138436/?title=%E4%B8%89%E4%B8%A4%E5%A4%A9%E4%B8%8D%E7%9D%A1%E8%A7%89&userType=NORMAL&tabIndex=0",
//			"followed": false,
//			"limitVideoOpen": false,
//			"library": "BLOCK",
//			"birthday": 0,
//			"country": null,
//			"city": null,
//			"university": null,
//			"job": null,
//			"expert": false
//		},
//		"likeCount": 0,
//		"liked": false,
//		"hot": false,
//		"userType": null,
//		"type": "video",
//		"actionUrl": null,
//		"imageUrl": null,
//		"ugcVideoId": null,
//		"recommendLevel": "not_recommend",
//		"parentReply": null,
//		"showParentReply": true,
//		"showConversationButton": false,
//		"ugcVideoUrl": null,
//		"cover": null,
//		"userBlocked": false,
//		"sid": "1323124094824087552"
//	},
//	"trackingData": null,
//	"tag": null,
//	"id": 0,
//	"adIndex": -1
//}
data class ReplyBean(
    val adIndex: Int,
    val `data`: Data?,
    val id: Int,
    val tag: Any,
    val trackingData: Any,
    val type: String
) {

    data class Data(
        val actionUrl: String,
        val adTrack: Any,
        val cover: Any,
        val createTime: Long?,
        val dataType: String,
        val font: String,
        val hot: Boolean,
        val id: Long,
        val imageUrl: String,
        val likeCount: Int?,
        val liked: Boolean,
        val message: String?,
        val parentReply: Any,
        val parentReplyId: Long,
        val recommendLevel: String,
        val replyStatus: String,
        val rootReplyId: Long,
        val sequence: Int,
        val showConversationButton: Boolean,
        val showParentReply: Boolean,
        val sid: String,
        val text: String,
        val type: String,
        val ugcVideoId: Any,
        val ugcVideoUrl: Any,
        val user: User?,
        val userBlocked: Boolean,
        val userType: Any,
        val videoId: Int,
        val videoTitle: String
    )

    data class User(
        val actionUrl: String,
        val area: Any,
        val avatar: String?,
        val birthday: Long,
        val city: Any,
        val country: String,
        val cover: String,
        val description: String,
        val expert: Boolean,
        val followed: Boolean,
        val gender: String,
        val ifPgc: Boolean,
        val job: String,
        val library: String,
        val limitVideoOpen: Boolean,
        val nickname: String?,
        val registDate: Long,
        val releaseDate: Long,
        val uid: Int,
        val university: String,
        val userType: String
    )

}
