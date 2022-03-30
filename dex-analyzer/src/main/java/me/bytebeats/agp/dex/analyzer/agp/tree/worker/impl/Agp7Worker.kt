package me.bytebeats.agp.dex.analyzer.agp.tree.worker.impl

import me.bytebeats.agp.dex.analyzer.DexNotParsedException
import me.bytebeats.agp.dex.analyzer.agp.Deobfuscator
import me.bytebeats.agp.dex.analyzer.agp.tree.worker.AbstractWorker
import org.gradle.api.file.RegularFileProperty
import java.io.IOException

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/30 19:50
 * @Version 1.0
 * @Description TO-DO
 */

abstract class Agp7Worker<P : Agp7Worker.Parameters> : AbstractWorker<P>() {
    interface Parameters : AbstractWorker.Parameters {
        fun getMappingFile(): RegularFileProperty
    }

    protected val deobuscator: Deobfuscator
        get() = parameters.getMappingFile().map {
            try {
                Deobfuscator.create(it.asFile)
            } catch (ignore: IOException) {
                throw DexNotParsedException("Fail to count dex symbols", ignore)
            }
        }.getOrElse(Deobfuscator.EMPTY)

}