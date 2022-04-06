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
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import com.android.base.BaseFragment
import com.android.project.R
import com.android.project.databinding.FragmentFindBinding
import com.android.project.entity.TabBean
import com.android.project.ui.find.TabListFragment
import com.android.project.widget.ScaleTransitionPagerTitleView
import com.android.util.SizeUtil
import com.android.util.StatusBarUtil
import com.android.util.visible
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator

/**
 * Created by xuzhb on 2021/12/30
 * Desc:发现
 */
class FindFragment : BaseFragment<FragmentFindBinding, FindViewModel>() {

    companion object {
        fun newInstance() = FindFragment()
    }

    override fun handleView(savedInstanceState: Bundle?) {
        activity?.let { StatusBarUtil.setPaddingSmart(it, mTitleBar!!) }
    }

    override fun refreshData() {
        viewModel.tabList()
    }

    override fun initViewModelObserver() {
        super.initViewModelObserver()
        viewModel.successData.observe(this, Observer {
            mSwipeRefreshLayout?.isEnabled = false
            initViewPager(it.tabInfo.tabList)
            mTitleBar?.showDividerLine = false
        })
        viewModel.errorData.observe(this, Observer {
            mSwipeRefreshLayout?.isEnabled = true
            mLoadingLayout?.loadFail()
            mTitleBar?.showDividerLine = true
        })
    }

    override fun initListener() {
    }

    private fun initViewPager(list: MutableList<TabBean.Tab>) {
        binding.viewPager.adapter = FragmentPagerAdapter(childFragmentManager, list)
        binding.viewPager.offscreenPageLimit = list.size - 1
        binding.magicIndicator.navigator = CommonNavigator(mContext).apply {
            adapter = object : CommonNavigatorAdapter() {
                override fun getCount(): Int = list.size

                override fun getTitleView(context: Context?, index: Int): IPagerTitleView =
                    ScaleTransitionPagerTitleView(mContext, 0.98f).apply {
                        normalColor = ContextCompat.getColor(mContext, R.color.gray)
                        selectedColor = ContextCompat.getColor(mContext, R.color.colorOnPrimary)
                        text = list[index].name
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
                        mode = LinePagerIndicator.MODE_MATCH_EDGE
                        xOffset = SizeUtil.dp2px(13f)
                        startInterpolator = AccelerateDecelerateInterpolator()
                        endInterpolator = DecelerateInterpolator(2.0f)
                        setColors(ContextCompat.getColor(mContext, R.color.colorOnPrimary))
                    }
            }
        }
        ViewPagerHelper.bind(binding.magicIndicator, binding.viewPager)
        binding.dividerView.visible()
    }

    //这里将fragment的初始化放在adapter里面，以防止内存泄漏
    private inner class FragmentPagerAdapter(fm: FragmentManager, private val mList: MutableList<TabBean.Tab>) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = mList.size

        override fun getItem(position: Int): Fragment = TabListFragment.newInstance(mList[position].apiUrl)

        override fun getItemPosition(`object`: Any) = PagerAdapter.POSITION_NONE
    }

}