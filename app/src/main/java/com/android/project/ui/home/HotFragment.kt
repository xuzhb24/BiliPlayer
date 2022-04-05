package com.android.project.ui.home

import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.android.project.adapter.CommonHomeAdapter
import com.android.project.entity.ItemBean
import com.android.project.server.UrlConstant
import com.android.project.ui.common.CommonListPlayFragment
import com.android.project.ui.common.CommonListViewModel
import com.android.project.ui.detail.VideoDetailActivity
import com.android.project.util.Constant
import com.android.util.LayoutParamsUtil
import com.android.util.ScreenUtil
import com.android.util.SizeUtil

/**
 * Created by xuzhb on 2021/12/30
 * Desc:热门
 */
class HotFragment : CommonListPlayFragment<CommonListViewModel>() {

    companion object {
        fun newInstance() = HotFragment()
    }

    override fun getAdapter() = CommonHomeAdapter()

    override fun getFirstPageUrl() = UrlConstant.ALLREC

    override fun initAdapter() {
        mRecyclerView?.layoutManager = GridLayoutManager(context, 2)
        super.initAdapter()
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
        super.initListener()
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val item = mAdapter.getItem(position)
            if (item.type != Constant.TYPE_SQUARE_CARD && item.type != Constant.TYPE_TEXT_CARD) {
                VideoDetailActivity.start(mContext, item)
            }
        }
    }

    override fun getPlayRangeTop() = ScreenUtil.getScreenHeight() / 2 - SizeUtil.dp2pxInt(300f)

    override fun getPlayRangeBottom() = ScreenUtil.getScreenHeight() / 2 + SizeUtil.dp2pxInt(300f)

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

}