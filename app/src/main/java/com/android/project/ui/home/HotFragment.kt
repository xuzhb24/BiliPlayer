package com.android.project.ui.home

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.base.BaseListFragment
import com.android.project.R
import com.android.project.adapter.CommonAdapter
import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.util.Constant
import com.android.universal.databinding.LayoutListBinding
import com.android.util.LayoutParamsUtil
import com.android.util.ScreenUtil
import com.android.util.SizeUtil
import com.android.video.util.AutoPlayScrollListener
import com.android.widget.recyclerView.CustomLoadMoreView
import com.chad.library.adapter.base.module.LoadMoreModule
import com.shuyu.gsyvideoplayer.GSYVideoManager

/**
 * Created by xuzhb on 2021/12/30
 * Desc:热门
 */
class HotFragment : BaseListFragment<ItemBean, LayoutListBinding, CommonListViewModel>() {

    companion object {
        fun newInstance() = HotFragment()
    }

    private var isFullScreen = false
    private var mAutoPlayScrollListener: AutoPlayScrollListener? = null

    override fun getAdapter() = CommonAdapter()

    override fun getFirstPageUrl() = UrlConstant.ALLREC

    override fun initAdapter() {
        mRecyclerView?.layoutManager = GridLayoutManager(context, 2)
        mAdapter = getAdapter()
        if (mAdapter is LoadMoreModule) {  //上拉加载更多
            mAdapter.loadMoreModule.loadMoreView = CustomLoadMoreView(R.layout.view_load_more_for_list)  //增加无数据时的脚布局高度，以便最后一个Item能够自动播放视频
            mAdapter.loadMoreModule.setOnLoadMoreListener {
                viewModel.loadData(mNextPageUrl, mLoadingLayout != null, mLoadingLayout == null && isFirstLoad())
            }
        }
        mRecyclerView?.adapter = mAdapter
    }

    override fun handleView(savedInstanceState: Bundle?) {
        binding.rootFl.setBackgroundColor(Color.parseColor("#F3F3F5"))
        mLoadingLayout?.setBackgroundColor(Color.parseColor("#F3F3F5"))
        LayoutParamsUtil.setPadding(
            mRecyclerView!!,
            SizeUtil.dp2pxInt(3.5f), 0,
            SizeUtil.dp2pxInt(3.5f), 0
        )
    }

    override fun initListener() {
        val playTop = ScreenUtil.getScreenHeight() / 2 - SizeUtil.dp2pxInt(300f)
        val playBottom = ScreenUtil.getScreenHeight() / 2 + SizeUtil.dp2pxInt(300f)
        mAutoPlayScrollListener = AutoPlayScrollListener(R.id.video_player, playTop, playBottom)
        val manager: LinearLayoutManager = mRecyclerView!!.layoutManager as LinearLayoutManager
        mRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                mAutoPlayScrollListener?.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisibleItem = manager.findFirstVisibleItemPosition()
                val lastVisibleItem = manager.findLastVisibleItemPosition()
                //滑动自动播放
                if (!isFullScreen) {
                    mAutoPlayScrollListener?.visibleCount = lastVisibleItem - firstVisibleItem
                }
            }
        })
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val item = mAdapter.getItem(position)
            if (item.type != Constant.TYPE_SQUARE_CARD && item.type != Constant.TYPE_TEXT_CARD) {
                VideoDetailActivity.start(mContext, item)
            }
        }
    }

    override fun convertData(response: MutableList<ItemBean>?): MutableList<ItemBean>? {
        val list: MutableList<ItemBean> = mutableListOf()
        response?.forEach {
            when (it.type) {
                Constant.TYPE_SQUARE_CARD -> {
                    it.itemType = Constant.ITEM_TYPE_SHOW_TITLE
                    list.add(it)
                    it.data.itemList?.forEach {
                        if (it.type == Constant.TYPE_FOLLOW_CARD) {
                            val item = ItemBean(it.adIndex, it.data, it.id, it.tag, it.trackingData, it.type)
                            item.itemType = Constant.ITEM_TYPE_LIST_PLAY
                            list.add(item)
                        }
                    }
                }
                Constant.TYPE_TEXT_CARD -> {
                    it.itemType = Constant.ITEM_TYPE_SHOW_TITLE
                    list.add(it)
                }
                Constant.TYPE_FOLLOW_CARD -> {
                    it.itemType = Constant.ITEM_TYPE_LIST_PLAY
                    list.add(it)
                }
                Constant.TYPE_VIDEO_SMALL_CARD -> {
                    it.itemType = Constant.ITEM_TYPE_SHOW_COVER
                    list.add(it)
                }
            }
        }
        return list
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isFullScreen = newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
        if (hasDataLoadedSuccess) {
            //切换回来后播放当前页面可见的视频
            mAutoPlayScrollListener?.onScrollStateChanged(mRecyclerView, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

}