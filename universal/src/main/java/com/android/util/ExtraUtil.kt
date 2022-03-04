package com.android.util

import android.os.Handler
import com.google.android.material.appbar.AppBarLayout

/**
 * Created by xuzhb on 2020/10/29
 * Desc:工具类扩展，存放单个方法
 */
//延迟执行任务，默认延迟1秒
fun postDelayed(delayMillis: Long = 1000, method: () -> Unit) {
    Handler().postDelayed(method, delayMillis)
}

//点赞、分享、评论个数格式化
fun formatCountStr(count: Int?, default: String = "—"): String {
    if (count == null) {
        return default
    }
    if (count <= 0) {
        return default
    }
    if (count > 10000) {
        return DigitalUtil.formatNumberByRounding3(count / 10000.0, 1) + "万"
    }
    return count.toString()
}

fun enableAppBarScroll(appBarLayout: AppBarLayout, canScroll: Boolean) {
    val firstChild = appBarLayout.getChildAt(0)
    val params = firstChild.layoutParams as AppBarLayout.LayoutParams
    if (canScroll) {
        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
        firstChild.layoutParams = params
    } else {
        params.scrollFlags = 0
    }
}

