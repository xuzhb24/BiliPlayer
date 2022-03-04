package com.android.http

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Created by xuzhb on 2019/8/8
 * Desc:持久化Cookie管理
 */
class CookieJarManage : CookieJar {

    companion object {
        //单例对象
        val instance = SingleTonHolder.holder
    }

    //静态内部类单例模式
    private object SingleTonHolder {
        val holder = CookieJarManage()
    }

    private val cookieStore = HashMap<String, List<Cookie>>()

    //网路访问后将服务器返回的cookies和对应的url存储在cookieStore中
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
    }

    //网路访问开始的时候，根据访问的url去查找cookie，然后将cokies放到请求头里面
    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        cookieStore[url.host] ?: mutableListOf<Cookie>() //cookieStore.get(url)为null时返回mutableListOf<Cookie>()

}