package com.android.project.ui.detail

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.TextView
import com.android.base.BaseListFragment
import com.android.project.R
import com.android.project.adapter.VideoReplyAdapter
import com.android.project.databinding.FragmentVideoReplyBinding
import com.android.project.entity.ReplyBean
import com.android.project.server.UrlConstant
import com.android.project.util.FilterUtil
import com.android.util.LogUtil
import com.android.widget.recyclerView.CustomLoadMoreView
import com.chad.library.adapter.base.module.LoadMoreModule
import kotlinx.android.synthetic.main.item_video_reply_header.view.*

/**
 * Created by xuzhb on 2022/2/11
 * Desc:视频详情页-评论
 */
class VideoReplyFragment : BaseListFragment<ReplyBean, FragmentVideoReplyBinding, VideoReplyViewModel>() {

    companion object {
        private const val TAG = "VideoReplyFragment"
        private const val EXTRA_ID = "EXTRA_ID"

        fun newInstance(id: Int) = VideoReplyFragment().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_ID, id)
            }
        }
    }

    private var mId: Int = 0  //视频id
    private var isSortByLikeCount = true  //默认按热度排序

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mId = arguments?.getInt(EXTRA_ID, 0) ?: 0
    }

    override fun initAdapter() {
        mAdapter = getAdapter()
        if (mAdapter is LoadMoreModule) {  //上拉加载更多
            mAdapter.loadMoreModule.loadMoreView = CustomLoadMoreView(R.layout.view_load_more_for_reply)  //增加无数据时的脚布局高度
            mAdapter.loadMoreModule.setOnLoadMoreListener {
                viewModel.loadData(mNextPageUrl, mLoadingLayout != null, mLoadingLayout == null && isFirstLoad())
            }
        }
        mRecyclerView?.adapter = mAdapter
    }

    override fun getAdapter() = VideoReplyAdapter()

    override fun getFirstPageUrl(): String {
        val url = "${UrlConstant.REPLIES}$mId"
        LogUtil.i(TAG, url)
        return url
    }

    override fun handleView(savedInstanceState: Bundle?) {
        mLoadingLayout?.let {
            it.emptySrc = null
            it.emptyDescText = "看看下面-来发评论吧"
            it.findViewById<TextView>(R.id.desc_tv).setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }
    }

    override fun initListener() {
    }

    override fun convertData(response: MutableList<ReplyBean>?): MutableList<ReplyBean>? {
        return FilterUtil.filterReplyList(response)
    }

    override fun showData(nextPageUrl: String, list: MutableList<ReplyBean>?) {
        if (isFirstLoad()) {
            initHeaderView()
        }
        val data = convertData(list)
        sortReplyList(data, isSortByLikeCount)
        LogUtil.i("showData", "conver list size：${data?.size ?: -1}")
        if (isFirstLoad()) {  //首次加载
            mAdapter.setNewInstance(data)
        } else {
            mAdapter.addData(data ?: mutableListOf())
        }
        mNextPageUrl = nextPageUrl
        if (mAdapter is LoadMoreModule) {
            if (list == null || mNextPageUrl.isBlank()) {  //加载到底，没有更多数据
                mAdapter.loadMoreModule.loadMoreEnd()
//                mAdapter.loadMoreModule.isEnableLoadMore = false
            } else {  //完成一页的加载
                mAdapter.loadMoreModule.loadMoreComplete()
//                mAdapter.loadMoreModule.isEnableLoadMore = true
            }
        }
    }

    private fun initHeaderView() {
        val headerView = LayoutInflater.from(mContext).inflate(R.layout.item_video_reply_header, null)
        headerView.title_tv.text = "热门评论"
        headerView.sort_tv.text = "按热度"
        isSortByLikeCount = true
        headerView.sort_tv.setOnClickListener {
            if (isSortByLikeCount) {
                headerView.title_tv.text = "最新评论"
                headerView.sort_tv.text = "按时间"
                isSortByLikeCount = false
            } else {
                headerView.title_tv.text = "热门评论"
                headerView.sort_tv.text = "按热度"
                isSortByLikeCount = true
            }
            sortReplyList(mAdapter.data, isSortByLikeCount)
            mAdapter.notifyDataSetChanged()
        }
        mAdapter.setHeaderView(headerView)
    }

    private fun sortReplyList(list: MutableList<ReplyBean>?, sortByLikeCount: Boolean) {
        if (sortByLikeCount) {
            //按热度排序
            list?.sortWith(Comparator { o1, o2 -> (o2.data?.likeCount ?: 0) - (o1.data?.likeCount ?: 0) })
        } else {
            //按时间排序
            list?.sortWith(Comparator { o1, o2 ->
                val t = (o2.data?.createTime ?: 0) - (o1.data?.createTime ?: 0)
                when {
                    t > 0 -> 1
                    t == 0L -> 0
                    else -> -1
                }
            })
        }
    }

}