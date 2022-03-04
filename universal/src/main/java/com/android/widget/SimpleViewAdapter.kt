package com.android.widget

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

/**
 * Created by xuzhb on 2020/12/28
 * Desc:
 */
class SimpleViewAdapter(private val viewList: MutableList<View>) : PagerAdapter() {

    //view是否由对象产生，官方写view == `object`即可
    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    //数据源的数目
    override fun getCount(): Int = viewList.size

    //销毁一个页卡(即ViewPager的一个item)
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(viewList[position])
    }

    //对应页卡添加上数据
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(viewList[position])  //千万别忘记添加到container
        return viewList[position]
    }

}