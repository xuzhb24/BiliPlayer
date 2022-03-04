package com.android.project.adapter.banner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.project.R
import com.android.project.entity.ItemBean
import com.android.project.util.showImageByCenterCrop
import com.youth.banner.adapter.BannerAdapter

/**
 * Created by xuzhb on 2022/1/6
 * Desc:
 */
class BannerImageTitleAdapter<T>(list: MutableList<T>) : BannerAdapter<T, BannerImageTitleAdapter.Holder>(list) {

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent!!.context).inflate(R.layout.item_banner_image_title, parent, false))
    }

    override fun onBindView(holder: Holder?, data: T, position: Int, size: Int) {
        holder?.let {
            convertData(it, data, position, size)
        }
    }

    //数据处理
    private fun convertData(holder: Holder, data: T, position: Int, size: Int) {
        if (data is ItemBean.ItemX) {
            showImageByCenterCrop(holder.iv, data.data.image ?: "", 3.5f)
            holder.tv.text = data.data.description
        }
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val iv: ImageView = view.findViewById(R.id.iv)
        val tv: TextView = view.findViewById(R.id.tv)
    }

}