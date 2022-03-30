package me.bytebeats.agp.dex.analyzer.agp

import com.google.auto.value.AutoValue
import javax.annotation.Nullable

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/30 14:15
 * @Version 1.0
 * @Description TO-DO
 */

@AutoValue
abstract class PrintOptions : java.io.Serializable {
    abstract fun getIncludeClasses(): Boolean
    abstract fun getIncludeClassCount(): Boolean
    abstract fun getIncludeMethodCount(): Boolean
    abstract fun getIncludeFieldCount(): Boolean
    abstract fun getIncludeTotalMethodCount(): Boolean
    abstract fun getTeamCityIntegration(): Boolean

    @Nullable
    abstract fun getTeamCitySlug(): String?
    abstract fun getPrintHeader(): Boolean
    abstract fun getOrderByMethodCount(): Boolean
    abstract fun getMaxTreeDepth(): Int
    abstract fun getMaxMethodCount(): Int
    abstract fun getPrintDeclarations(): Boolean
    abstract fun isAndroidProject(): Boolean
    abstract fun isVerbose(): Boolean
    abstract fun getOutputFormat(): OutputFormat

    abstract fun toBuilder(): Builder

    fun withProjectIsAndroid(isAndroidProject: Boolean): PrintOptions {
        return toBuilder().setAndroidProject(isAndroidProject).build()
    }

    @AutoValue.Builder
    abstract class Builder {
        abstract fun setIncludeClasses(includeClasses: Boolean): Builder
        abstract fun setIncludeClassCount(includeClassCount: Boolean): Builder
        abstract fun setIncludeMethodCount(includeMethodCount: Boolean): Builder
        abstract fun setIncludeFieldCount(includeFieldCount: Boolean): Builder
        abstract fun setIncludeTotalMethodCount(includeTotalMethodCount: Boolean): Builder
        abstract fun setTeamCityIntegration(teamCityIntegration: Boolean): Builder
        abstract fun setTeamCitySlug(teamCitySlug: String?): Builder
        abstract fun setPrintHeader(printHeader: Boolean): Builder
        abstract fun setOrderByMethodCount(orderByMethodCount: Boolean): Builder
        abstract fun setMaxTreeDepth(maxTreeDepth: Int): Builder
        abstract fun setMaxMethodCount(maxMethodCount: Int): Builder
        abstract fun setPrintDeclarations(printDeclarations: Boolean): Builder
        abstract fun setAndroidProject(androidProject: Boolean): Builder
        abstract fun setVerbose(verbose: Boolean): Builder
        abstract fun setOutputFormat(outputFormat: OutputFormat): Builder

        abstract fun build(): PrintOptions
    }

    companion object {
        fun builder(): Builder {
            return AutoValue_PrintOptions.Builder()
                .setIncludeClasses(false)
                .setIncludeClassCount(false)
                .setIncludeMethodCount(true)
                .setIncludeFieldCount(false)
                .setIncludeTotalMethodCount(false)
                .setTeamCityIntegration(false)
                .setTeamCitySlug(null)
                .setPrintHeader(false)
                .setOrderByMethodCount(false)
                .setMaxTreeDepth(Integer.MAX_VALUE)
                .setMaxMethodCount(-1)
                .setPrintDeclarations(false)
                .setAndroidProject(true)
                .setVerbose(false)
                .setOutputFormat(OutputFormat.LIST)
        }

        fun fromDexCountExtension(ext: DexAnalyzeExtension): PrintOptions {
            return builder()
                .setIncludeClasses(ext.includeClasses.get())
                .setIncludeClassCount(ext.includeClassCount.get())
                .setIncludeMethodCount(true)
                .setIncludeFieldCount(ext.includeFieldCount.get())
                .setIncludeTotalMethodCount(ext.includeTotalMethodCount.get())
                .setTeamCityIntegration(ext.teamCityIntegration.get())
                .setTeamCitySlug(ext.teamCitySlug.orNull)
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