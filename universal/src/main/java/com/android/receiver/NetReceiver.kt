package com.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.android.util.NetworkUtil

/**
 * Created by xuzhb on 2019/12/29
 * Desc:监听网络变化
 */
class NetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if ((ConnectivityManager.CONNECTIVITY_ACTION).equals(intent?.action)) {
            mOnNetChangeListener?.invoke(NetworkUtil.isConnected(context!!))
        }
    }

    private var mOnNetChangeListener: ((isConnected: Boolean) -> Unit)? = null

    fun setOnNetChangeListener(listener: ((isConnected: Boolean) -> Unit)) {
        this.mOnNetChangeListener = listener
    }

}