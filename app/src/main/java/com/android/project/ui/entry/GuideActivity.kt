package com.android.project.ui.entry

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.viewpager.widget.ViewPager
import com.android.base.CommonBaseActivity
import com.android.project.R
import com.android.project.databinding.ActivityGuideBinding
import com.android.project.func.UserInfo
import com.android.project.ui.main.MainActivity
import com.android.util.SizeUtil
import com.android.widget.SimpleViewAdapter

/**
 * Created by xuzhb on 2021/8/13
 * Desc:引导页
 */
class GuideActivity : CommonBaseActivity<ActivityGuideBinding>() {

    private val mLayoutIds = intArrayOf(
        R.layout.layout_guide_one,
        R.layout.layout_guide_two,
        R.layout.layout_guide_three
    )

    override fun handleView(savedInstanceState: Bundle?) {
        val viewList: MutableList<View> = mutableListOf()
        setIndicator(0)
        for (i in mLayoutIds.indices) {
            viewList.add(layoutInflater.inflate(mLayoutIds[i], null))
        }
        binding.guideVp.adapter = SimpleViewAdapter(viewList)
        binding.guideVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                setIndicator(position)
                if (position == mLayoutIds.size - 1) {
                    binding.useBtn.visibility = View.VISIBLE
                } else {
                    binding.useBtn.visibility = View.GONE
                }
            }
        })
    }

    override fun initListener() {
        binding.useBtn.setOnClickListener {
            UserInfo.instance.isFirstUse = false
            startActivity(MainActivity::class.java)
            finish()
        }
    }

    private fun setIndicator(position: Int) {
        binding.guideRg.removeAllViews()
        for (i in mLayoutIds.indices) {
            val radioButton = RadioButton(this)
            radioButton.setButtonDrawable(R.drawable.selector_guide_indicator)
            val params = RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.leftMargin = SizeUtil.dp2pxInt(3f)
            params.rightMargin = SizeUtil.dp2pxInt(3f)
            if (i == position) {
                params.width = SizeUtil.dp2pxInt(12f)
                params.height = SizeUtil.dp2pxInt(6f)
                radioButton.isChecked = true
            } else {
                params.width = SizeUtil.dp2pxInt(6f)
                params.height = SizeUtil.dp2pxInt(6f)
                radioButton.isChecked = false
            }
            radioButton.layoutParams = params
            binding.guideRg.addView(radioButton, params)
        }
    }

}