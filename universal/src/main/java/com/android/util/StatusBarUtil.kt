package com.android.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import java.util.regex.Pattern

/**
 * Created by xuzhb on 2019/7/19
 * Desc:实现沉浸式状态栏，只支持4.4（API 19）以上，Android 4.4才支持状态栏，6.0才支持修改状态栏字体和图标颜色
 */
object StatusBarUtil {

    private const val DEFAULT_COLOR = Color.BLACK
    private const val DEFAULT_ALPHA = 0f  //默认透明度为0，0：完全透明 1：完全不透明
    private const val MIN_API = 19

    //判断是否是Flyme4（魅族4）以上
    private val isFlyme4Later: Boolean
        get() = (Build.FINGERPRINT.contains("Flyme_OS_4")
                || Build.VERSION.INCREMENTAL.contains("Flyme_OS_4")
                || Pattern.compile("Flyme OS [4|5]", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find())

    //判断是否是MIUI6（小米6）以上
    private val isMIUI6Later: Boolean
        get() {
            return try {
                val clazz = Class.forName("android.os.SystemProperties")
                val method = clazz.getMethod("get", String::class.java)
                var versionName = method.invoke(null, "ro.miui.ui.version.name") as String
                versionName = versionName.replace("[vV]".toRegex(), "")
                val version = Integer.parseInt(versionName)
                version >= 6
            } catch (e: Exception) {
                false
            }
        }

    //设置状态栏样式和padding
    fun darkModeAndPadding(
        activity: Activity,
        view: View,
        color: Int = DEFAULT_COLOR,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float = DEFAULT_ALPHA,
        dark: Boolean = true
    ) {
        darkMode(activity, color, alpha, dark)
        setPadding(activity.applicationContext, view)
    }

    /**
     * 设置状态栏样式
     * @param activity：对应的Activity
     * @param color：状态栏颜色
     * @param alpha：状态栏颜色的透明度
     * @param dark：是否设置状态栏字体及图标颜色为黑色，true为黑色，false为白色，目前只支持MIUI6以上、Flyme4以上以及Android M以上
     * @return
     */
    fun darkMode(
        activity: Activity,
        color: Int = DEFAULT_COLOR,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float = DEFAULT_ALPHA,
        dark: Boolean = true
    ) {
        darkMode(activity.window, color, alpha, dark)
    }

    /**
     * 设置状态栏样式
     * @param window：对应的窗口window
     * @param color：状态栏颜色
     * @param alpha：状态栏颜色的透明度
     * @param dark：是否设置状态栏字体及图标颜色为黑色，true为黑色，false为白色，目前只支持MIUI6以上、Flyme4以上以及Android M以上
     * @return
     */
    fun darkMode(
        window: Window,
        color: Int = DEFAULT_COLOR,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float = DEFAULT_ALPHA,
        dark: Boolean = true
    ) {
        when {
            isFlyme4Later -> {
                darkModeForFlyme4(window, dark)
                immersive(window, color, alpha)
            }
            isMIUI6Later -> {
                darkModeForMIUI6(window, dark)
                immersive(window, color, alpha)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                darkModeForM(window, dark)
                immersive(window, color, alpha)
            }
            Build.VERSION.SDK_INT >= 19 -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                setTranslucentView(window.decorView as ViewGroup, color, alpha)
            }
            else -> {
                immersive(window, color, alpha)
            }
        }
    }

    //设置状态栏字体及图标颜色为黑色，只支持MIUI6以上、Flyme4以上以及Android M以上
    fun darkIconAndText(window: Window) {
        when {
            isFlyme4Later -> {
                darkModeForFlyme4(window, true)
            }
            isMIUI6Later -> {
                darkModeForMIUI6(window, true)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                darkModeForM(window, true)
            }
        }
    }

    //Flyme4以上设置状态栏字体和图标颜色，dark为true时为黑色，false为白色
    private fun darkModeForFlyme4(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            try {
                val e = window.attributes
                val darkFlag =
                    WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(e)
                if (dark) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(e, value)
                window.attributes = e
                result = true
            } catch (var8: Exception) {
                Log.e("StatusBar", "darkIcon: failed")
            }
        }
        return result
    }

    //MIUI6以上设置状态栏字体和图标颜色，dark为true时为黑色，false为白色
    private fun darkModeForMIUI6(window: Window, darkmode: Boolean): Boolean {
        val clazz = window.javaClass
        return try {
            val darkModeFlag: Int
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            darkModeFlag = field.getInt(layoutParams)
            val extraFlagField =
                clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            extraFlagField.invoke(window, if (darkmode) darkModeFlag else 0, darkModeFlag)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                if (darkmode) {
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    //android M（6.0）以上设置状态栏字体和图标颜色，dark为true时为黑色，false为白色
    @RequiresApi(Build.VERSION_CODES.M)
    private fun darkModeForM(window: Window, dark: Boolean) {
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility = if (dark) {
            systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        window.decorView.systemUiVisibility = systemUiVisibility
    }

    //设置状态栏透明且状态栏高度为0
    private fun immersive(
        window: Window,
        color: Int = DEFAULT_COLOR,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float = DEFAULT_ALPHA
    ) {
        if (Build.VERSION.SDK_INT >= 21) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = mixtureColor(color, alpha)

            var systemUiVisibility = window.decorView.systemUiVisibility
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = systemUiVisibility
        } else if (Build.VERSION.SDK_INT >= 19) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setTranslucentView(window.decorView as ViewGroup, color, alpha)
        } else {
            var systemUiVisibility = window.decorView.systemUiVisibility
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = systemUiVisibility
        }
    }

    //创建假的透明栏
    private fun setTranslucentView(container: ViewGroup, color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            val mixtureColor = mixtureColor(color, alpha)
            var translucentView: View? = container.findViewById(android.R.id.custom)
            if (translucentView == null && mixtureColor != 0) {
                translucentView = View(container.context)
                translucentView.id = android.R.id.custom
                val lp = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight(container.context)
                )
                container.addView(translucentView, lp)
            }
            if (translucentView != null) {
                translucentView.setBackgroundColor(mixtureColor)
            }
        }
    }

    private fun mixtureColor(color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float): Int {
        val a = if (color and -0x1000000 == 0) 0xff else color.ushr(24)
        return color and 0x00ffffff or ((a * alpha).toInt() shl 24)
    }

    //增加View的paddingTop,增加的值为状态栏高度
    fun setPadding(context: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            view.setPadding(
                view.paddingLeft, view.paddingTop + getStatusBarHeight(context),
                view.paddingRight, view.paddingBottom
            )
        }
    }

    //增加View的paddingTop,增加的值为状态栏高度 (智能判断，并设置高度)
    fun setPaddingSmart(context: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            val lp = view.layoutParams
            if (lp != null && lp.height > 0) {
                lp.height += getStatusBarHeight(context)  //增高
            }
            view.setPadding(
                view.paddingLeft, view.paddingTop + getStatusBarHeight(context),
                view.paddingRight, view.paddingBottom
            )
        }
    }

    //增加View的高度以及paddingTop,增加的值为状态栏高度.一般是在沉浸式全屏给ToolBar用的
    fun setHeightAndPadding(context: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            val lp = view.layoutParams
            lp.height += getStatusBarHeight(context)  //增高
            view.setPadding(
                view.paddingLeft, view.paddingTop + getStatusBarHeight(context),
                view.paddingRight, view.paddingBottom
            )
        }
    }

    //增加View上边距（MarginTop）一般是给高度为 WARP_CONTENT 的小控件用的
    fun setMargin(context: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            val lp = view.layoutParams
            if (lp is ViewGroup.MarginLayoutParams) {
                lp.topMargin += getStatusBarHeight(context)  //增高
            }
            view.layoutParams = lp
        }
    }

    //通过反射获取状态栏高度
    fun getStatusBarHeight(context: Context): Int {
        var result = 24
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        result = if (resId > 0) {
            context.resources.getDimensionPixelSize(resId)
        } else {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                result.toFloat(), Resources.getSystem().displayMetrics
            ).toInt()
        }
        return result
    }

    //隐藏导航栏，并且全屏
    //SystemUI Flag介绍：https://www.jianshu.com/p/e6656707f56c
    fun hideNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            activity.window.decorView.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            val options = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  //隐藏导航栏
                    or View.SYSTEM_UI_FLAG_FULLSCREEN  //隐藏状态栏且全屏展示
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            activity.window.decorView.systemUiVisibility = options
        }
    }

    //设置导航栏和状态栏透明
    fun setNavigationBarStatusBarTranslucent(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            val options = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            with(activity.window) {
                decorView.systemUiVisibility = options
                navigationBarColor = Color.TRANSPARENT
                statusBarColor = Color.TRANSPARENT
            }
        }
    }

}