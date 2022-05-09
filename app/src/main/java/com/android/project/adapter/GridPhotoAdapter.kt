package com.android.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.android.project.R
import com.android.project.util.showImageByCenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created by xuzhb on 2022/4/18
 * Desc:动态-综合-图片、图片详情页
 */
class GridPhotoAdapter(private val isOnePhoto: Boolean) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_grid_photo) {

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        //为什么重写这个方法，因为默认实现有个BUG，不能使用inflater.inflate(R.layout.item_grid_photo, parent, false)
        //这样会导致使用GridLayoutManager只会显示第一行的控件，超过一行的控件不会显示
        //所以使用inflater.inflate(R.layout.item_grid_photo, null, false)
        val convertView = inflater.inflate(R.layout.item_grid_photo, null, false)
        return createBaseViewHolder(convertView)
    }

    override fun convert(holder: BaseViewHolder, item: String) {
        val rootCl: ConstraintLayout = holder.getView(R.id.root_cl)
        holder.setVisible(R.id.content_iv, item.isNotBlank())
        if (item.isNotBlank()) {
            showImageByCenterCrop(holder.getView(R.id.content_iv), item, 4f)
        }
        if (isOnePhoto) {  //只有一张图片时高度自适应
            ConstraintSet().apply {
                clone(rootCl)
                setDimensionRatio(R.id.content_iv, "h,46:60")
                applyTo(rootCl)
            }
        } else {
            ConstraintSet().apply {
                clone(rootCl)
                setDimensionRatio(R.id.content_iv, "h,1:1")
                applyTo(rootCl)
            }
        }
    }
}