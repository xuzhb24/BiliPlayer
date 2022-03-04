package com.android.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by xuzhb on 2019/10/28
 * Desc:SharedPreferences工具类
 */
class SPUtil<T>(val context: Context, val spName: String, val key: String, val defValue: T) :
    ReadWriteProperty<Any?, T> {

    //通过属性代理初始化SharedPreferences
    val prefs: SharedPreferences by lazy { context.getSharedPreferences(spName, Context.MODE_PRIVATE) }

    //接管属性值得获取行为
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = getValue(key, defValue)

    //接管属性值的修改行为
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putValue(key, value)
    }

    //利用with函数定义临时的命名空间
    private fun <T> getValue(key: String, defValue: T): T = with(prefs) {
        val value: Any = when (defValue) {
            is Long -> getLong(key, defValue)
            is String -> getString(key, defValue) ?: defValue
            is Int -> getInt(key, defValue)
            is Boolean -> getBoolean(key, defValue)
            is Float -> getFloat(key, defValue)
            else -> throw IllegalArgumentException("This type can not be saved into SharedPreferences")
        }
        return value as T
    }

    private fun <T> putValue(key: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            else -> throw IllegalArgumentException("This type can not be saved into SharedPreferences")
        }.apply() //commit方法和apply方法都表示提交修改
    }


}