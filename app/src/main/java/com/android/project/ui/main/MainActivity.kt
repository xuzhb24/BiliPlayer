package com.android.project.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.android.base.CommonBaseActivity
import com.android.base.DontSwipeBack
import com.android.project.R
import com.android.project.databinding.ActivityMainBinding
import com.android.util.StatusBarUtil
import com.android.util.gone
import com.android.util.visible
import com.android.widget.SimpleFragmentPagerAdapter
import kotlinx.android.synthetic.main.layout_tab_content.view.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView

/**
 * Created by xuzhb on 2021/8/13
 * Desc:首页
 */
class MainActivity : CommonBaseActivity<ActivityMainBinding>(), DontSwipeBack {

    companion object {
        private const val ADD_POSITION = 2
    }

    override fun initBar() {
        StatusBarUtil.darkMode(this)
    }

    private val mTitleList: MutableList<String> = mutableListOf("首页", "动态", "", "发现", "我的")
    private val mHomeFragment = HomeFragment.newInstance()

    private val mImageButton = intArrayOf(
        R.drawable.selector_bottom_home,
        R.drawable.selector_bottom_trends,
        R.drawable.ic_add,
        R.drawable.selector_bottom_find,
        R.drawable.selector_bottom_mine
    )

    private val mFragmentList: MutableList<Fragment> = mutableListOf(
        mHomeFragment,
        TrendsFragment.newInstance(),
        Fragment(),
        FindFragment.newInstance(),
        MineFragment.newInstance()
    )

    override fun handleView(savedInstanceState: Bundle?) {
        initBottomNavigationBar()
    }

    //初始化底部导航栏
    private fun initBottomNavigationBar() {
        binding.viewPager.isScrollable = false  //禁止左右滑动
        binding.viewPager.adapter = SimpleFragmentPagerAdapter(supportFragmentManager, mFragmentList);
        binding.viewPager.offscreenPageLimit = mFragmentList.size
        binding.magicIndicator.navigator = CommonNavigator(this).apply {
            isAdjustMode = true
            adapter = object : CommonNavigatorAdapter() {

                override fun getTitleView(context: Context?, index: Int): IPagerTitleView =
                    CommonPagerTitleView(this@MainActivity).apply {
                        val tabLayout = LayoutInflater.from(applicationContext).inflate(R.layout.layout_tab_content, null)
                        tabLayout.iv.setImageResource(mImageButton[index])
                        if (index == ADD_POSITION) {  //显示+号
                            tabLayout.tv.gone()
                        } else {
                            tabLayout.tv.visible()
                            tabLayout.tv.text = mTitleList[index]
                        }
                        setContentView(tabLayout)
                        onPagerTitleChangeListener = object : CommonPagerTitleView.OnPagerTitleChangeListener {

                            override fun onDeselected(index: Int, totalCount: Int) {
                                if (index != ADD_POSITION) {
                                    tabLayout.iv.isSelected = false
                                    tabLayout.tv.isSelected = false
                                }
                            }

                            override fun onSelected(index: Int, totalCount: Int) {
                                if (index != ADD_POSITION) {
                                    tabLayout.iv.isSelected = true
                                    tabLayout.tv.isSelected = true
                                }
                            }

                            override fun onLeave(index: Int, totalCount: Int, leavePercent: Float, leftToRight: Boolean) {
                                //设置缩放效果
//                                if (index != ADD_POSITION) {
//                                    tabLayout.iv.scaleX = leavePercent
//                                    tabLayout.iv.scaleY = leavePercent
//                                }
                            }

                            override fun onEnter(index: Int, totalCount: Int, enterPercent: Float, leftToRight: Boolean) {
                                //设置缩放效果
//                                if (index != ADD_POSITION) {
//                                    tabLayout.iv.scaleX = 1.1f * enterPercent
//                                    tabLayout.iv.scaleY = 1.1f * enterPercent
//                                }
                            }

                        }
                        setOnClickListener {
                            if (index != ADD_POSITION) {
                                binding.viewPager.currentItem = index
                            }
                        }
                    }

                override fun getCount(): Int = mTitleList.size

                override fun getIndicator(context: Context?): IPagerIndicator? = null

            }
        }
        ViewPagerHelper.bind(binding.magicIndicator, binding.viewPager)
    }

    override fun initListener() {
    }

    //再按一次退出程序
    private var mLastPressTime: Long = 0

    override fun onBackPressed() {
        if (mHomeFragment.onBackPressed()) {  //在首页全屏播放时先退出全屏
            return
        }
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - mLastPressTime > 2 * 1000) {
            showToast("再按一次退出程序")
        } else {
            super.onBackPressed()
        }
        mLastPressTime = currentTimeMillis
    }

}