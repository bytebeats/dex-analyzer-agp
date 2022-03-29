package me.bytebeats.agp.dex.analyzer.agp.source

import me.bytebeats.agp.dex.analyzer.DexNotParsedException
import me.bytebeats.agp.dex.analyzer.DexParser
import me.bytebeats.agp.dex.analyzer.FieldRef
import me.bytebeats.agp.dex.analyzer.MethodRef
import me.bytebeats.agp.dex.analyzer.agp.DexAnalyzeFailException
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/29 21:02
 * @Version 1.0
 * @Description TO-DO
 */

class DexFile(val file: File, val isTemporary: Boolean) : SourceFile {
    private val raf = RandomAccessFile(file, "r")
    private val dexParser = DexParser(raf)

    init {
        try {
            dexParser.parse()
        } catch (e: IOException) {
            throw DexAnalyzeFailException("Error parsing dex file", e)
        } catch (e: DexNotParsedException) {
            throw DexAnalyzeFailException("Error parsing dex file", e)
        }
    }

    override fun close() {
        raf.close()
        if (isTemporary) {
            file.delete()
        }
    }

    override fun getMethodRefs(): List<MethodRef> {
        return dexParser.getMethodRefs().toList()
    }

    override fun getFieldRefs(): List<FieldRef> {
        return dexParser.getFieldRefs().toList()
    }
}