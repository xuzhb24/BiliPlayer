package com.android.project.adapter

import android.view.View
import android.widget.SeekBar
import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.util.*
import com.android.util.formatCountStr
import com.android.util.gone
import com.android.util.invisible
import com.android.util.visible
import com.android.video.player.SwitchVideoPlayer
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack

/**
 * Created by xuzhb on 2022/3/19
 * Desc:上下切换
 */
class SwitchDetailAdapter : BaseQuickAdapter<ItemBean, BaseViewHolder>(R.layout.item_switch_detail) {

    companion object {
        private const val TAG = "SwitchDetailAdapter"
    }

    override fun convert(holder: BaseViewHolder, item: ItemBean) {
        val videoWidth = item.data.content?.data?.width ?: 0
        val videoHeight = item.data.content?.data?.height ?: 0
        val isPortrait = videoWidth < videoHeight
        val contentCl: View = holder.getView(R.id.content_cl)
        val player: SwitchVideoPlayer = holder.getView(R.id.video_player)
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
            .setShrinkImageRes(if (isPortrait) R.drawable.ic_video_shrink_port else R.drawable.ic_video_shrink)
            .setEnlargeImageRes(if (isPortrait) R.drawable.ic_video_enlarge_port else R.drawable.ic_video_enlarge)
            .setVideoAllCallBack(object : GSYSampleCallBack() {

                override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                    super.onQuitFullscreen(url, *objects)
                }

                override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
                    super.onEnterFullscreen(url, *objects)
                    player.currentPlayer.titleTextView.text = objects[0] as String
                }

            }).build(player)
        with(player) {
            titleTextView.gone()
            backButton.gone()
            fullscreenButton.setOnClickListener {
                if (isPortrait) {
                    holder.setGone(R.id.content_cl, contentCl.visibility == View.VISIBLE)
                } else {
                    startWindowFullscreen(context, true, true)
                }
            }
//            loadCoverImage(getVideoCover(item), -1)
            setVideoInfo(ConvertEntityUtil.item2VideoBean(item))
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        if (contentCl.visibility != View.GONE) {
                            contentCl.invisible()
                            holder.getView<View>(R.id.small_bottom_control_ll).invisible()
                            holder.getView<View>(R.id.fullscreen).invisible()
                        }
                    } else {
                        if (contentCl.visibility != View.GONE) {
                            contentCl.visible()
                            holder.getView<View>(R.id.small_bottom_control_ll).visible()
                            holder.getView<View>(R.id.fullscreen).visible()
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (contentCl.visibility != View.GONE) {
                        contentCl.visible()
                        holder.getView<View>(R.id.small_bottom_control_ll).visible()
                        holder.getView<View>(R.id.fullscreen).visible()
                    }
                }
            })
        }
        val headerImage = item.data.content?.data?.owner?.avatar ?: ""
        showCircleImageByCenterCrop(holder.getView(R.id.head_iv), headerImage)
        holder.setText(R.id.name_tv, item.data.content?.data?.owner?.nickname ?: "")
            .setText(R.id.desc_tv, item.data.content?.data?.owner?.description ?: "")
            .setText(R.id.title_tv, getVideoDescription(item))
            .setText(R.id.praise_tv, formatCountStr(getVideoCollectionCount(item), "点赞"))
            .setText(R.id.comment_tv, formatCountStr(getVideoReplyCount(item), "评论"))
            .setText(R.id.collect_tv, formatCountStr(getVideoRealCollectionCount(item), "收藏"))
            .setText(R.id.share_tv, formatCountStr(getVideoShareCount(item), "分享"))
    }

}