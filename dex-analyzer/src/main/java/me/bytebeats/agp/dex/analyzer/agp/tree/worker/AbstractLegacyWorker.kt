package me.bytebeats.agp.dex.analyzer.agp.tree.worker

import me.bytebeats.agp.dex.analyzer.agp.Deobfuscator
import me.bytebeats.agp.dex.analyzer.agp.PackageTree
import me.bytebeats.agp.dex.analyzer.agp.source.extractDex
import me.bytebeats.agp.dex.analyzer.agp.source.extractJarFromAar
import me.bytebeats.agp.dex.analyzer.agp.source.extractJarFromJar
import org.gradle.api.file.RegularFileProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/30 19:50
 * @Version 1.0
 * @Description TO-DO
 */

abstract class AbstractLegacyWorker<P : AbstractLegacyWorker.Parameters> : AbstractWorker<P>() {
    interface Parameters : AbstractWorker.Parameters {
        fun getInputFile(): RegularFileProperty
        fun getMappingFile(): RegularFileProperty
    }

    override fun generatePackageTree(): PackageTree {
        val options = parameters.getPrintOptions().value
        val deobfuscator = Deobfuscator.create(parameters.getMappingFile().asFile.orNull)
        val inputFile = parameters.getInputFile().asFile.get()
        val fileName = inputFile.name

        val isApk = fileName.endsWith(".apk")
        val isAar = fileName.endsWith(".aar")
        val isJar = fileName.endsWith(".jar")
        val isAndroid = isApk || isAar

        if (!isApk && !isAar && !isJar) {
            throw IllegalStateException("File type is not supported: $fileName")
        }
        val packageTree = PackageTree(deobfuscator = deobfuscator)
        if (isAndroid) {
            val sourceFiles = extractDex(inputFile)
            for (methodRef in sourceFiles.flatMap { it.getMethodRefs() }) {
                packageTree.addMethodRef(methodRef)
            }
            for (fieldRef in sourceFiles.flatMap { it.getFieldRefs() }) {
                packageTree.addFieldRef(fieldRef)
            }
        }
        val jar = if (isAar && options.getPrintDeclarations()) {
            extractJarFromAar(inputFile)
        } else {
            extractJarFromJar(inputFile)
        }
        for (methodRef in jar.getMethodRefs()) {
            packageTree.addMethodRef(methodRef)
        }
        for (fieldRef in jar.getFieldRefs()) {
            packageTree.addFieldRef(fieldRef)
        }
        return packageTree
    }

    override fun getInputRepresentation(): String = parameters.getInputFile().asFile.get().name

    override fun getLogger(): Logger = LOGGER

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AbstractLegacyWorker::class.java)
    }
}