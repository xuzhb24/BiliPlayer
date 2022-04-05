package com.android.project.ui.main

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.android.base.CommonBaseFragment
import com.android.project.databinding.FragmentTrendsBinding
import com.android.project.ui.trends.AllTrendsFragment
import com.android.project.ui.trends.RecommendTrendsFragment
import com.android.project.ui.trends.VideoTrendsFragment
import com.android.project.widget.ScaleTransitionPagerTitleView
import com.android.util.SizeUtil
import com.android.util.StatusBarUtil
import com.android.widget.SimpleFragmentPagerAdapter
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator

/**
 * Created by xuzhb on 2021/8/13
 * Desc:动态
 */
class TrendsFragment : CommonBaseFragment<FragmentTrendsBinding>() {

    companion object {
        fun newInstance() = TrendsFragment()
    }

    private val mTitleList: MutableList<String> = mutableListOf("视频", "综合", "推荐")
    private val mFragmentList: MutableList<Fragment> = mutableListOf(
        VideoTrendsFragment.newInstance(),
        AllTrendsFragment.newInstance(),
        RecommendTrendsFragment.newInstance()
    )

    override fun handleView(savedInstanceState: Bundle?) {
        activity?.let { StatusBarUtil.setPaddingSmart(it, binding.rootLl) }
        binding.viewPager.adapter = SimpleFragmentPagerAdapter(childFragmentManager, mFragmentList)
        binding.viewPager.offscreenPageLimit = mFragmentList.size - 1
        binding.magicIndicator.navigator = CommonNavigator(mContext).apply {
            adapter = object : CommonNavigatorAdapter() {
                override fun getCount(): Int = mTitleList.size

                override fun getTitleView(context: Context?, index: Int): IPagerTitleView =
                    ScaleTransitionPagerTitleView(mContext, 0.8f).apply {
                        normalColor = Color.parseColor("#757575")
                        selectedColor = Color.parseColor("#FB7299")
                        text = mTitleList[index]
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, SizeUtil.dp2px(18f))
                        setPadding(
                            SizeUtil.dp2px(20f).toInt(), 0, SizeUtil.dp2px(20f).toInt(), 0
                        )
                        setOnClickListener {
                            binding.viewPager.currentItem = index
                        }
                    }

                override fun getIndicator(context: Context?): IPagerIndicator =
                    LinePagerIndicator(mContext).apply {
                        mode = LinePagerIndicator.MODE_EXACTLY
                        lineWidth = SizeUtil.dp2px(36f)
                        lineHeight = SizeUtil.dp2px(2.5f)
//                        roundRadius = SizeUtil.dp2px(2.75f)
                        startInterpolator = AccelerateDecelerateInterpolator()
                        endInterpolator = DecelerateInterpolator(2.0f)
                        setColors(Color.parseColor("#FB7299"))
                    }
            }
        }
        binding.magicIndicator.onPageSelected(1)
        binding.viewPager.currentItem = 1
        ViewPagerHelper.bind(binding.magicIndicator, binding.viewPager)
    }

    override fun initListener() {
    }

}