package com.android.http

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Created by xuzhb on 2019/8/8
 * Desc:校验证书
 */
object SSLSocketFactoryUtil {

    fun getPassAnySSLSocketFactory(): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLS")
        return sslContext.apply {
            init(null, arrayOf<TrustManager>(TrustAllManager()), SecureRandom())
        }.socketFactory
    }

    fun getTrustAllManager() = TrustAllManager()

    class TrustAllManager : X509TrustManager {
        override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
        }

        override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return emptyArray<X509Certificate>()
        }

    }

}