package me.bytebeats.agp.dex.analyzer.agp

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/29 17:43
 * @Version 1.0
 * @Description TO-DO
 */

/**
 * Specifies what format the task output should take.
 */
enum class OutputFormat(val extension: String) {
    /**
     * Specifies that method counts will be printed in a flat list of packages.
     */
    LIST(".txt"),

    /**
     * Specifies that the output will be pretty-printed as an tree.
     */
    TREE(".txt"),

    /**
     * Specifies that the output will be a pretty-printed JSON object.
     */
    JSON(".json"),

    /**
     * Specifies that output will be a YAML document.
     */
    YAML(".yml"),
}