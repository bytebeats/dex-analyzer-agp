package me.bytebeats.agp.dex.analyzer

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/21 20:33
 * @Version 1.0
 * @Description TO-DO
 */

class DexNotParsedException @JvmOverloads constructor(message: String? = null, t: Throwable? = null) :
        RuntimeException(message, t)