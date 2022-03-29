package me.bytebeats.agp.dex.analyzer.agp.source

import me.bytebeats.agp.dex.analyzer.FieldRef
import me.bytebeats.agp.dex.analyzer.MethodRef

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/29 21:09
 * @Version 1.0
 * @Description TO-DO
 */

class JarFile(private val methodRefs: List<MethodRef>, private val fieldRefs: List<FieldRef>) : SourceFile {
    override fun getFieldRefs(): List<FieldRef> = fieldRefs
    override fun getMethodRefs(): List<MethodRef> = methodRefs

    override fun close() {
        //do nothing here
    }
}