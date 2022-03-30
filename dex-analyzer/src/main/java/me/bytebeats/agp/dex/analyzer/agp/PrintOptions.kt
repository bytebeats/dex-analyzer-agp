package me.bytebeats.agp.dex.analyzer.agp

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/30 14:15
 * @Version 1.0
 * @Description TO-DO
 */

class PrintOptions private constructor(
    private val includeClasses: Boolean,
    private val includeClassCount: Boolean,
    private val includeMethodCount: Boolean,
    private val includeFieldCount: Boolean,
    private val includeTotalMethodCount: Boolean,
    private val printHeader: Boolean,
    private val orderByMethodCount: Boolean,
    private val maxTreeDepth: Int,
    private val maxMethodCount: Int,
    private val printDeclarations: Boolean,
    private val androidProject: Boolean,
    private val verbose: Boolean,
    private val outputFormat: OutputFormat = OutputFormat.LIST
) : java.io.Serializable {
    fun getIncludeClasses(): Boolean = includeClasses
    fun getIncludeClassCount(): Boolean = includeClassCount
    fun getIncludeMethodCount(): Boolean = includeMethodCount
    fun getIncludeFieldCount(): Boolean = includeFieldCount
    fun getIncludeTotalMethodCount(): Boolean = includeTotalMethodCount
    fun getPrintHeader(): Boolean = printHeader
    fun getOrderByMethodCount(): Boolean = orderByMethodCount
    fun getMaxTreeDepth(): Int = maxTreeDepth
    fun getMaxMethodCount(): Int = maxMethodCount
    fun getPrintDeclarations(): Boolean = printDeclarations
    fun isAndroidProject(): Boolean = androidProject
    fun isVerbose(): Boolean = verbose
    fun getOutputFormat(): OutputFormat = outputFormat

    fun toBuilder(): Builder = Builder().setIncludeClasses(includeClasses).setIncludeClassCount(includeClassCount)
        .setIncludeMethodCount(includeMethodCount).setIncludeFieldCount(includeFieldCount)
        .setIncludeTotalMethodCount(includeTotalMethodCount).setPrintHeader(printHeader)
        .setOrderByMethodCount(orderByMethodCount).setMaxTreeDepth(maxTreeDepth).setMaxMethodCount(maxMethodCount)
        .setPrintDeclarations(printDeclarations).setAndroidProject(androidProject).setVerbose(verbose)
        .setOutputFormat(outputFormat)

    class Builder {
        private var includeClasses: Boolean = false
        private var includeClassCount: Boolean = false
        private var includeMethodCount: Boolean = false
        private var includeFieldCount: Boolean = false
        private var includeTotalMethodCount: Boolean = false
        private var printHeader: Boolean = false
        private var orderByMethodCount: Boolean = false
        private var maxTreeDepth: Int = 0
        private var maxMethodCount: Int = 0
        private var printDeclarations: Boolean = false
        private var androidProject: Boolean = true
        private var verbose: Boolean = false
        private var outputFormat: OutputFormat = OutputFormat.LIST

        fun setIncludeClasses(includeClasses: Boolean): Builder {
            this.includeClasses = includeClasses
            return this
        }

        fun setIncludeClassCount(includeClassCount: Boolean): Builder {
            this.includeClassCount = includeClassCount
            return this
        }

        fun setIncludeMethodCount(includeMethodCount: Boolean): Builder {
            this.includeMethodCount = includeMethodCount
            return this
        }

        fun setIncludeFieldCount(includeFieldCount: Boolean): Builder {
            this.includeFieldCount = includeFieldCount
            return this
        }

        fun setIncludeTotalMethodCount(includeTotalMethodCount: Boolean): Builder {
            this.includeTotalMethodCount = includeTotalMethodCount
            return this
        }

        fun setPrintHeader(printHeader: Boolean): Builder {
            this.printHeader = printHeader
            return this
        }

        fun setOrderByMethodCount(orderByMethodCount: Boolean): Builder {
            this.orderByMethodCount = orderByMethodCount
            return this
        }

        fun setMaxTreeDepth(maxTreeDepth: Int): Builder {
            this.maxTreeDepth = maxTreeDepth
            return this
        }

        fun setMaxMethodCount(maxMethodCount: Int): Builder {
            this.maxMethodCount = maxMethodCount
            return this
        }

        fun setPrintDeclarations(printDeclarations: Boolean): Builder {
            this.printDeclarations = printDeclarations
            return this
        }

        fun setAndroidProject(androidProject: Boolean): Builder {
            this.androidProject = androidProject
            return this
        }

        fun setVerbose(verbose: Boolean): Builder {
            this.verbose = verbose
            return this
        }

        fun setOutputFormat(outputFormat: OutputFormat): Builder {
            this.outputFormat = outputFormat
            return this
        }

        fun build(): PrintOptions = PrintOptions(
            includeClasses,
            includeClassCount,
            includeMethodCount,
            includeFieldCount,
            includeTotalMethodCount,
            printHeader,
            orderByMethodCount,
            maxTreeDepth,
            maxMethodCount,
            printDeclarations,
            androidProject,
            verbose,
            outputFormat
        )
    }

    companion object {
        fun builder(): Builder {
            return Builder()
                .setIncludeClasses(false)
                .setIncludeClassCount(false)
                .setIncludeMethodCount(true)
                .setIncludeFieldCount(false)
                .setIncludeTotalMethodCount(false)
                .setPrintHeader(false)
                .setOrderByMethodCount(false)
                .setMaxTreeDepth(Integer.MAX_VALUE)
                .setMaxMethodCount(-1)
                .setPrintDeclarations(false)
                .setAndroidProject(true)
                .setVerbose(false)
                .setOutputFormat(OutputFormat.LIST)
        }

        fun fromDexAnalyzeExtension(ext: DexAnalyzeExtension): PrintOptions {
            return builder()
                .setIncludeClasses(ext.includeClasses.get())
                .setIncludeClassCount(ext.includeClassCount.get())
                .setIncludeMethodCount(true)
                .setIncludeFieldCount(ext.includeFieldCount.get())
                .setIncludeTotalMethodCount(ext.includeTotalMethodCount.get())
                .setPrintHeader(ext.printVersion.get())
                .setPrintDeclarations(ext.printDeclarations.get())
                .setMaxTreeDepth(ext.maxTreeDepth.get())
                .setMaxMethodCount(ext.maxMethodCount.get())
                .setOrderByMethodCount(ext.orderByMethodCount.get())
                .setVerbose(ext.verbose.get())
                .setOutputFormat(ext.outputFormat.get())
                .build()
        }

    }

}