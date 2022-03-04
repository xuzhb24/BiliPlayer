package com.android.project.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.util.*
import com.android.util.DateUtil
import com.android.util.formatCountStr
import com.android.util.gone
import com.android.video.player.ListVideoPlayer
import com.android.video.util.VideoConfig
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack

/**
 * Created by xuzhb on 2022/1/11
 * Desc:封面播放器适配器，只展示封面
 */
class CoverVideoAdapter : BaseMultiItemQuickAdapter<ItemBean, BaseViewHolder>(), LoadMoreModule {

    companion object {
        private const val TAG = "CoverVideoAdapter"
    }

    init {
        addItemType(Constant.ITEM_TYPE_SHOW_COVER, R.layout.item_cover_video1)
        addItemType(Constant.ITEM_TYPE_LIST_PLAY, R.layout.item_cover_video2)
    }

    override fun convert(holder: BaseViewHolder, item: ItemBean) {
        when (item.itemType) {
            Constant.ITEM_TYPE_SHOW_COVER -> initCoverVideo1(holder, item)
            Constant.ITEM_TYPE_LIST_PLAY -> initCoverVideo2(holder, item)
        }
    }

    //只显示视频封面
    private fun initCoverVideo1(holder: BaseViewHolder, item: ItemBean) {
        showImageByCenterCrop(holder.getView(R.id.cover_iv), getVideoCover(item), 3.5f, 3.5f)
        holder.setText(R.id.share_tv, formatCountStr(getVideoShareCount(item)))
            .setText(R.id.comment_tv, formatCountStr(getVideoReplyCount(item)))
            .setText(R.id.duration_tv, DateUtil.getDuration(getVideoDuration(item)))
            .setText(R.id.title_tv, getVideoTitle(item))
            .setText(R.id.up_tv, getVideoAuthor(item)?.name ?: getFirstTag(item)?.name ?: "")
    }

    //列表播放视频
    private fun initCoverVideo2(holder: BaseViewHolder, item: ItemBean) {
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
        }
        holder.setText(R.id.title_tv, getVideoTitle(item))
            .setText(R.id.praise_count_tv, formatCountStr(getVideoCollectionCount(item), "0"))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    //如果当前是footer的位置，那么该item占据1个单元格，正常情况下占据2个单元格
                    //注意RecyclerView要先设置layoutManager再设置adapter
                    return if (getItemViewType(position) == HEADER_VIEW
                        || getItemViewType(position) == LOAD_MORE_VIEW
                        || getItemViewType(position) == Constant.ITEM_TYPE_LIST_PLAY
                    ) manager.spanCount else 1
                }

            }
        }
    }

}