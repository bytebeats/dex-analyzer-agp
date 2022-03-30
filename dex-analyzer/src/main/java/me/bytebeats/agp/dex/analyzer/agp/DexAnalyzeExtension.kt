package me.bytebeats.agp.dex.analyzer.agp

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import javax.inject.Inject

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/29 19:37
 * @Version 1.0
 * @Description TO-DO
 */


class DexAnalyzeExtension @Inject constructor(objectFactory: ObjectFactory, providerFactory: ProviderFactory) {
    private val runOnEachPackageProperty: Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(true)

    /**
     * When false, does not automatically count methods following the `package` task.
     */
    @Internal("plugin input, not task input")
    val runOnEachPackage: Property<Boolean> = runOnEachPackageProperty

    private val outputFormatProperty: Property<OutputFormat> =
        objectFactory.property(OutputFormat::class.java).convention(OutputFormat.LIST)

    /**
     * The format of the method count output, either "list", "tree", "json", or "yaml".
     */
    @Input
    val outputFormat: Property<OutputFormat> = outputFormatProperty

    private val includeClassesProperty: Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(false)

    /**
     * When true, individual classes will be included in the package list - otherwise, only packages
     * are included.
     */
    @Input
    val includeClasses: Property<Boolean> = includeClassesProperty


    private val includeClassCountProperty: Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(false)

    /**
     * When true, the number of classes in a package or class will be included in the printed output.
     */
    @Input
    val includeClassCount: Property<Boolean> = includeClassCountProperty
    private val includeFieldCountProperty: Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(false)

    /**
     * When true, the number of fields in a package or class will be included in the printed output.
     */
    @Input
    val includeFieldCount: Property<Boolean> = includeFieldCountProperty


    private val includeTotalMethodCountProperty: Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(false)

    /**
     * When true, the total number of methods in the application will be included in the printed
     * output.
     */
    @Input
    val includeTotalMethodCount: Property<Boolean> = includeTotalMethodCountProperty

    private val orderByMethodCountProperty: Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(false)

    /**
     * When true, packages will be sorted in descending order by the number of methods they contain.
     */
    @Input
    val orderByMethodCount: Property<Boolean> = orderByMethodCountProperty

    private val verboseProperty: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(false)

    /**
     * When true, the output file will also be printed to the build's standard output.
     */
    @Internal
    val verbose: Property<Boolean> = verboseProperty

    private val maxTreeDepthProperty: Property<Int> = objectFactory.property(Int::class.java).convention(Int.MAX_VALUE)

    /**
     * Sets the max number of package segments in the output - i.e. when set to 2, counts stop at
     * com.google, when set to 3 you get com.google.android, etc. "Unlimited" by default.
     */
    @Input
    val maxTreeDepth: Property<Int> = maxTreeDepthProperty

    private val maxMethodCountProperty: Property<Int> = objectFactory.property(Int::class.java).convention(-1)

    /**
     * When set, the build will fail when the APK/AAR has more methods than the max. 0 by default.
     */
    @Input
    val maxMethodCount: Property<Int> = maxMethodCountProperty

    private val printVersionProperty: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(false)

    /**
     * If the user has passed '--stacktrace' or '--full-stacktrace', assume that they are trying to
     * report a dexcount bug. Help us help them out by printing the current plugin title and version.
     */
    @Internal("stdout-only")
    val printVersion: Property<Boolean> = printVersionProperty

    private val printDeclarationsProperty: Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(false)

    /**
     * When true, then the plugin only counts the declared methods and fields inside this module.
     * This does NOT represent the actual reference method count, because method references are
     * ignored. This flag is false by default and can only be turned on for library modules.
     */
    @Input
    val printDeclarations: Property<Boolean> = printDeclarationsProperty

    private val enabledProperty: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(true)

    /**
     * When true, the plugin is enabled and will be run as normal.  When false,
     * the plugin is disabled and will not be run.
     */
    @Internal("this is plugin input, not task input")
    val enabled: Property<Boolean> = enabledProperty
}