package com.android.util

import androidx.annotation.StringDef
import kotlin.math.pow

/**
 * Created by xuzhb on 2020/12/1
 * Desc:流量的单位
 */
object ByteUnit {

    const val B = "byte"
    const val KB = "kb"
    const val MB = "mb"
    const val GB = "gb"

    @StringDef(B, KB, MB, GB)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class ByteUnitDef

    private const val UNIT = 1024.0

    fun convertByteUnit(totalBytes: Double, @ByteUnitDef unit: String): Double {
        var totalBytes = totalBytes
        if (totalBytes > 0) {
            when (unit) {
                KB -> totalBytes /= UNIT.pow(1.0)
                MB -> totalBytes /= UNIT.pow(2.0)
                GB -> totalBytes /= UNIT.pow(3.0)
            }
        }
        return totalBytes
    }

}