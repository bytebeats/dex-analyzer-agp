package me.bytebeats.agp.dex.analyzer.agp.tree.worker

import me.bytebeats.agp.dex.analyzer.agp.Deobfuscator
import me.bytebeats.agp.dex.analyzer.agp.PackageTree
import me.bytebeats.agp.dex.analyzer.agp.source.extractDex
import org.gradle.api.file.RegularFileProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/30 21:15
 * @Version 1.0
 * @Description TO-DO
 */

abstract class AbstractJarWorker : AbstractWorker<AbstractJarWorker.Parameters>() {
    interface Parameters : AbstractModernWorker.Parameters {
        fun getJarFile(): RegularFileProperty
    }

    private fun getInputFile(): File {
        return parameters.getJarFile().asFile.get()
    }

    override fun generatePackageTree(): PackageTree {
        val packageTree = PackageTree(deobfuscator = Deobfuscator.EMPTY)
        val inputFile = getInputFile()
        val sourceFiles = extractDex(inputFile)
        for (methodRef in sourceFiles.flatMap { it.getMethodRefs() }) {
            packageTree.addMethodRef(methodRef)
        }
        for (fieldRef in sourceFiles.flatMap { it.getFieldRefs() }) {
            packageTree.addFieldRef(fieldRef)
        }
        return packageTree
    }

    override fun getInputRepresentation(): String = getInputFile().name

    override fun getLogger(): Logger = LOGGER

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AbstractJarWorker::class.java)
    }
}