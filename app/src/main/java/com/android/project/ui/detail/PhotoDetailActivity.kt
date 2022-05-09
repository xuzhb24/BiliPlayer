package com.android.project.ui.detail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.android.base.CommonBaseActivity
import com.android.project.adapter.GridPhotoAdapter
import com.android.project.databinding.ActivityPhotoDetailBinding
import com.android.project.entity.ItemBean
import com.android.project.util.*
import com.android.util.CheckFastClickUtil
import com.android.util.SizeUtil
import com.android.widget.SimpleFragmentPagerAdapter
import com.google.gson.Gson
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView

/**
 * Created by xuzhb on 2022/4/18
 * Desc:图片详情页
 */
class PhotoDetailActivity : CommonBaseActivity<ActivityPhotoDetailBinding>() {

    companion object {
        private const val EXTRA_ITEM_BEAN = "EXTRA_ITEM_BEAN"

        fun start(context: Context, item: ItemBean) {
            if (CheckFastClickUtil.isFastClick()) {
                return
            }
            val intent = Intent(context, PhotoDetailActivity::class.java)
            intent.putExtra(EXTRA_ITEM_BEAN, Gson().toJson(item))
            context.startActivity(intent)
        }
    }

    private var mItemBean: ItemBean? = null  //当前稿件
    private var mImageUrlList: MutableList<String>? = null  //图片列表

    override fun handleView(savedInstanceState: Bundle?) {
        mItemBean = Gson().fromJson(intent.getStringExtra(EXTRA_ITEM_BEAN) ?: "{}", ItemBean::class.java)
        mImageUrlList = mItemBean?.data?.content?.data?.urls ?: mutableListOf()
        initData(mItemBean, mImageUrlList)
    }

    private fun initData(item: ItemBean?, imageUrlList: MutableList<String>?) {
        if (item == null || imageUrlList == null) {
            return
        }
        val owner = item.data.content?.data?.owner
        showCircleImageByCenterCrop(binding.headIv, owner?.avatar ?: "")
        binding.nameTv.text = owner?.nickname ?: ""
        binding.descTv.text = if (owner?.description.isNullOrBlank()) "ta暂时没有简介！" else owner?.description
        binding.contentTv.text = getVideoDescription(item)
        val spanCount = when (imageUrlList.size) {
            1 -> 1
            4 -> 2
            else -> 3
        }
        binding.photoRv.layoutManager = GridLayoutManager(this@PhotoDetailActivity, spanCount)
        binding.photoRv.adapter = GridPhotoAdapter(imageUrlList.size == 1).apply {
            setNewInstance(imageUrlList)
            setOnItemClickListener { adapter, view, position ->
                PhotoViewActivity.start(this@PhotoDetailActivity, item, position)
            }
        }
        val titleList: MutableList<String> = mutableListOf("转发 0", "评论 ${getVideoReplyCount(item)}", "点赞 0")
        val fragmentList: MutableList<Fragment> = mutableListOf(
            PhotoRelayFragment.newInstance(),
            PhotoReplyFragment.newInstance(getVideoId(item)),
            PhotoPraiseFragment.newInstance()
        )
        binding.viewPager.adapter = SimpleFragmentPagerAdapter(supportFragmentManager, fragmentList)
        binding.viewPager.offscreenPageLimit = fragmentList.size - 1
        binding.magicIndicator.navigator = CommonNavigator(this@PhotoDetailActivity).apply {
            isAdjustMode = true
            adapter = object : CommonNavigatorAdapter() {
                override fun getCount(): Int = titleList.size

                override fun getTitleView(context: Context?, index: Int): IPagerTitleView =
                    ColorTransitionPagerTitleView(this@PhotoDetailActivity).apply {
                        normalColor = Color.parseColor("#757575")
                        selectedColor = Color.parseColor("#FB7299")
                        text = titleList[index]
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, SizeUtil.dp2px(15f))
                        setOnClickListener {
                            binding.viewPager.currentItem = index
                        }
                    }

                override fun getIndicator(context: Context?): IPagerIndicator =
                    LinePagerIndicator(this@PhotoDetailActivity).apply {
                        mode = LinePagerIndicator.MODE_EXACTLY
                        lineWidth = SizeUtil.dp2px(36f)
                        lineHeight = SizeUtil.dp2px(2f)
//                            roundRadius = SizeUtil.dp2px(2.75f)
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
        binding.backIv.setOnClickListener {
            finish()
        }
    }
}