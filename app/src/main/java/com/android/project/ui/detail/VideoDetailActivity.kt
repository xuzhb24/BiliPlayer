package com.android.project.ui.detail

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import com.android.base.BaseActivity
import com.android.base.DontSwipeBack
import com.android.project.R
import com.android.project.databinding.ActivityVideoDetailBinding
import com.android.project.entity.ItemBean
import com.android.project.util.*
import com.android.project.widget.ScaleTransitionPagerTitleView
import com.android.util.*
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator

/**
 * Created by xuzhb on 2022/2/7
 * Desc:视频详情页
 */
class VideoDetailActivity : BaseActivity<ActivityVideoDetailBinding, VideoDetailViewModel>(), DontSwipeBack {

    companion object {
        private const val TAG = "VideoDetailActivityTAG"
        private const val EXTRA_ITEM_BEAN = "EXTRA_ITEM_BEAN"

        fun start(context: Context, item: ItemBean) {
            if (CheckFastClickUtil.isFastClick()) {
                return
            }
            val intent = Intent(context, VideoDetailActivity::class.java)
            intent.putExtra(EXTRA_ITEM_BEAN, Gson().toJson(item))
            context.startActivity(intent)
        }
    }

    private var mId: Int? = null             //当前视频id
    private var mItemBean: ItemBean? = null  //当前视频
    private var mReleatedList: MutableList<ItemBean>? = null  //推荐视频列表
    private val mTitleList: MutableList<String> = mutableListOf()
    private var mFragmentPagerAdapter: FragmentStatePagerAdapter? = null
    private var mNavigatorAdapter: CommonNavigatorAdapter? = null

    private var isPrepared = false   //是否加载过视频
    private var isOnPause = false    //是否调用onPause
    private var isClickStop = false  //是否点击了停止按钮
    private var mOrientationUtils: OrientationUtils? = null

    override fun initBar() {
        StatusBarUtil.darkMode(this, Color.BLACK, 1f, false)
        StatusBarUtil.setPaddingSmart(this, binding.coordinatorLayout)
    }

    override fun handleView(savedInstanceState: Bundle?) {
        mLoadingLayout?.let {
            it.getRootLayout().gravity = Gravity.CENTER_HORIZONTAL
            LayoutParamsUtil.setPaddingTop(it.getRootLayout(), SizeUtil.dp2pxInt(50f))
        }
        mItemBean = Gson().fromJson(intent.getStringExtra(EXTRA_ITEM_BEAN) ?: "{}", ItemBean::class.java)
        mId = getVideoId(mItemBean)
        initPlayer(mItemBean)
    }

    //初始化播放器
    private fun initPlayer(itemBean: ItemBean?) {
        if (itemBean == null) {
            return
        }
        mOrientationUtils = OrientationUtils(this, binding.videoPlayer)
        mOrientationUtils?.isEnable = false
        with(binding.videoPlayer) {
            titleTextView.gone()
            backButton.gone()
            fullscreenButton.setOnClickListener {
                mOrientationUtils?.resolveByClick()
                startWindowFullscreen(this@VideoDetailActivity, true, true)
            }
        }
        binding.videoPlayer.loadCoverImage(getVideoCover(itemBean), R.drawable.ic_place_holder)
        GSYVideoOptionBuilder()
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setAutoFullWithSize(false)
            .setShowFullAnimation(false)
            .setNeedLockFull(true)
            .setUrl(getVideoPlayUrl(itemBean))
            .setCacheWithPlay(false)
            .setVideoTitle(getVideoTitle(itemBean))
            .setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onPrepared(url: String?, vararg objects: Any?) {
                    mOrientationUtils?.isEnable = binding.videoPlayer.isRotateWithSystem
                    isPrepared = true
                    setPlayerState(false)
                }

                override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                    mOrientationUtils?.backToProtVideo()
                }

                override fun onClickStartIcon(url: String?, vararg objects: Any?) {
                    super.onClickStartIcon(url, *objects)
                    setPlayerState(false)
                }

                override fun onClickStop(url: String?, vararg objects: Any?) {
                    super.onClickStop(url, *objects)
                    setPlayerState(true)
                }

                override fun onClickStopFullscreen(url: String?, vararg objects: Any?) {
                    super.onClickStopFullscreen(url, *objects)
                    setPlayerState(true)
                }

                override fun onClickResume(url: String?, vararg objects: Any?) {
                    super.onClickResume(url, *objects)
                    setPlayerState(false)
                }

                override fun onClickResumeFullscreen(url: String?, vararg objects: Any?) {
                    super.onClickResumeFullscreen(url, *objects)
                    setPlayerState(false)
                }
            }).setLockClickListener { view, lock ->
                mOrientationUtils?.isEnable = !lock
            }.build(binding.videoPlayer)
        binding.videoPlayer.startPlayLogic()
    }

    override fun refreshData() {
        mId = getVideoId(mItemBean)
        viewModel.releatedVideo(mId.toString())  //获取推荐视频列表
    }

    override fun initViewModelObserver() {
        super.initViewModelObserver()
        viewModel.successData.observe(this, Observer {
            mLoadingLayout?.loadComplete()
            mReleatedList = FilterUtil.filterVideoList(it.itemList)
            LogUtil.i(TAG, "current video:${Gson().toJson(mItemBean)}")
            LogUtil.i(TAG, "releted list size:${mReleatedList?.size} first title:${getVideoTitle(mReleatedList?.takeIf { it.isNotEmpty() }?.get(0))}")
            //更新绑定的当前视频信息和推荐视频列表
            binding.videoPlayer.setVideoInfo(
                ConvertEntityUtil.item2VideoBean(mItemBean),
                ConvertEntityUtil.item2VideoBeanList(mReleatedList)
            )
            //保存推荐视频列表到本地缓存，因为releatedList的数据量可能会很大导致无法通过intent传递
            CacheUtil(applicationContext).putString(Constant.RELEATED_VIDEO_LIST, Gson().toJson(mReleatedList))
            mTitleList.clear()
            mTitleList.add("简介")
            mTitleList.add("评论 ${formatCountStr(getVideoReplyCount(mItemBean), "")}")
            if (binding.viewPager.childCount == 0) {  //首次初始化
                mFragmentPagerAdapter = object : FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

                    override fun getCount(): Int = 2

                    override fun getItem(position: Int): Fragment {
                        return when (position) {
                            0 -> VideoIntroduceFragment.newInstance(mItemBean)
                            1 -> VideoReplyFragment.newInstance(getVideoId(mItemBean))
                            else -> throw RuntimeException("初始化异常")
                        }
                    }

                    //使用FragmentStatePagerAdapter和设置ItemPosition为PagerAdapter.POSITION_NONE才会刷新页面
                    override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE

                }
                mNavigatorAdapter = object : CommonNavigatorAdapter() {
                    override fun getCount() = mTitleList.size

                    override fun getTitleView(context: Context?, index: Int): IPagerTitleView =
                        ScaleTransitionPagerTitleView(this@VideoDetailActivity, 1f).apply {
                            normalColor = ContextCompat.getColor(applicationContext, R.color.gray)
                            selectedColor = ContextCompat.getColor(applicationContext, R.color.colorOnPrimary)
                            text = mTitleList[index]
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, SizeUtil.dp2px(13f))
                            setPadding(
                                SizeUtil.dp2px(25f).toInt(), 0,
                                SizeUtil.dp2px(25f).toInt(), 0
                            )
                            setOnClickListener {
                                binding.viewPager.currentItem = index
                            }
                        }

                    override fun getIndicator(context: Context?): IPagerIndicator =
                        LinePagerIndicator(this@VideoDetailActivity).apply {
                            mode = LinePagerIndicator.MODE_EXACTLY
                            lineWidth = SizeUtil.dp2px(30f)
                            lineHeight = SizeUtil.dp2px(1.5f)
//                            roundRadius = SizeUtil.dp2px(2.75f)
                            startInterpolator = AccelerateDecelerateInterpolator()
                            endInterpolator = DecelerateInterpolator(2.0f)
                            setColors(ContextCompat.getColor(applicationContext, R.color.colorOnPrimary))
                        }
                }
                binding.viewPager.adapter = mFragmentPagerAdapter
                binding.viewPager.offscreenPageLimit = mTitleList.size
                binding.magicIndicator.navigator = CommonNavigator(this).apply { adapter = mNavigatorAdapter }
                ViewPagerHelper.bind(binding.magicIndicator, binding.viewPager)
            } else {
                //刷新页面
                mFragmentPagerAdapter?.notifyDataSetChanged()
                mNavigatorAdapter?.notifyDataSetChanged()
                binding.viewPager.currentItem = 0
                binding.magicIndicator.onPageSelected(0)
            }
        })
        viewModel.errorData.observe(this, Observer {
            mLoadingLayout?.loadFail()
        })
    }

    override fun initListener() {
        mLoadingLayout?.setOnFailListener {
            initPlayer(mItemBean)
            refreshData()
        }
        with(binding.videoPlayer) {
            //网络出错后重新加载
            setOnChildClickListener(R.id.reload_tv) {
                initPlayer(mItemBean)
                refreshData()
            }
            //退出页面
            setOnChildClickListener(R.id.quit_iv) {
                finish()
            }
            //播放下一个视频
            setOnVideoChangeListener { id ->
                getItemBeanById(id)?.let {
                    mItemBean = it
                    refreshData()
                }
            }
        }
        binding.appBarLayout.addOnOffsetChangedListener(object : OnAppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                if (state == State.COLLAPSED) {
                    binding.titleFl.visible()
                } else {
                    binding.titleFl.gone()
                }
            }
        })
        binding.backIv.setOnClickListener {
            finish()
        }
        binding.playTv.setOnClickListener {
            binding.appBarLayout.setExpanded(true)
            postDelayed(300) { binding.videoPlayer.clickStart() }
            setPlayerState(false)
        }
    }

    private fun getItemBeanById(id: Int?): ItemBean? {
        if (id == null) {
            return null
        }
        if (getVideoId(mItemBean) == id) {
            return mItemBean
        }
        mReleatedList?.forEach {
            if (getVideoId(it) == id) {
                return it
            }
        }
        return null
    }

    override fun onPause() {
        binding.videoPlayer.currentPlayer.onVideoPause()
        super.onPause()
        isOnPause = true
    }

    override fun onResume() {
        //这里要分情况，如果是跳转另一个播放页，onVideoResume是不会生效的，
        //如果是熄屏后打开或者跳转其他非播放页面，返回是可以恢复播放的，
        //这是因为只有一个播放器内核，在其他播放页的状态改变会影响到当前页面的播放器状态
        //https://github.com/CarGuo/GSYVideoPlayer/issues/2923
        if (!isClickStop) {
            binding.videoPlayer.currentPlayer.onVideoResume()
        }
        super.onResume()
        isOnPause = false
    }

    override fun onBackPressed() {
        mOrientationUtils?.backToProtVideo()
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        binding.videoPlayer.isBackPressed = true
        super.onBackPressed()
    }

    override fun onDestroy() {
        binding.videoPlayer.release()
        binding.videoPlayer.setVideoAllCallBack(null)
        isPrepared = false
        mOrientationUtils?.releaseListener()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isPrepared && !isOnPause) {
            binding.videoPlayer.onConfigurationChanged(this, newConfig, mOrientationUtils, true, true)
        }
    }

    //防止AudioManager持有Activity导致的内存泄漏
    override fun getSystemService(name: String): Any? {
        if (Context.AUDIO_SERVICE == name) {
            return applicationContext.getSystemService(name)
        }
        return super.getSystemService(name)
    }

    //设置播放器状态
    private fun setPlayerState(clickStop: Boolean) {
        isClickStop = clickStop
        enableAppBarScroll(binding.appBarLayout, clickStop)  //暂停时可以折叠，播放中不可以
    }

}