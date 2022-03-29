package me.bytebeats.agp.dex.analyzer.agp

import org.gradle.api.Project

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/29 17:31
 * @Version 1.0
 * @Description TO-DO
 */

fun isInstantRun(project: Project): Boolean {
    val propCompilation = project.properties["android.optional.compilation"]
    if (propCompilation != null && propCompilation is String) {
        val options = propCompilation.split(",")
        return options.contains("INSTANT_DEV")
    }
    return false
}