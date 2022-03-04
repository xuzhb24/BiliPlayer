package com.android.project.ui.home

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.base.BaseListFragment
import com.android.project.R
import com.android.project.adapter.CoverVideoAdapter
import com.android.project.adapter.banner.BannerImageTitleAdapter
import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.util.Constant
import com.android.project.util.FilterUtil
import com.android.universal.databinding.LayoutListBinding
import com.android.util.ScreenUtil
import com.android.util.SizeUtil
import com.android.util.StatusBarUtil
import com.android.util.ToastUtil
import com.android.video.util.ScrollCalculatorHelper
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.youth.banner.config.IndicatorConfig
import com.youth.banner.indicator.CircleIndicator
import kotlinx.android.synthetic.main.item_recommend_header.view.*

/**
 * Created by xuzhb on 2021/12/30
 * Desc:推荐
 */
class RecommendFragment : BaseListFragment<ItemBean, LayoutListBinding, RecommendViewModel>() {

    companion object {
        fun newInstance() = RecommendFragment()
    }

    private var isFullScreen = false
    private var mScrollCalculatorHelper: ScrollCalculatorHelper? = null
    private var hasNoticeDataChange = false

    override fun getAdapter() = CoverVideoAdapter()

    override fun getFirstPageUrl() = UrlConstant.FEED

    override fun initAdapter() {
        mRecyclerView?.layoutManager = GridLayoutManager(context, 2)
        super.initAdapter()
    }

    override fun handleView(savedInstanceState: Bundle?) {
        binding.rootFl.setBackgroundColor(Color.parseColor("#F3F3F5"))
        mLoadingLayout?.setBackgroundColor(Color.parseColor("#F3F3F5"))
//        LayoutParamsUtil.setPadding(
//            mRecyclerView!!,
//            SizeUtil.dp2pxInt(3.5f), SizeUtil.dp2pxInt(3.5f),
//            SizeUtil.dp2pxInt(3.5f), 0
//        )
    }

    override fun observerListDataChange() {
        viewModel.bannerData.observe(this, {
            initBanner(it)
        })
        super.observerListDataChange()
    }

    override fun initListener() {
        val playTop = ScreenUtil.getScreenHeight() / 2 - SizeUtil.dp2pxInt(300f)
        val playBottom = ScreenUtil.getScreenHeight() / 2 + SizeUtil.dp2pxInt(300f)
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

    override fun convertData(response: MutableList<ItemBean>?): MutableList<ItemBean>? {
        val list: MutableList<ItemBean> = FilterUtil.filterVideoList(response)
        for (i in list.indices) {
            val existsDataSize = if (isFirstLoad()) 0 else mAdapter.data.size
            if ((i + 1 + existsDataSize) % 9 == 0) {
                //每隔8个取一个列表播放
                list[i].itemType = Constant.ITEM_TYPE_LIST_PLAY
            } else {
                list[i].itemType = Constant.ITEM_TYPE_SHOW_COVER
            }
        }
        if (!hasNoticeDataChange && list.size > 0) {
            val yOffset = StatusBarUtil.getStatusBarHeight(mContext) + SizeUtil.dp2pxInt(99f)
            ToastUtil.showToast("发现${list.size}条新内容", Gravity.TOP, 0, yOffset)
            hasNoticeDataChange = true
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
            mScrollCalculatorHelper?.onScrollStateChanged(mRecyclerView, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    private fun initBanner(list: MutableList<ItemBean.ItemX>?) {
        val headerView = LayoutInflater.from(mContext).inflate(R.layout.item_recommend_header, null)
        if (!list.isNullOrEmpty()) {
            headerView.banner.addBannerLifecycleObserver(this)
                .setLoopTime(5000)
                .setAdapter(BannerImageTitleAdapter(list))
                .setOnBannerListener { data, position ->

                }.setIndicator(CircleIndicator(mContext))
                .setIndicatorGravity(IndicatorConfig.Direction.RIGHT)
                .setIndicatorWidth(SizeUtil.dp2pxInt(4.8f), SizeUtil.dp2pxInt(4.8f))
                .setIndicatorHeight(SizeUtil.dp2pxInt(4.8f))
                .setIndicatorNormalColor(Color.parseColor("#80ffffff"))
                .setIndicatorSelectedColor(Color.parseColor("#ffffff"))
                .setIndicatorSpace(SizeUtil.dp2pxInt(3.5f))
                .setIndicatorMargins(IndicatorConfig.Margins(0, 0, SizeUtil.dp2pxInt(10f), SizeUtil.dp2pxInt(8f)))
            mAdapter.setHeaderView(headerView)
        } else {
            mAdapter.removeAllHeaderView()
        }
    }

}