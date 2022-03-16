package com.android.base

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import com.android.http.Config
import com.android.receiver.NetReceiver
import com.android.universal.R
import com.android.util.*
import com.android.util.uiparse.ParseUtil
import com.android.widget.LoadingLayout
import com.android.widget.TitleBar
import com.android.widget.loadingDialog.LoadingDialog
import java.lang.reflect.ParameterizedType

/**
 * Created by xuzhb on 2021/8/5
 * Desc:基类Activity(MVVM)
 */
abstract class BaseActivity<VB : ViewBinding, VM : BaseViewModel> : AppCompatActivity(), IBaseView,
    SwipeRefreshLayout.OnRefreshListener {

    protected lateinit var binding: VB
    protected lateinit var viewModel: VM

    //加载弹窗
    protected var mLoadingDialog: LoadingDialog? = null

    //通用加载状态布局，需在布局文件中固定id名为loading_layout
    protected var mLoadingLayout: LoadingLayout? = null

    //通用标题栏，需在布局文件中固定id名为title_bar
    protected var mTitleBar: TitleBar? = null

    //通用下拉刷新组件，需在布局文件中固定id名为swipe_refresh_layout
    protected var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    //通用RecyclerView组件，需在布局文件中固定id名为R.id.recycler_view
    protected var mRecyclerView: RecyclerView? = null

    //通用网络异常的布局
    private var mNetErrorFl: FrameLayout? = null
    private var mNetReceiver: NetReceiver? = null

    protected var hasDataLoadedSuccess = false  //是否成功加载过数据，设置这个变量的原因是加载状态布局一般只会在第一次加载时显示，当加载成功过一次就不再显示

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = Color.WHITE  //设置底部虚拟导航栏白色
        initViewBindingAndViewModel()  //获取ViewBinding和ViewModel
        setContentView(binding.root)
        BaseApplication.instance.addActivity(this)
        initBaseView()     //初始化一些通用控件
        initBar()          //初始化状态栏
        initNetReceiver()  //监听网络变化
        initViewModelObserver()  //监听BaseViewMode的数据变化
        handleView(savedInstanceState)  //执行onCreate接下来的逻辑
        initListener()     //所有的事件回调均放在该层，如onClickListener等
        //加载数据
        refreshData()
    }

    //获取ViewBinding和ViewModel，这里通过反射获取
    protected open fun initViewBindingAndViewModel() {
        val superclass = javaClass.genericSuperclass
        val vbClass = (superclass as ParameterizedType).actualTypeArguments[0] as Class<VB>
        val vmClass = (superclass as ParameterizedType).actualTypeArguments[1] as Class<VM>
        val method = vbClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        binding = method.invoke(null, layoutInflater) as VB
        viewModel = ViewModelProvider(this).get(vmClass)
    }

    //初始化一些通用控件，如加载弹窗、加载状态布局、SwipeRefreshLayout、网络错误提示布局
    protected open fun initBaseView() {
        mTitleBar = findViewById(R.id.title_bar)
        mLoadingDialog = LoadingDialog(this, R.style.LoadingDialogStyle)
        //获取布局中的加载状态布局
        mLoadingLayout = findViewById(R.id.loading_layout)
        mLoadingLayout?.setOnFailListener { refreshData() }
        //获取布局中的SwipeRefreshLayout组件，重用BaseActivity的下拉刷新逻辑
        //注意布局中SwipeRefreshLayout的id命名为swipe_refresh_layout，否则mSwipeRefreshLayout为null
        //如果SwipeRefreshLayout里面只包含RecyclerView，可引用<include layout="@layout/layout_list" />
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        //如果当前布局文件不包含id为swipe_refresh_layout的组件则不执行下面的逻辑
        mSwipeRefreshLayout?.let {
            it.setColorSchemeColors(Color.parseColor("#FB7299"))
            it.setProgressBackgroundColorSchemeColor(Color.parseColor("#FFFFFF"))
            it.setOnRefreshListener(this)
        }
        //获取布局中的RecyclerView组件，注意布局中RecyclerView的id命名为recycler_view，否则mRecyclerView为null
        mRecyclerView = findViewById(R.id.recycler_view)
        //在当前布局的合适位置引用<include layout="@layout/layout_net_error" />，则当网络出现错误时会进行相应的提示
        mNetErrorFl = findViewById(R.id.net_error_fl)
    }

    //实现默认的沉浸式状态栏样式，特殊的Activity可以通过重写该方法改变状态栏样式，如颜色等
    protected open fun initBar() {
        if (mTitleBar != null) {  //如果当前布局包含id为title_bar的标题栏控件，以该控件为基准实现沉浸式状态栏
            StatusBarUtil.darkModeAndPadding(this, mTitleBar!!)
            if (isBarBack()) {
                mTitleBar?.setOnLeftIconClickListener {
                    finish()
                }
            }
        } else {  //以ContentView为基准实现沉浸式状态栏，颜色是整个布局的背景色
            val content: ViewGroup = findViewById(android.R.id.content)
            StatusBarUtil.darkModeAndPadding(this, content)
        }
    }

    //点击标题栏左侧图标是否退出Activity，默认true
    protected open fun isBarBack(): Boolean = true

    //监听BaseViewMode的数据变化
    protected open fun initViewModelObserver() {
        //加载状态布局
        viewModel.showLoadLayoutData.observe(this, Observer {
            showLoadingLayout()
        })
        //加载弹窗
        viewModel.showLoadingDialogData.observe(this, Observer {
            showLoadingDialog(it.message, it.cancelable)
        })
        //加载完成的逻辑
        viewModel.loadFinishErrorData.observe(this, Observer {
            loadFinish(it)
        })
    }

    //执行onCreate接下来的逻辑
    abstract fun handleView(savedInstanceState: Bundle?)

    //所有的事件回调均放在该层，如onClickListener等
    abstract fun initListener()

    //加载数据，进入页面时默认就会进行加载，请务必重写refreshData，当加载失败点击重试或者下拉刷新时会调用这个方法
    protected open fun refreshData() {

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetReceiver()
        //销毁加载框
        mLoadingDialog?.dismiss()
        mLoadingDialog = null

        BaseApplication.instance.removeActivity(this)
    }

    //显示加载弹窗
    override fun showLoadingDialog(message: String, cancelable: Boolean) {
        runOnUiThread {
            mLoadingDialog?.show(message, cancelable)
        }
    }

    //显示加载状态布局
    override fun showLoadingLayout() {
        runOnUiThread {
            if (!hasDataLoadedSuccess) {  //加载成功过都不显示加载状态
                mSwipeRefreshLayout?.isRefreshing = true
            }
        }
    }

    //数据加载完成
    override fun loadFinish(isError: Boolean) {
        runOnUiThread {
            if (!isError) {
                hasDataLoadedSuccess = true
            }
            showNetErrorLayout()
            if (isError && !hasDataLoadedSuccess) {  //数据加载失败，且当前页面无数据
                mLoadingLayout?.loadFail()
            } else {
                mLoadingLayout?.loadComplete()
            }
            //取消加载弹窗
            mLoadingDialog?.dismiss()
            //完成下拉刷新动作
            mSwipeRefreshLayout?.isRefreshing = false
        }
    }

    //显示Toast
    override fun showToast(text: CharSequence, isCenter: Boolean, longToast: Boolean) {
        runOnUiThread {
            ToastUtil.showToast(text, isCenter, longToast)
        }
    }

    //跳转到登录界面
    override fun gotoLogin() {
        BaseApplication.instance.finishAllActivities()
        val intent = Intent()
        val action = "${packageName}.login"  //登录页的action
        LogUtil.i("BaseActivity", "LoginActivity action：$action")
        intent.action = action
        startActivity(intent)
    }

    //下拉刷新
    override fun onRefresh() {
        refreshData()              //重新加载数据
        mLoadingDialog?.dismiss()  //下拉时就不显示加载框了
    }

    //网络断开连接提示
    fun showNetErrorLayout() {
        //如果当前布局文件中不包含layout_net_error则netErrorFl为null，此时不执行下面的逻辑
        runOnUiThread {
            mNetErrorFl?.visibility =
                if (NetworkUtil.isConnected(applicationContext)) View.GONE else View.VISIBLE
        }
    }

    //启动指定的Activity
    protected fun startActivity(clazz: Class<*>, extras: Bundle? = null) {
        if (CheckFastClickUtil.isFastClick()) {  //防止快速点击启动两个Activity
            return
        }
        val intent = Intent()
        extras?.let {
            intent.putExtras(it)
        }
        intent.setClass(this, clazz)
        startActivity(intent)
    }

    //启动指定的Activity并接收返回的结果
    protected fun startActivityForResult(
        clazz: Class<*>,
        requestCode: Int,
        extras: Bundle? = null
    ) {
        if (CheckFastClickUtil.isFastClick()) {  //防止快速点击启动两个Activity
            return
        }
        val intent = Intent()
        extras?.let {
            intent.putExtras(it)
        }
        intent.setClass(this, clazz)
        startActivityForResult(intent, requestCode)
    }

    //注册广播动态监听网络变化
    private fun initNetReceiver() {
        //如果不含有layout_net_error，则不注册广播
        if (mNetErrorFl == null) {
            return
        }
        //动态注册，Android 7.0之后取消了静态注册方式
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        mNetReceiver = NetReceiver()
        registerReceiver(mNetReceiver, filter)
        mNetReceiver!!.setOnNetChangeListener {
            showNetErrorLayout()
        }
    }

    //注销广播
    private fun unregisterNetReceiver() {
        if (mNetErrorFl == null) {
            return
        }
        unregisterReceiver(mNetReceiver)
        mNetReceiver = null
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //屏幕顶部中间区域双击获取当前Activity类名，只在debug环境下有效
        if (Config.DEBUG) {
            ParseUtil.showTopActivityInfo(this, ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    //防止AudioManager持有Activity导致的内存泄漏
    override fun getSystemService(name: String): Any? {
        if (Context.AUDIO_SERVICE == name) {
            return applicationContext.getSystemService(name)
        }
        return super.getSystemService(name)
    }

}