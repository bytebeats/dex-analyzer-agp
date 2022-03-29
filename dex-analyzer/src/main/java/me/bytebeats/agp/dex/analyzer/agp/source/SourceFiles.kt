package me.bytebeats.agp.dex.analyzer.agp.source

import java.io.File
import java.io.IOException
import java.util.regex.Pattern
import java.util.zip.ZipException

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/29 21:48
 * @Version 1.0
 * @Description TO-DO
 */


private val CLASSES_DEX = Pattern.compile("(.*/)*classes.*\\.dex")
private val CLASSES_JAR = Pattern.compile("(.*/)*classes\\.jar")
private val MIN_SDK_VERSION = Pattern.compile("android:minSdkVersion=\"(\\d+)\"")


@Throws(IOException::class)
fun extractDexData(file: File?): List<SourceFile> {
    if (file?.exists() == false) return emptyList()
    if (file?.name?.endsWith(".aar") == true) {
        return extractDexFromAar(file)
    }
    return try {
        extractDexFromZip(file!!)
    } catch (ignore: ZipException) {
        listOf(DexFile(file!!, false))
    }
}

@Throws(IOException::class)
fun extractDexFromAar(aar: File): List<SourceFile> {
    return emptyList()
}

@Throws(IOException::class)
fun extractDexFromZip(zip: File): List<SourceFile> {
    return emptyList()
}

