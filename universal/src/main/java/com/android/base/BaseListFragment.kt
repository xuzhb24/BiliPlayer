package com.android.base

import android.view.LayoutInflater
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.android.util.LogUtil
import com.android.widget.recyclerView.CustomLoadMoreView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.lang.reflect.ParameterizedType

/**
 * Created by xuzhb on 2021/8/7
 * Desc:列表数据对应的基类Fragment(MVVM)
 */
abstract class BaseListFragment<T, VB : ViewBinding, VM : BaseListViewModel<T>> : BaseFragment<VB, VM>(), IBaseListView<T> {

    protected var mNextPageUrl: String = getFirstPageUrl()
    protected lateinit var mAdapter: BaseQuickAdapter<T, BaseViewHolder>

    override fun initViewBindingAndViewModel() {
        val superclass = javaClass.genericSuperclass
        val vbClass = (superclass as ParameterizedType).actualTypeArguments[1] as Class<VB>
        val vmClass = (superclass as ParameterizedType).actualTypeArguments[2] as Class<VM>
        val method = vbClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        binding = method.invoke(null, layoutInflater) as VB
        viewModel = ViewModelProvider(this).get(vmClass)
    }

    override fun initViewModelObserver() {
        super.initViewModelObserver()
        observerListDataChange()
    }

    //监听列表数据变化
    protected open fun observerListDataChange() {
        //获取到列表数据成功
        viewModel.successData.observe(this, Observer {
            showData(it.nextPageUrl ?: "", it.itemList)  //展示列表数据
        })
        //获取列表数据失败
        viewModel.errorData.observe(this, Observer {
            showToast(it.message)  //提示错误信息
        })
    }

    override fun initBaseView() {
        super.initBaseView()
        initAdapter()
    }

    protected open fun initAdapter() {
        mAdapter = getAdapter()
        if (mAdapter is LoadMoreModule) {  //上拉加载更多
            mAdapter.loadMoreModule.loadMoreView = CustomLoadMoreView()
            mAdapter.loadMoreModule.setOnLoadMoreListener {
                viewModel.loadData(mNextPageUrl, mLoadingLayout != null, mLoadingLayout == null && isFirstLoad())
            }
        }
        mRecyclerView?.adapter = mAdapter
    }

    //获取页面对应的adapter，注意adapter需要实现LoadMoreModule接口才能集成上拉加载更多的逻辑，如果没有实现就会当成普通的adapter进行处理，不能上拉加载更多
    abstract fun getAdapter(): BaseQuickAdapter<T, BaseViewHolder>

    //获取加载的起始地址
    abstract fun getFirstPageUrl(): String

    override fun refreshData() {
        //加载第一页的数据
        mNextPageUrl = getFirstPageUrl()
        viewModel.loadData(mNextPageUrl, mLoadingLayout != null, mLoadingLayout == null && isFirstLoad())
    }

    override fun showLoadingLayout() {
        activity?.runOnUiThread {
            if (mAdapter.data.size > 0) {  //已有数据则不显示加载状态
                mLoadingLayout?.loadComplete()
            } else {
                mSwipeRefreshLayout?.isRefreshing = true
            }
        }
    }

    override fun loadFinish(isError: Boolean) {
        activity?.runOnUiThread {
            if (!isError) {
                hasDataLoadedSuccess = true
            }
            showNetErrorLayout()
            if (isError) {  //数据加载失败
                if (mAdapter is LoadMoreModule) {
                    mAdapter.loadMoreModule.loadMoreFail()
                }
                if (mAdapter.data.size > 0) {
                    mLoadingLayout?.loadComplete()
                } else {
                    mLoadingLayout?.loadFail()
                }
            } else {
                if (mAdapter.data.size > 0) {
                    mLoadingLayout?.loadComplete()
                } else {
                    mLoadingLayout?.loadEmpty()  //空数据
                }
            }
            //取消加载弹窗
            mLoadingDialog?.dismiss()
            //完成下拉刷新动作
            mSwipeRefreshLayout?.isRefreshing = false
        }
    }

    //是否是首次加载
    override fun isFirstLoad() = mNextPageUrl == getFirstPageUrl()

    //展示列表数据
    override fun showData(nextPageUrl: String, list: MutableList<T>?) {
        val data = convertData(list)
        LogUtil.i("showData", "conver list size：${data?.size ?: -1}")
        if (isFirstLoad()) {  //首次加载
            mAdapter.setNewInstance(data)
        } else {
            mAdapter.addData(data ?: mutableListOf())
        }
        mNextPageUrl = nextPageUrl
        if (mAdapter is LoadMoreModule) {
            if (list == null || mNextPageUrl.isBlank()) {  //加载到底，没有更多数据
                mAdapter.loadMoreModule.loadMoreEnd()
//                mAdapter.loadMoreModule.isEnableLoadMore = false
            } else {  //完成一页的加载
                mAdapter.loadMoreModule.loadMoreComplete()
//                mAdapter.loadMoreModule.isEnableLoadMore = true
            }
        }
    }

    //可以通过重写这个方法处理返回的列表数据
    protected open fun convertData(response: MutableList<T>?): MutableList<T>? {
        return response
    }

}