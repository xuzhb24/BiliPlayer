package com.android.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.util.*
import com.android.util.*
import com.android.video.player.TrendsVideoPlayer
import com.android.widget.ExpandTextView
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
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
            com.android.util.postDelayed(500) {
                if (holder.adapterPosition == 0) {
                    startPlayLogic()
                }
            }
        }
        val owner = item.data.content?.data?.owner
        showImageByCenterCrop(holder.getView(R.id.head_iv), owner?.avatar ?: "", CircleCrop())
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
            .setText(R.id.desc_tv, if (owner?.description.isNullOrBlank()) "他暂时没有简介！" else owner?.description)
            .setText(R.id.share_tv, formatCountStr(getVideoShareCount(item), "转发"))
            .setText(R.id.comment_tv, formatCountStr(getVideoReplyCount(item), "评论"))
            .setText(R.id.praise_tv, formatCountStr(getVideoCollectionCount(item), "点赞"))
    }

    private fun initPhoto(holder: BaseViewHolder, item: ItemBean) {
        val urls = item.data.content?.data?.urls ?: mutableListOf()
        val photoRv: RecyclerView = holder.getView(R.id.photo_rv)
        val spanCount = if (urls.size == 1) 2 else 3
        photoRv.layoutManager = GridLayoutManager(context, spanCount)
        val result: MutableList<String> = mutableListOf()
        when {
            urls.size == 4 -> {
                result.add(urls[0])
                result.add(urls[1])
                result.add("")  //占位图
                result.add(urls[2])
                result.add(urls[3])
            }
            urls.size > 9 -> {
                result.addAll(urls.subList(0, 9))
            }
            else -> {
                result.addAll(urls)
            }
        }
        photoRv.adapter = GridPhotoAdapter().apply {
            setNewInstance(result)
        }
        val owner = item.data.content?.data?.owner
        showImageByCenterCrop(holder.getView(R.id.head_iv), owner?.avatar ?: "", CircleCrop())
        val titleEtv: ExpandTextView = holder.getView(R.id.title_etv)
        titleEtv.contentText = getVideoDescription(item)
        titleEtv.setOnTextClickListener(object : ExpandTextView.OnTextClickListener {
            override fun onContentTextClick(isExpand: Boolean) {
                when (item.itemType) {
                    Constant.ITEM_TYPE_VIDEO -> VideoDetailActivity.start(context, item)
                    Constant.ITEM_TYPE_PHOTO -> ToastUtil.toast("待实现")
                }
            }

            override fun onLabelTextClick(isExpand: Boolean) {
                titleEtv.isExpand = !isExpand
            }
        })
        holder.setText(R.id.name_tv, owner?.nickname ?: "")
            .setText(R.id.desc_tv, if (owner?.description.isNullOrBlank()) "他暂时没有简介！" else owner?.description)
            .setText(R.id.share_tv, formatCountStr(getVideoShareCount(item), "转发"))
            .setText(R.id.comment_tv, formatCountStr(getVideoReplyCount(item), "评论"))
            .setText(R.id.praise_tv, formatCountStr(getVideoCollectionCount(item), "点赞"))
    }

    private inner class GridPhotoAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_grid_photo) {

        override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            //为什么重写这个方法，因为默认实现有个BUG，不能使用inflater.inflate(R.layout.item_grid_photo, parent, false)
            //这样会导致使用GridLayoutManager只会显示第一行的控件，超过一行的控件不会显示
            //所以使用inflater.inflate(R.layout.item_grid_photo, null, false)
            val convertView = inflater.inflate(R.layout.item_grid_photo, null, false)
            return createBaseViewHolder(convertView)
        }

        override fun convert(holder: BaseViewHolder, item: String) {
            holder.setVisible(R.id.content_iv, item.isNotBlank())
            if (item.isNotBlank()) {
                showImageByCenterCrop(holder.getView(R.id.content_iv), item, 4f)
            }
        }
    }

}