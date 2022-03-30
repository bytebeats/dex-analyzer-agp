package me.bytebeats.agp.dex.analyzer.agp

import org.apache.commons.io.FileUtils
import java.io.File
import java.util.regex.Pattern

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/30 15:17
 * @Version 1.0
 * @Description TO-DO
 */

/**
 * An object that can produce an unobfuscated class name from a Proguard
 * mapping file.
 */
class Deobfuscator(private val mapping: Map<String, String>) {

    fun deobfuscator(name: String): String = mapping.getOrDefault(name, name)

    companion object {
        /**
         * Proguard mapping files have the following syntax:
         *
         * ```
         * line : comment | class_mapping | member_mapping
         * comment: '#' ...
         * class_mapping: type_name ' -> ' obfuscated_name ':'
         * member_mapping: '    ' type_name ' ' member_name ' -> ' obfuscated_name
         * ```
         *
         * Class mapping lines are easily distinguished because they're the only
         * lines that start with an identifier character.  We can just pluck them
         * out of the file with a regex.
         */
        private val CLASS_LINE = Pattern.compile("^([a-zA-Z][^\\s]*) -> ([^:]+):$")
        val EMPTY = Deobfuscator(emptyMap())

        @JvmStatic
        fun create(mappingFile: File?): Deobfuscator {
            if (mappingFile?.exists() != true) return EMPTY
            val mapping = LinkedHashMap<String, String>()
            val lines = FileUtils.readLines(mappingFile, Charsets.UTF_8)
            for (line in lines) {
                val matcher = CLASS_LINE.matcher(line)
                if (matcher.matches()) {
                    val clearText = matcher.group(1)
                    val obfuscated = matcher.group(2)
                    mapping[obfuscated] = clearText
                }
            }
            return Deobfuscator(mapping)
        }
    }
}