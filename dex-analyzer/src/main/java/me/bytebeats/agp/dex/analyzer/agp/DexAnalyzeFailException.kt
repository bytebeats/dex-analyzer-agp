package me.bytebeats.agp.dex.analyzer.agp

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/29 19:31
 * @Version 1.0
 * @Description TO-DO
 */

class DexAnalyzeFailException @JvmOverloads constructor(
    message: String? = "Failed in analyzing methods and fields of dex",
    error: Throwable? = null
) : RuntimeException(message, error)