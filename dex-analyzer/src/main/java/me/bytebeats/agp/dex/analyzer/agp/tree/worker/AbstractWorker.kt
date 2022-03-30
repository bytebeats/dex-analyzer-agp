package me.bytebeats.agp.dex.analyzer.agp.tree.worker

import javafx.beans.property.Property
import me.bytebeats.agp.dex.analyzer.DexNotParsedException
import me.bytebeats.agp.dex.analyzer.agp.PackageTree
import me.bytebeats.agp.dex.analyzer.agp.PrintOptions
import me.bytebeats.agp.dex.analyzer.agp.plugin.DexAnalyzerPlugin
import org.apache.commons.io.FileUtils
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkAction
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.nio.file.Files

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/30 19:14
 * @Version 1.0
 * @Description TO-DO
 */

abstract class AbstractWorker<P : AbstractWorker.Parameters> :
        WorkAction<P> {
    private var outputDirectory: File? = null

    private fun getOutputDirectory(): File {
        if (outputDirectory == null) {
            outputDirectory = parameters.getOutputDirectory().get().asFile
        }
        return outputDirectory!!
    }

    override fun execute() {
        try {
            val packageTree = generatePackageTree()
            ensureOutputDirectoryClean()
            writeSummaryFile(packageTree)
            writeChartFile(packageTree)
            writeFullTree(packageTree)
        } catch (e: IOException) {
            throw DexNotParsedException("Fail to count dex symbol references", e)
        }
    }


    @Throws(IOException::class)
    private fun ensureOutputDirectoryClean() {
        FileUtils.deleteDirectory(getOutputDirectory())
        FileUtils.forceMkdir(getOutputDirectory())
    }

    @Throws(IOException::class)
    private fun writeSummaryFile(packageTree: PackageTree) {
        val summaryFile = File(getOutputDirectory(), "summary.csv")
        FileUtils.forceMkdirParent(summaryFile)
        val headers = "methods, fields, classes"
        val counts =
            "%d, %d, %d".format(packageTree.getMethodCount(), packageTree.getFieldCount(), packageTree.getClassCount())
        val writer = Files.newBufferedWriter(summaryFile.toPath())
        writer.append(headers).append('\n')
        writer.append(counts).append('\n')
    }

    @Throws(IOException::class)
    private fun writeChartFile(packageTree: PackageTree) {
        val chartDir = File(getOutputDirectory(), "chart")
        FileUtils.forceMkdir(chartDir)

        val options = parameters.getPrintOptions()
            .value
            .toBuilder()
            .setIncludeClasses(true)
            .build()
        val jsData = File(chartDir, "data.js")
        val out = Files.newBufferedWriter(jsData.toPath())
        out.write("var data = ")
        packageTree.printJson(out, options)

        for (resource in listOf("chart-builder.js", "d3.v3.min.js", "index.html", "style.css")) {
            val path = "me/bytebeats/agp/dex/analyzer/$resource"
            val inputStream = DexAnalyzerPlugin::class.java.classLoader.getResourceAsStream(resource)
            if (inputStream == null) {
                getLogger().error("No such resource: {}", resource)
                continue
            }
            val target = File(chartDir, resource)
            FileUtils.copyInputStreamToFile(inputStream, target)
        }
    }

    @Throws(IOException::class)
    private fun writeFullTree(packageTree: PackageTree) {
        val options = parameters.getPrintOptions().value
        val fullCounterFileName = parameters.getOutputFileName().value + options.getOutputFormat().extension
        val fullCounterFile = File(getOutputDirectory(), fullCounterFileName)
        val writer = Files.newBufferedWriter(fullCounterFile.toPath())
        packageTree.print(writer, options.getOutputFormat(), options)
    }

    protected abstract fun getLogger(): Logger
    protected abstract fun getInputRepresentation(): String
    protected abstract fun generatePackageTree(): PackageTree


    interface Parameters : WorkParameters {
        fun getOutputFileName(): Property<String>
        fun getPackageTreeFile(): RegularFileProperty
        fun getOutputDirectory(): DirectoryProperty
        fun getPrintOptions(): Property<PrintOptions>
    }
}