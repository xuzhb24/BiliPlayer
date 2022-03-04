package com.android.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.telephony.TelephonyManager
import com.android.base.BaseApplication
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

/**
 * Created by xuzhb on 2019/12/29
 * Desc:网络工具
 */
object NetworkUtil {

    //打开网络设置界面
    fun openWirelessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    //是否连接网络
    fun isConnected(context: Context = BaseApplication.instance): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected
    }

    //获取NetworkInfo
    fun getNetworkInfo(context: Context) = getConnectivityManager(context).activeNetworkInfo

    //判断网络是否可用
    fun isAvailable(): Boolean {
        val result = ShellUtil.execCmd("ping -c 1 -w 1 223.5.5.5", false)
        LogUtil.e("isAvailable", result.toString())
        return result.code == 0
    }

    //判断移动数据是否打开
    fun getDataEnabled(context: Context): Boolean {
        try {
            val tm = getTelephonyManager(context)
            return tm.javaClass.getDeclaredMethod("getDataEnabled").invoke(tm) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    //打开或关闭移动数据，需要系统应用和权限<uses-permission android:name="android.permission.MODIFY_PHONE_STATE"/>
    fun setDataEnabled(context: Context, enabled: Boolean) {
        try {
            val tm = getTelephonyManager(context)
            tm.javaClass.getDeclaredMethod("setDataEnabled", Boolean::class.javaPrimitiveType).invoke(tm, enabled)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //判断网络是否是4G
    fun is4G(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isAvailable && info.subtype == TelephonyManager.NETWORK_TYPE_LTE
    }

    //判断wifi是否打开
    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = getWifiManager(context)
        return wifiManager.isWifiEnabled
    }

    //打开或关闭wifi
    fun setWifiEnabled(context: Context, enabled: Boolean) {
        val wifiManager = getWifiManager(context)
        if (enabled) {  //打开
            if (!wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = true
            }
        } else {  //关闭
            if (wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = false
            }
        }
    }

    //判断wifi是否连接
    fun isWifiConnected(context: Context): Boolean {
        val cm = getConnectivityManager(context)
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
    }

    //判断wifi是否可用
    fun isWifiAvailable(context: Context): Boolean {
        return isWifiEnabled(context) && isAvailable()
    }

    //获取ConnectivityManager
    fun getConnectivityManager(context: Context) =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    //获取TelephonyManager
    fun getTelephonyManager(context: Context) =
        context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    //获取WifiManager
    fun getWifiManager(context: Context) =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private const val NETWORK_TYPE_GSM = 16
    private const val NETWORK_TYPE_TD_SCDMA = 17
    private const val NETWORK_TYPE_IWLAN = 18

    enum class NetworkType {
        NETWORK_WIFI,
        NETWORK_4G,
        NETWORK_3G,
        NETWORK_2G,
        NETWORK_UNKNOWN,
        NETWORK_NO
    }

    //获取当前网络类型
    fun getNetworkType(context: Context): NetworkType {
        var netType = NetworkType.NETWORK_NO
        val info = getNetworkInfo(context)
        if (info != null && info.isAvailable) {
            netType = when (info.type) {
                ConnectivityManager.TYPE_WIFI -> {
                    NetworkType.NETWORK_WIFI
                }
                ConnectivityManager.TYPE_MOBILE -> {
                    when (info.subtype) {
                        NETWORK_TYPE_GSM,
                        TelephonyManager.NETWORK_TYPE_GPRS,
                        TelephonyManager.NETWORK_TYPE_CDMA,
                        TelephonyManager.NETWORK_TYPE_EDGE,
                        TelephonyManager.NETWORK_TYPE_1xRTT,
                        TelephonyManager.NETWORK_TYPE_IDEN -> {
                            NetworkType.NETWORK_2G
                        }
                        NETWORK_TYPE_TD_SCDMA,
                        TelephonyManager.NETWORK_TYPE_EVDO_A,
                        TelephonyManager.NETWORK_TYPE_UMTS,
                        TelephonyManager.NETWORK_TYPE_EVDO_0,
                        TelephonyManager.NETWORK_TYPE_HSDPA,
                        TelephonyManager.NETWORK_TYPE_HSUPA,
                        TelephonyManager.NETWORK_TYPE_HSPA,
                        TelephonyManager.NETWORK_TYPE_EVDO_B,
                        TelephonyManager.NETWORK_TYPE_EHRPD,
                        TelephonyManager.NETWORK_TYPE_HSPAP -> {
                            NetworkType.NETWORK_3G
                        }
                        NETWORK_TYPE_IWLAN,
                        TelephonyManager.NETWORK_TYPE_LTE -> {
                            NetworkType.NETWORK_4G
                        }
                        else -> {
                            val subtypeName = info.subtypeName
                            if (subtypeName.equals("TD-SCDMA", ignoreCase = true)
                                || subtypeName.equals("WCDMA", ignoreCase = true)
                                || subtypeName.equals("CDMA2000", ignoreCase = true)
                            ) {
                                NetworkType.NETWORK_3G
                            } else {
                                NetworkType.NETWORK_UNKNOWN
                            }
                        }
                    }
                }
                else -> {
                    NetworkType.NETWORK_UNKNOWN
                }
            }
        }
        return netType
    }

    //获取IP地址
    fun getIPAddress(useIPv4: Boolean): String? {
        try {
            val nis = NetworkInterface.getNetworkInterfaces()
            while (nis.hasMoreElements()) {
                val ni = nis.nextElement()
                // 防止小米手机返回10.0.2.15
                if (!ni.isUp) {
                    continue
                }
                val addresses = ni.inetAddresses
                while (addresses.hasMoreElements()) {
                    val inetAddress = addresses.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        val hostAddress = inetAddress.hostAddress
                        val isIPv4 = hostAddress.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4) return hostAddress
                        } else {
                            if (!isIPv4) {
                                val index = hostAddress.indexOf('%')
                                return if (index < 0) hostAddress.toUpperCase() else hostAddress.substring(0, index).toUpperCase()
                            }
                        }
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return null
    }

    //解析域名对应的IP地址
    fun getDomainAddress(domain: String?): String? {
        try {
            val exec = Executors.newCachedThreadPool()
            val fs = exec.submit<String>(Callable {
                val inetAddress: InetAddress
                try {
                    inetAddress = InetAddress.getByName(domain)
                    return@Callable inetAddress.hostAddress
                } catch (e: UnknownHostException) {
                    e.printStackTrace()
                }
                null
            })
            return fs.get()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        return null
    }

}