package com.android.project.ui.home

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.base.BaseListFragment
import com.android.project.R
import com.android.project.adapter.ListVideoAdapter
import com.android.project.entity.ItemBean
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.widget.LoadMoreViewForList
import com.android.universal.databinding.LayoutListBinding
import com.android.util.ScreenUtil
import com.android.util.SizeUtil
import com.android.video.util.ScrollCalculatorHelper
import com.chad.library.adapter.base.module.LoadMoreModule
import com.shuyu.gsyvideoplayer.GSYVideoManager

/**
 * Created by xuzhb on 2022/1/25
 * Desc:通用的列表视频的Fragment
 */
abstract class CommonListVideoFragment : BaseListFragment<ItemBean, LayoutListBinding, CommonListViewModel>() {

    private var isFullScreen = false
    private var mScrollCalculatorHelper: ScrollCalculatorHelper? = null

    override fun initViewBindingAndViewModel() {
        binding = LayoutListBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(CommonListViewModel::class.java)
    }

    override fun initAdapter() {
        mAdapter = getAdapter()
        if (mAdapter is LoadMoreModule) {  //上拉加载更多
            mAdapter.loadMoreModule.loadMoreView = LoadMoreViewForList()  //增加无数据时的脚布局高度，以便最后一个Item能够自动播放视频
            mAdapter.loadMoreModule.setOnLoadMoreListener {
                viewModel.loadData(mNextPageUrl, mLoadingLayout != null, mLoadingLayout == null && isFirstLoad())
            }
        }
        mRecyclerView?.adapter = mAdapter
    }

    override fun getAdapter() = ListVideoAdapter()

    override fun handleView(savedInstanceState: Bundle?) {
        mLoadingLayout?.setBackgroundColor(Color.parseColor("#F3F3F5"))
    }

    override fun initListener() {
        val playTop = ScreenUtil.getScreenHeight() / 2 - SizeUtil.dp2pxInt(220f)
        val playBottom = ScreenUtil.getScreenHeight() / 2 + SizeUtil.dp2pxInt(160f)
        mScrollCalculatorHelper = ScrollCalculatorHelper(R.id.video_player, playTop, playBottom)
        val manager: LinearLayoutManager = mRecyclerView!!.layoutManager as LinearLayoutManager
        mRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                mScrollCalculatorHelper?.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisibleItem = manager.findFirstVisibleItemPosition()
                val lastVisibleItem = manager.findLastVisibleItemPosition()
                //滑动自动播放
                if (!isFullScreen) {
                    mScrollCalculatorHelper?.visibleCount = lastVisibleItem - firstVisibleItem
                }
            }
        })
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val item = mAdapter.getItem(position)
            VideoDetailActivity.start(mContext, item)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isFullScreen = newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER
    }

    fun onBackPressed(): Boolean {
        if (GSYVideoManager.backFromWindowFull(activity)) {
            return true
        }
        return false
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
            mScrollCalculatorHelper?.onScrollStateChanged(mRecyclerView, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

}