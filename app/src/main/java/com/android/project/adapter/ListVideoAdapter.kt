package com.android.project.adapter

import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.util.*
import com.android.util.DateUtil
import com.android.util.formatCountStr
import com.android.util.gone
import com.android.video.player.ListVideoPlayer
import com.android.video.util.VideoConfig
import com.android.widget.recyclerView.LoadMoreAdapter
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack

/**
 * Created by xuzhb on 2022/1/11
 * Desc:列表播放器适配器，可播放
 */
class ListVideoAdapter : LoadMoreAdapter<ItemBean>(R.layout.item_list_video) {

    companion object {
        private const val TAG = "ListVideoAdapter"
    }

    override fun convert(holder: BaseViewHolder, item: ItemBean) {
        val player: ListVideoPlayer = holder.getView(R.id.video_player)
        player.setData(
            formatCountStr(getVideoShareCount(item)),
            formatCountStr(getVideoReplyCount(item)),
            DateUtil.getDuration(getVideoDuration(item))
        )
        val builder = GSYVideoOptionBuilder()
        builder.setIsTouchWiget(false)
            .setUrl(getVideoPlayUrl(item))
            .setVideoTitle(getVideoTitle(item))
            .setCacheWithPlay(false)
            .setRotateViewAuto(false)
            .setLockLand(true)
            .setPlayTag(TAG)
            .setShowFullAnimation(false)
            .setNeedLockFull(true)
            .setPlayPosition(holder.adapterPosition)
            .setVideoAllCallBack(object : GSYSampleCallBack() {

                override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                    super.onQuitFullscreen(url, *objects)
                    GSYVideoManager.instance().isNeedMute = VideoConfig.muteInTheList
                }

                override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
                    super.onEnterFullscreen(url, *objects)
                    GSYVideoManager.instance().isNeedMute = false
                    player.currentPlayer.titleTextView.text = objects[0] as String
                }

            }).build(player)
        with(player) {
            titleTextView.gone()
            backButton.gone()
            fullscreenButton.setOnClickListener {
                startWindowFullscreen(context, true, true)
            }
            //非全屏时点击进入详情页
            setOnNormalClickListener {
                VideoDetailActivity.start(context, item)
            }
            loadCoverImage(getVideoCover(item), R.drawable.ic_place_holder)
            //自动播放第一个视频
            com.android.util.postDelayed(500) {
                if (holder.adapterPosition == 0) {
                    startPlayLogic()
                }
            }
        }
        val headerImage = getVideoAuthor(item)?.icon ?: getFirstTag(item)?.headerImage
        showImageByCenterCrop(
            holder.getView(R.id.head_iv), headerImage ?: "", CircleCrop(),
            R.drawable.ic_place_holder_square, R.drawable.ic_place_holder_square
        )
        val name = getVideoAuthor(item)?.name ?: getFirstTag(item)?.name
        val category = getVideoCategory(item)
        val authorDesc = "$name${if (category.isBlank()) "" else "  # $category"}"  //描述
        holder.setText(R.id.title_tv, getVideoTitle(item))
            .setText(R.id.up_tv, authorDesc)
            .setText(R.id.praise_count_tv, formatCountStr(getVideoCollectionCount(item), "0"))
    }

}