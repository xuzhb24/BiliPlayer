package com.android.project.ui.detail

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.TextView
import com.android.base.CommonBaseFragment
import com.android.project.R
import com.android.project.adapter.VideoReleatedAdapter
import com.android.project.databinding.FragmentVideoIntroduceBinding
import com.android.project.entity.ItemBean
import com.android.project.ui.entry.GuideActivity
import com.android.project.util.*
import com.android.util.*
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by xuzhb on 2022/2/11
 * Desc:视频详情页-简介
 */
class VideoIntroduceFragment : CommonBaseFragment<FragmentVideoIntroduceBinding>() {

    companion object {
        private const val TAG = "VideoIntroduceFragment"
        private const val EXTRA_ITEM_BEAN = "EXTRA_ITEM_BEAN"

        fun newInstance(item: ItemBean?) = VideoIntroduceFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_ITEM_BEAN, Gson().toJson(item))
            }
        }
    }

    private var mId: Int? = null  //视频id
    private var mItemBean: ItemBean? = null
    private var mReleatedList: MutableList<ItemBean>? = null
    private var mAdapter = VideoReleatedAdapter()
    private var showAllTag = true  //是否显示全部标签

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mItemBean = Gson().fromJson(arguments?.getString(EXTRA_ITEM_BEAN) ?: "{}", ItemBean::class.java)
        mItemBean?.let {
            mId = if (it.data.content != null) it.data.content.data.id else it.data.id
        }
        mReleatedList = Gson().fromJson(
            CacheUtil(mContext.applicationContext).getString(Constant.RELEATED_VIDEO_LIST),
            object : TypeToken<MutableList<ItemBean>>() {}.type
        )
        LogUtil.i(TAG, "releted list size:${mReleatedList?.size} first title:${getVideoTitle(mReleatedList?.takeIf { it.isNotEmpty() }?.get(0))}")
    }

    override fun handleView(savedInstanceState: Bundle?) {
        binding.topCl.gone()
        mItemBean?.let {
            binding.topCl.visible()
            showImageByCenterCrop(binding.headIv, getVideoAuthor(it)?.icon ?: "", CircleCrop())
            binding.upTv.text = getVideoAuthor(it)?.name
            binding.upDescTv.text = getVideoAuthor(it)?.description
            binding.titleTv.text = getVideoTitle(it)
            binding.commentTv.text = formatCountStr(getVideoReplyCount(it))
            binding.timeTv.text = DateUtil.long2String(getVideoReleaseTime(it), DateUtil.Y_M_D_H_M)
            binding.praiseTv.text = formatCountStr(getVideoCollectionCount(it), "点赞")
            binding.denyTv.text = "不喜欢"
            binding.toubiTv.text = "投币"
            binding.collectTv.text = formatCountStr(getVideoRealCollectionCount(it), "收藏")
            binding.shareTv.text = formatCountStr(getVideoShareCount(it), "分享")
            initTags(it)
        }
        if (!mReleatedList.isNullOrEmpty()) {
            mRecyclerView.visible()
            mRecyclerView?.adapter = mAdapter
            mAdapter.setNewInstance(mReleatedList)
        } else {
            mRecyclerView.gone()
        }
    }

    private fun initTags(itemBean: ItemBean) {
        val showTagCount = 4  //设置刚开始最多显示4个标签
        binding.tagFbl.removeAllViews()
        val tags = getData(itemBean)?.tags
        if (tags.isNullOrEmpty()) {
            binding.tagFbl.invisible()
            binding.tagMoreTv.gone()
        } else {
            if (tags.size <= showTagCount) {
                binding.tagFbl.visible()
                binding.tagMoreTv.gone()
                addTags(binding.tagFbl, tags)
                showAllTag = true
            } else {
                binding.tagFbl.visible()
                binding.tagMoreTv.visible()
                binding.tagMoreTv.text = "更多${tags.size - showTagCount}个"
                binding.tagMoreTv.setDrawables(mContext, R.drawable.ic_arrow_down_gray)
                val tempTags: MutableList<ItemBean.Tag> = mutableListOf()
                for (i in 0 until showTagCount) {
                    tempTags.add(tags[i])
                }
                addTags(binding.tagFbl, tempTags)
                showAllTag = false
            }
            binding.tagMoreTv.setOnClickListener {
                if (showAllTag) {  //收起
                    binding.tagMoreTv.text = "更多${tags.size - showTagCount}个"
                    binding.tagMoreTv.setDrawables(mContext, R.drawable.ic_arrow_down_gray)
                    val tempTags: MutableList<ItemBean.Tag> = mutableListOf()
                    for (i in 0 until showTagCount) {
                        tempTags.add(tags[i])
                    }
                    addTags(binding.tagFbl, tempTags)
                } else {  //展开
                    binding.tagMoreTv.text = ""
                    binding.tagMoreTv.setDrawables(mContext, R.drawable.ic_arrow_up_gray)
                    addTags(binding.tagFbl, tags)
                }
                showAllTag = !showAllTag
            }
        }
    }

    private fun addTags(flexboxLayout: FlexboxLayout, tags: MutableList<ItemBean.Tag>?) {
        flexboxLayout.removeAllViews()
        tags?.forEach {
            if (it.name.isNullOrBlank()) {
                return
            }
            val tv = TextView(context).apply {
                text = it.name
                setTextColor(Color.parseColor("#888888"))
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                background = DrawableUtil.createSolidShape(
                    SizeUtil.dp2px(17f),
                    Color.parseColor("#F4F4F4")
                )
                setPadding(
                    SizeUtil.dp2pxInt(10f), SizeUtil.dp2pxInt(5f),
                    SizeUtil.dp2pxInt(10f), SizeUtil.dp2pxInt(5f)
                )
                setOnClickListener {
                }
//            isSingleLine = true
//            ellipsize = TextUtils.TruncateAt.END
            }
            flexboxLayout.addView(tv)
            val params = tv.layoutParams as FlexboxLayout.LayoutParams
            params.rightMargin = SizeUtil.dp2pxInt(8f)
            params.bottomMargin = SizeUtil.dp2pxInt(8f)
        }
    }

    override fun initListener() {
        mAdapter.setOnItemClickListener { adapter, view, position ->
            VideoDetailActivity.start(mContext, mAdapter.getItem(position))
        }
        binding.focusTv.setOnClickListener {
            startActivity(GuideActivity::class.java)
        }
    }

}