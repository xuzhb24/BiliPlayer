package com.android.base

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import com.android.http.Config
import com.android.universal.databinding.ActivityWebviewBinding
import com.android.util.KeyboardUtil
import com.android.util.LogUtil
import com.android.util.StatusBarUtil
import com.android.widget.picGetterDialog.OnPicGetterListener
import com.android.widget.picGetterDialog.PicGetterDialog
import java.io.File

/**
 * Created by xuzhb on 2021/8/13
 * Desc:H5 Activity基类
 */
open class WebviewActivity : CommonBaseActivity<ActivityWebviewBinding>() {

    companion object {
        private const val TAG = "WebviewActivity"
        private const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val EXTRA_URL = "EXTRA_URL"
        private const val EXTRA_IS_TITLE_FIXED = "EXTRA_IS_TITLE_FIXED"

        fun start(context: Context, title: String, url: String, isTitleFixed: Boolean = false) {
            val intent = Intent()
            intent.setClass(context, WebviewActivity::class.java)
            intent.putExtra(EXTRA_TITLE, title)
            intent.putExtra(EXTRA_URL, url)
            intent.putExtra(EXTRA_IS_TITLE_FIXED, isTitleFixed)
            context.startActivity(intent)
        }
    }

    private var mTitle: String = ""
    private var mUrl: String = ""
    private var isTitleFixed = false  //传进来的标题是否固定不变

    private var mUploadMsg: ValueCallback<Uri>? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null

    protected var mWebView: WebView? = null
    private var hasReceivedError = false   //是否加载失败

    override fun initViewBindingAndViewModel() {
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get((BaseViewModel())::class.java)
    }

    override fun initBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)  //保证WebView的输入框不被软键盘遮挡
        mTitleBar?.let {
            StatusBarUtil.darkModeAndPadding(this, it)
            it.setOnLeftIconClickListener {
                goPageBack()
            }
            if (Config.DEBUG) {  //长按复制网页地址
                it.setOnLongClickListener {
                    val url = mWebView?.url.toString()
                    KeyboardUtil.copyToClipboard(applicationContext, url)
                    showToast("${url}\n已复制到剪切板！")
                    true
                }
            }
        }
    }

    //返回操作
    protected open fun goPageBack() {
        if (mWebView != null && mWebView!!.canGoBack()) {
            mWebView!!.goBack()
        } else {
            finish()
        }
    }

    override fun handleView(savedInstanceState: Bundle?) {
        mTitle = intent.getStringExtra(EXTRA_TITLE)
        mUrl = intent.getStringExtra(EXTRA_URL)
        isTitleFixed = intent.getBooleanExtra(EXTRA_IS_TITLE_FIXED, false)
        mTitleBar?.titleText = mTitle  //设置标题
        createWebView()        //创建WebView
        initWebView()          //设置WebView属性
        initWebViewClient()    //设置WebViewClient
        initWebChromeClient()  //设置SetWebChromeClient
    }

    override fun refreshData() {
        if (!TextUtils.isEmpty(mUrl)) {
//            showLoadingDialog()  //显示加载框
            showLoadingLayout()
            mWebView!!.loadUrl(mUrl)  //加载网页
        }
    }

    //创建WebView
    private fun createWebView() {
        mWebView = WebView(applicationContext)
        with(mWebView!!) {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            overScrollMode = WebView.OVER_SCROLL_NEVER  //取消滑动到边缘时的波纹效果
            isVerticalScrollBarEnabled = false    //不显示垂直滚动条
            isHorizontalScrollBarEnabled = false  //不显示水平滚动条
        }
        binding.webLl.addView(mWebView)
    }

    //可以通过重写以下方法重新初始化WebView
    protected open fun initWebView() {
        mWebView?.let {
            with(it) {
                settings.javaScriptEnabled = true  //支持javascript
                //解决H5页面中的图片不显示的问题
                //安卓5.0之后，WebView默认不允许加载http与https混合内容，需要设置webview允许其加载混合网络协议内容
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                settings.textZoom = 100            //防止系统字体改变导致H5页面排版错乱
                settings.domStorageEnabled = true  //支持html5的DomStorage
                settings.cacheMode = WebSettings.LOAD_DEFAULT  //设置缓存模式，根据cache-control决定是否从网络上取数据
                //支持缩放
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false  //隐藏缩放工具
                settings.useWideViewPort = true       //支持meta标签的viewport属性
                //自适应屏幕
                settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
                settings.loadWithOverviewMode = true
//                addJavascriptInterface(JavaScriptMethods(), "androidjs")  //js调用Android的方法
                requestFocus()
            }
        }
    }

    //可以通过重写以下方法改变WebView的setWebViewClient行为
    protected open fun initWebViewClient() {
        mWebView?.let {
            it.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    LogUtil.i(TAG, "shouldOverrideUrlLoading,url:$url")
                    url?.let {
                        if (!it.equals("about:blank", ignoreCase = true)) {
                            if (it.startsWith("http://") or it.startsWith("https://")) {
                                view?.loadUrl(url)
                            }
                        }
                    }
                    return true
                }

                //页面开始加载时调用
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    LogUtil.i(TAG, "onPageStarted,url:$url")
                    hasReceivedError = false
                }

                //加载页面服务器出现错误时调用
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    LogUtil.i(TAG, "onReceivedError,error:$error")
                    hasReceivedError = true
                }

                //页面加载结束时调用
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    LogUtil.i(TAG, "onPageFinished,url:$url")
                    loadFinish(hasReceivedError)
                }

            }
        }
    }

    //可以通过重写以下方法改变WebView的SetWebChromeClient行为
    protected open fun initWebChromeClient() {
        mWebView?.let {
            it.webChromeClient = object : WebChromeClient() {

                //Android 3.0以下
                fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
                    openFileChooser(uploadMsg, "")
                }

                //Android 3.0 - 4.0
                fun openFileChooser(
                    uploadMsg: ValueCallback<Uri>,
                    acceptType: String
                ) {
                    openFileChooser(uploadMsg, acceptType, "")
                }

                //Android 4.0 - 5.0
                fun openFileChooser(
                    uploadMsg: ValueCallback<Uri>,
                    acceptType: String, capture: String
                ) {
                    mUploadMsg = uploadMsg
                    showImageChooseDialog()
                }

                //Android 5.0以上，包括5.0
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    mFilePathCallback = filePathCallback
                    showImageChooseDialog()
                    return true
                }

                // 配置权限（同样在WebChromeClient中实现）
                override fun onGeolocationPermissionsShowPrompt(
                    origin: String,
                    callback: GeolocationPermissions.Callback
                ) {
                    callback.invoke(origin, true, false)
                    super.onGeolocationPermissionsShowPrompt(origin, callback)
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    LogUtil.i(TAG, "newProgress：${newProgress}")
                }

                //获取网页标题
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    LogUtil.i(TAG, "title：$title，isTitleFixed：$isTitleFixed")
                    if (!isTitleFixed) {
                        mTitleBar?.titleText = title
                    }
                }
            }
        }
    }

    //拍照或从相册选取照片
    private fun showImageChooseDialog() {
        val dialog = PicGetterDialog()
        dialog.setOnPicGetterListener(object : OnPicGetterListener {

            override fun onSuccess(bitmap: Bitmap?, picPath: String?) {
                if (TextUtils.isEmpty(picPath)) {
                    if (mFilePathCallback != null) {
                        mFilePathCallback!!.onReceiveValue(arrayOf())
                        mFilePathCallback = null
                    } else if (mUploadMsg != null) {
                        mUploadMsg!!.onReceiveValue(null)
                        mUploadMsg = null
                    }
                } else {
                    val result = Uri.fromFile(File(picPath))
                    if (mFilePathCallback != null) {
                        mFilePathCallback!!.onReceiveValue(arrayOf(result))
                        mFilePathCallback = null
                    } else {
                        mUploadMsg!!.onReceiveValue(result)
                        mUploadMsg = null
                    }
                }
            }

            override fun onFailure(errorMsg: String?) {
                dialog.dismiss()
                showToast(errorMsg ?: "发生异常了")
                releaseUploadMessage()
            }

            override fun onCancel() {
                releaseUploadMessage()
            }

        })
        dialog.show(supportFragmentManager)
    }

    private fun releaseUploadMessage() {
        if (mFilePathCallback != null) {
            mFilePathCallback!!.onReceiveValue(null)
            mFilePathCallback = null
        }
        if (mUploadMsg != null) {
            mUploadMsg!!.onReceiveValue(null)
            mUploadMsg = null
        }
    }

    override fun initListener() {
    }

    override fun onDestroy() {
        destroyWebView()
        super.onDestroy()
    }

    //销毁WebView
    private fun destroyWebView() {
        mWebView?.let {
            with(it) {
                //加载null的内容
                loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
                //清除历史记录
                clearHistory()
                //先移除WebView再销毁WebView
                (parent as ViewGroup).removeView(it)
                it.destroy()
            }
            mWebView = null
        }
    }

    //back键控制网页后退
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK && mWebView != null && mWebView!!.canGoBack()) {
                mWebView!!.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    //js调用Android的方法
//    inner class JavaScriptMethods {
//        @JavascriptInterface
//        fun toHome() {
//            startActivity(MainActivity::class.java)
//        }
//    }

}