package com.android.project.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.ui.detail.PhotoDetailActivity
import com.android.project.ui.detail.PhotoViewActivity
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.util.*
import com.android.util.*
import com.android.video.player.TrendsVideoPlayer
import com.android.widget.ExpandTextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder

/**
 * Created by xuzhb on 2022/3/29
 * Desc:动态-视频、综合
 */
class CommonTrendsAdapter : BaseMultiItemQuickAdapter<ItemBean, BaseViewHolder>(), LoadMoreModule {

    companion object {
        private const val TAG = "CommonTrendsAdapter"
    }

    init {
        addItemType(Constant.ITEM_TYPE_VIDEO, R.layout.item_trends_video)
        addItemType(Constant.ITEM_TYPE_PHOTO, R.layout.item_trends_photo)
    }

    override fun convert(holder: BaseViewHolder, item: ItemBean) {
        when (item.itemType) {
            Constant.ITEM_TYPE_VIDEO -> initVideo(holder, item)
            Constant.ITEM_TYPE_PHOTO -> initPhoto(holder, item)
        }
    }

    private fun initVideo(holder: BaseViewHolder, item: ItemBean) {
        val rootCl: ConstraintLayout = holder.getView(R.id.root_cl)
        val width = item.data.content?.data?.width ?: 0
        val height = item.data.content?.data?.height ?: 0
        LogUtil.i("width x height", "$width x $height")
        if (width < height) {  //竖屏
            ConstraintSet().apply {
                clone(rootCl)
                setDimensionRatio(R.id.video_player, "h,64:72")
                applyTo(rootCl)
            }
        } else {
            ConstraintSet().apply {
                clone(rootCl)
                setDimensionRatio(R.id.video_player, "h,64:36")
                applyTo(rootCl)
            }
        }
        val player: TrendsVideoPlayer = holder.getView(R.id.video_player)
        player.setData(
            formatCountStr(getVideoShareCount(item), "0"),
            formatCountStr(getVideoReplyCount(item), "0"),
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
            .build(player)
        with(player) {
            titleTextView.gone()
            backButton.gone()
            //非全屏时点击进入详情页
            setOnNormalClickListener {
                VideoDetailActivity.start(context, item)
            }
            loadCoverImage(getVideoCover(item), R.drawable.ic_place_holder)
            //自动播放第一个视频
            postDelayed(500) {
                if (holder.adapterPosition == 0) {
                    startPlayLogic()
                }
            }
        }
        val owner = item.data.content?.data?.owner
        showCircleImageByCenterCrop(holder.getView(R.id.head_iv), owner?.avatar ?: "")
        val titleEtv: ExpandTextView = holder.getView(R.id.title_etv)
        titleEtv.contentText = getVideoDescription(item)
        titleEtv.setOnTextClickListener(object : ExpandTextView.OnTextClickListener {
            override fun onContentTextClick(isExpand: Boolean) {
                VideoDetailActivity.start(context, item)
            }

            override fun onLabelTextClick(isExpand: Boolean) {
                titleEtv.isExpand = !isExpand
            }
        })
        holder.setText(R.id.name_tv, owner?.nickname ?: "")
            .setText(R.id.desc_tv, if (owner?.description.isNullOrBlank()) "ta暂时没有简介！" else owner?.description)
            .setText(R.id.share_tv, formatCountStr(getVideoShareCount(item), "转发"))
            .setText(R.id.comment_tv, formatCountStr(getVideoReplyCount(item), "评论"))
            .setText(R.id.praise_tv, formatCountStr(getVideoCollectionCount(item), "点赞"))
    }

    private fun initPhoto(holder: BaseViewHolder, item: ItemBean) {
        val urls = item.data.content?.data?.urls ?: mutableListOf()
        val photoRv: RecyclerView = holder.getView(R.id.photo_rv)
        var spanCount = 0
        var marginEnd = 0
        when (urls.size) {
            1 -> {
                spanCount = 1
                marginEnd = SizeUtil.dp2pxInt(105f)
            }
            2, 4 -> {
                spanCount = 2
                marginEnd = ((ScreenUtil.getScreenWidth() - SizeUtil.dp2px(21f)) / 3 + SizeUtil.dp2px(10.5f)).toInt()
            }
            else -> {
                spanCount = 3
                marginEnd = SizeUtil.dp2pxInt(10.5f)
            }
        }
        LayoutParamsUtil.setMarginRight(photoRv, marginEnd)
        photoRv.layoutManager = GridLayoutManager(context, spanCount)
        val result: MutableList<String> = mutableListOf()
        if (urls.size > 9) {
            result.addAll(urls.subList(0, 9))
        } else {
            result.addAll(urls)
        }
        photoRv.adapter = GridPhotoAdapter(urls.size == 1).apply {
            setNewInstance(result)
            setOnItemClickListener { adapter, view, position ->
                if (getItem(position).isNotBlank()) {
                    PhotoViewActivity.start(context, item, position)
                }
            }
        }
        val owner = item.data.content?.data?.owner
        showCircleImageByCenterCrop(holder.getView(R.id.head_iv), owner?.avatar ?: "")
        val titleEtv: ExpandTextView = holder.getView(R.id.title_etv)
        titleEtv.contentText = getVideoDescription(item)
        titleEtv.setOnTextClickListener(object : ExpandTextView.OnTextClickListener {
            override fun onContentTextClick(isExpand: Boolean) {
                when (item.itemType) {
                    Constant.ITEM_TYPE_VIDEO -> VideoDetailActivity.start(context, item)
                    Constant.ITEM_TYPE_PHOTO -> PhotoDetailActivity.start(context, item)
                }
            }

            override fun onLabelTextClick(isExpand: Boolean) {
                titleEtv.isExpand = !isExpand
            }
        })
        holder.setText(R.id.name_tv, owner?.nickname ?: "")
            .setText(R.id.desc_tv, if (owner?.description.isNullOrBlank()) "ta暂时没有简介！" else owner?.description)
            .setText(R.id.share_tv, formatCountStr(getVideoShareCount(item), "转发"))
            .setText(R.id.comment_tv, formatCountStr(getVideoReplyCount(item), "评论"))
            .setText(R.id.praise_tv, formatCountStr(getVideoCollectionCount(item), "点赞"))
    }

}