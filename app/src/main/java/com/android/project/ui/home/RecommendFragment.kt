package com.android.project.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.android.project.R
import com.android.project.adapter.CommonHomeAdapter
import com.android.project.adapter.banner.BannerImageTitleAdapter
import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.ui.common.CommonListPlayFragment
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.ui.detail.WebDetailActivity
import com.android.project.util.Constant
import com.android.project.util.FilterUtil
import com.android.util.*
import com.android.widget.recyclerView.CustomLoadMoreView
import com.chad.library.adapter.base.module.LoadMoreModule
import com.youth.banner.config.IndicatorConfig
import com.youth.banner.indicator.CircleIndicator
import kotlinx.android.synthetic.main.item_recommend_header.view.*
import java.net.URLDecoder

/**
 * Created by xuzhb on 2021/12/30
 * Desc:推荐
 */
class RecommendFragment : CommonListPlayFragment<RecommendViewModel>() {

    companion object {
        fun newInstance() = RecommendFragment()
    }

    private var hasNoticeDataChange = false

    override fun getAdapter() = CommonHomeAdapter()

    override fun getFirstPageUrl() = UrlConstant.FEED

    override fun initAdapter() {
        mRecyclerView?.layoutManager = GridLayoutManager(context, 2)
        mAdapter = getAdapter()
        if (mAdapter is LoadMoreModule) {  //上拉加载更多
            mAdapter.loadMoreModule.loadMoreView = CustomLoadMoreView(R.layout.view_load_more_for_recommend)
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

    override fun observerListDataChange() {
        viewModel.bannerData.observe(this, {
            initBanner(it)
        })
        super.observerListDataChange()
    }

    override fun initListener() {
        super.initListener()
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val item = mAdapter.getItem(position)
            VideoDetailActivity.start(mContext, item)
        }
    }

    override fun getPlayRangeTop() = ScreenUtil.getScreenHeight() / 2 - SizeUtil.dp2pxInt(300f)

    override fun getPlayRangeBottom() = ScreenUtil.getScreenHeight() / 2 + SizeUtil.dp2pxInt(300f)

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

    private fun initBanner(list: MutableList<ItemBean.ItemX>?) {
        val headerView = LayoutInflater.from(mContext).inflate(R.layout.item_recommend_header, null)
        if (!list.isNullOrEmpty()) {
            headerView.banner.addBannerLifecycleObserver(this)
                .setLoopTime(5000)
                .setAdapter(BannerImageTitleAdapter(list))
                .setOnBannerListener { data, position ->
                    val actionUrl = list[position].data.actionUrl ?: ""
                    val decodeUrl = URLDecoder.decode(actionUrl, "UTF-8")
                    LogUtil.i("WebviewActivityTAG", "decodeUrl:$decodeUrl")
                    val title = decodeUrl.split("title=").last().split("&url").first()
                    val url = decodeUrl.split("url=").last()
                    if (decodeUrl.startsWith("eyepetizer://webview/?title=")) {  //网页
                        WebDetailActivity.start(mContext, title, url)
                    } else {
                        showToast("当前模块暂未开放！")
                    }
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