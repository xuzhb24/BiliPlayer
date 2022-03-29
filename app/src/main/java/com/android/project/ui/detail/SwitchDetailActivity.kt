package com.android.project.ui.detail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.android.base.BaseApplication
import com.android.base.CommonBaseActivity
import com.android.base.DontSwipeBack
import com.android.project.R
import com.android.project.adapter.SwitchDetailAdapter
import com.android.project.databinding.ActivitySwitchDetailBinding
import com.android.project.dialog.SwitchCommentDialog
import com.android.project.entity.ItemBean
import com.android.project.util.getVideoId
import com.android.project.util.getVideoReplyCount
import com.android.util.*
import com.android.video.util.AutoPlayPageChangeListener
import com.android.widget.popupWindow.CommonPopupWindow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shuyu.gsyvideoplayer.GSYVideoManager

/**
 * Created by xuzhb on 2022/3/19
 * Desc:上下切换详情页
 */
class SwitchDetailActivity : CommonBaseActivity<ActivitySwitchDetailBinding>(), DontSwipeBack {

    companion object {
        private const val EXTRA_CURRENT_ITEM = "EXTRA_CURRENT_ITEM"
        private const val EXTRA_ITEM_LIST = "EXTRA_ITEM_LIST"

        fun start(context: Context, currentItem: ItemBean, itemList: MutableList<ItemBean>) {
            if (CheckFastClickUtil.isFastClick()) {
                return
            }
            CacheUtil(BaseApplication.instance).putString(EXTRA_CURRENT_ITEM, Gson().toJson(currentItem))
            CacheUtil(BaseApplication.instance).putString(EXTRA_ITEM_LIST, Gson().toJson(itemList))
            val intent = Intent(context, SwitchDetailActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var mCurrentItem: ItemBean? = null
    private var mItemList: MutableList<ItemBean>? = null
    private var mCurrentItemPosition = -1

    private var mAdapter = SwitchDetailAdapter()
    private var mOnPageChangeCallback: ViewPager2.OnPageChangeCallback? = null
    private var mCommentPopupWindow: CommonPopupWindow? = null

    override fun initBar() {
        StatusBarUtil.darkMode(this, Color.BLACK, 1f, false)
        StatusBarUtil.setPaddingSmart(this, binding.rootLl)
    }

    override fun handleView(savedInstanceState: Bundle?) {
        window.navigationBarColor = Color.BLACK
        mCurrentItem = Gson().fromJson(CacheUtil(applicationContext).getString(EXTRA_CURRENT_ITEM), ItemBean::class.java)
        mItemList = Gson().fromJson(
            CacheUtil(applicationContext).getString(EXTRA_ITEM_LIST),
            object : TypeToken<MutableList<ItemBean>>() {}.type
        )
        if (mCurrentItem == null || mItemList.isNullOrEmpty()) {
            finish()
            return
        }
        mItemList?.forEachIndexed { index, itemBean ->
            if (mCurrentItem == itemBean) {
                mCurrentItemPosition = index
                return@forEachIndexed
            }
        }
        mAdapter.setNewInstance(mItemList)
        with(binding.viewPager) {
            adapter = mAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 1
            mOnPageChangeCallback = AutoPlayPageChangeListener(this, mCurrentItemPosition, R.id.video_player)
            registerOnPageChangeCallback(mOnPageChangeCallback!!)
            setCurrentItem(mCurrentItemPosition, false)
        }
    }

    override fun initListener() {
        binding.backIv.setOnClickListener {
            finish()
        }
        mAdapter.addChildClickViewIds(R.id.comment_tv)
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            val bean = mAdapter.getItem(position)
            when (view.id) {
                R.id.comment_tv -> {
                    showCommentWindow(bean)
                }
            }
        }
    }

    private fun showCommentWindow(bean: ItemBean) {
        if (CheckFastClickUtil.isFastClick()) {
            return
        }
        SwitchCommentDialog.newInstance(getVideoId(bean), getVideoReplyCount(bean)).show(supportFragmentManager)
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
//        GSYVideoManager.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
        mOnPageChangeCallback?.run { binding.viewPager.unregisterOnPageChangeCallback(this) }
        mOnPageChangeCallback = null
    }

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }

}