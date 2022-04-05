package com.android.project.ui.common

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.base.BaseListFragment
import com.android.base.BaseListViewModel
import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.universal.databinding.LayoutListBinding
import com.android.util.ScreenUtil
import com.android.util.SizeUtil
import com.android.video.util.AutoPlayScrollListener
import com.android.widget.recyclerView.CustomLoadMoreView
import com.chad.library.adapter.base.module.LoadMoreModule
import com.shuyu.gsyvideoplayer.GSYVideoManager
import java.lang.reflect.ParameterizedType

/**
 * Created by xuzhb on 2022/3/31
 * Desc:通用的列表视频播放Fragment
 */
abstract class CommonListPlayFragment<VM : CommonListViewModel> : BaseListFragment<ItemBean, LayoutListBinding, VM>() {

    private var isFullScreen = false
    private var mAutoPlayScrollListener: AutoPlayScrollListener? = null

    override fun initViewBindingAndViewModel() {
        binding = LayoutListBinding.inflate(layoutInflater)
        val superclass = javaClass.genericSuperclass
        val vmClass = (superclass as ParameterizedType).actualTypeArguments[0] as Class<VM>
        viewModel = ViewModelProvider(this).get(vmClass)
    }

    override fun initAdapter() {
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
        mLoadingLayout?.setBackgroundColor(Color.parseColor("#F3F3F5"))
    }

    override fun initListener() {
        mAutoPlayScrollListener = object : AutoPlayScrollListener(R.id.video_player, getPlayRangeTop(), getPlayRangeBottom()) {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                mSwipeRefreshLayout?.isEnabled = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() == 0
            }
        }
        mRecyclerView?.addOnScrollListener(mAutoPlayScrollListener!!)
    }

    //视频播放区域的上边界
    protected open fun getPlayRangeTop() = ScreenUtil.getScreenHeight() / 2 - SizeUtil.dp2pxInt(220f)

    //视频播放区域的下边界
    protected open fun getPlayRangeBottom() = ScreenUtil.getScreenHeight() / 2 + SizeUtil.dp2pxInt(160f)

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
//        GSYVideoManager.onResume()
        if (hasDataLoadedSuccess) {
            //切换回来后播放当前页面可见的视频
            mAutoPlayScrollListener?.onResume(mRecyclerView!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

}