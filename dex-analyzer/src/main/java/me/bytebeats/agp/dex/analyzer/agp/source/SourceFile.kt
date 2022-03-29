package me.bytebeats.agp.dex.analyzer.agp.source

import me.bytebeats.agp.dex.analyzer.FieldRef
import me.bytebeats.agp.dex.analyzer.MethodRef
import java.io.Closeable

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/29 21:00
 * @Version 1.0
 * @Description TO-DO
 */

interface SourceFile : Closeable {
    fun getMethodRefs(): List<MethodRef>
    fun getFieldRefs(): List<FieldRef>
}