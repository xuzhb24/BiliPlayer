package com.android.project.ui.main

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.android.base.CommonBaseFragment
import com.android.project.R
import com.android.project.databinding.FragmentHomeBinding
import com.android.project.ui.home.*
import com.android.project.widget.ScaleTransitionPagerTitleView
import com.android.util.SizeUtil
import com.android.util.StatusBarUtil
import com.shuyu.gsyvideoplayer.GSYVideoManager
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator

/**
 * Created by xuzhb on 2021/8/13
 * Desc:首页
 */
class HomeFragment : CommonBaseFragment<FragmentHomeBinding>() {

    companion object {
        fun newInstance() = HomeFragment()

        private const val TITLE_HOT = "热门"
        private const val TITLE_RECOMMEND = "推荐"
        private const val TITLE_COMMUNITY = "社区"
        private const val TITLE_WEEK = "周榜"
        private const val TITLE_MONTH = "月榜"
        private const val TITLE_TOTAL = "总榜"
    }

    private val mTitleList: MutableList<String> = mutableListOf(
        TITLE_HOT, TITLE_RECOMMEND, TITLE_COMMUNITY,
        TITLE_WEEK, TITLE_MONTH, TITLE_TOTAL
    )
    private var mFragmentPagerAdapter: FragmentPagerAdapter? = null
    private var mNavigatorAdapter: CommonNavigatorAdapter? = null

    override fun handleView(savedInstanceState: Bundle?) {
        activity?.let { StatusBarUtil.setPaddingSmart(it, binding.titleLl) }
        //初始化页面
        initPage()
    }

    //初始化页面
    private fun initPage() {
        mFragmentPagerAdapter = FragmentPagerAdapter(childFragmentManager, mTitleList)
        mNavigatorAdapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int = mTitleList.size

            override fun getTitleView(context: Context?, index: Int): IPagerTitleView =
                ScaleTransitionPagerTitleView(mContext, 0.98f).apply {
                    normalColor = ContextCompat.getColor(mContext, R.color.gray)
                    selectedColor = ContextCompat.getColor(mContext, R.color.colorOnPrimary)
                    text = mTitleList[index]
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, SizeUtil.dp2px(15f))
                    setPadding(
                        SizeUtil.dp2px(13f).toInt(), 0,
                        SizeUtil.dp2px(13f).toInt(), 0
                    )
                    setOnClickListener {
                        binding.viewPager.currentItem = index
                    }
                }

            override fun getIndicator(context: Context?): IPagerIndicator =
                LinePagerIndicator(mContext).apply {
                    mode = LinePagerIndicator.MODE_EXACTLY
                    lineWidth = SizeUtil.dp2px(30f)
                    lineHeight = SizeUtil.dp2px(3f)
//                    roundRadius = SizeUtil.dp2px(2.75f)
                    startInterpolator = AccelerateDecelerateInterpolator()
                    endInterpolator = DecelerateInterpolator(2.0f)
                    setColors(ContextCompat.getColor(mContext, R.color.colorOnPrimary))
                }

        }
        binding.viewPager.adapter = mFragmentPagerAdapter
        binding.viewPager.offscreenPageLimit = mTitleList.size
        binding.magicIndicator.navigator = CommonNavigator(mContext).apply { adapter = mNavigatorAdapter }
        ViewPagerHelper.bind(binding.magicIndicator, binding.viewPager)
        switchPage(1)
    }

    //页面切换
    private fun switchPage(position: Int) {
        if (position >= 0 && position < mTitleList.size) {
            binding.viewPager.currentItem = position
            binding.magicIndicator.onPageSelected(position)
        }
    }

    override fun initListener() {
    }

    //这里将fragment的初始化放在adapter里面，以防止内存泄漏
    private inner class FragmentPagerAdapter(fm: FragmentManager, private val mList: MutableList<String>) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (mList[position]) {
                TITLE_HOT -> HotFragment.newInstance()
                TITLE_RECOMMEND -> RecommendFragment.newInstance()
                TITLE_COMMUNITY -> CommunityFragment.newInstance()
                TITLE_WEEK -> WeekRankFragment.newInstance()
                TITLE_MONTH -> MonthRankFragment.newInstance()
                TITLE_TOTAL -> TotalRankFragment.newInstance()
                else -> throw RuntimeException("Fragment初始化异常")
            }
        }

        override fun getCount(): Int = mList.size

        override fun getItemPosition(`object`: Any) = PagerAdapter.POSITION_NONE

    }

    fun onBackPressed(): Boolean {
        if (activity != null && GSYVideoManager.backFromWindowFull(activity)) {  //全屏播放时退出全屏
            return true
        }
        return false
    }

}