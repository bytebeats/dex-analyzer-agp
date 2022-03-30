package me.bytebeats.agp.dex.analyzer.agp

import com.google.gson.stream.JsonWriter
import me.bytebeats.agp.dex.analyzer.FieldRef
import me.bytebeats.agp.dex.analyzer.HasDeclaringClass
import me.bytebeats.agp.dex.analyzer.MethodRef
import me.bytebeats.agp.dex.analyzer.Printer
import org.jetbrains.annotations.NotNull
import java.io.IOException
import java.io.Writer
import java.nio.CharBuffer
import java.util.*
import java.util.function.Predicate
import java.util.stream.Stream


/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/30 15:36
 * @Version 1.0
 * @Description TO-DO
 */

class PackageTree @JvmOverloads constructor(
    val name: String = "",
    val isClass: Boolean = isClassName(name),
    private val deobfuscator: Deobfuscator = Deobfuscator.EMPTY
) {
    private val classCounter = LinkedHashMap<Type, Int>()
    private val methodCounter = LinkedHashMap<Type, Int>()
    private val fieldCounter = LinkedHashMap<Type, Int>()
    private val children = TreeMap<String, PackageTree>()
    private val methods = LinkedHashMap<Type, LinkedHashSet<MethodRef>>()
    private val fields = LinkedHashMap<Type, LinkedHashSet<FieldRef>>()

    init {
        for (type in Type.values()) {
            methods[type] = LinkedHashSet()
            fields[type] = LinkedHashSet()
        }
    }

    fun getClassCount(): Int = getClassCount(Type.REFERENCED)

    fun getClassCountDeclared(): Int = getClassCount(Type.DECLARED)

    private fun getClassCount(type: Type): Int {
        val count = classCounter[type]
        if (count != null) {
            return count
        }
        if (isClass) {
            classCounter[type] = 1
            return 1
        }
        val childCount = children.values.parallelStream().mapToInt { it.getClassCount(type) }.sum()
        classCounter[type] = childCount
        return childCount
    }

    fun getMethodCount(): Int = getMethodCount(Type.REFERENCED)

    fun getMethodCountDeclared(): Int = getMethodCount(Type.DECLARED)

    private fun getMethodCount(type: Type): Int {
        val count = methodCounter[type]
        if (count != null) {
            return count
        }
        var methodCount = methods[type]?.size ?: 0
        methodCount += children.values.parallelStream().mapToInt { it.getMethodCount(type) }.sum()
        methodCounter[type] = methodCount
        return methodCount
    }

    fun getFieldCount(): Int = getFieldCount(Type.REFERENCED)

    fun getFieldCountDeclared(): Int = getFieldCount(Type.DECLARED)

    private fun getFieldCount(type: Type): Int {
        val count = fieldCounter[type]
        if (count != null) {
            return count
        }
        var fieldCount = fields[type]?.size ?: 0
        fieldCount += children.values.parallelStream().mapToInt { it.getFieldCount(type) }.sum()
        fieldCounter[type] = fieldCount
        return fieldCount
    }

    private fun addInternal(name: String, startIndex: Int, isMethod: Boolean, type: Type, ref: HasDeclaringClass) {
        val idx = name.indexOf('.', startIndex)
        val segment = if (idx == -1) name.substring(startIndex)
        else name.substring(startIndex, idx)
        var child = children[segment]
        if (child == null) {
            child = PackageTree(segment, deobfuscator = deobfuscator)
            children[segment] = child
        }

        if (idx == -1) {
            if (isMethod) {
                child.methods[type]?.add(ref as MethodRef)
            } else {
                child.fields[type]?.add(ref as FieldRef)
            }
        } else {
            if (isMethod) {
                methodCounter.remove(type)
            } else {
                fieldCounter.remove(type)
            }
            child.addInternal(name, idx + 1, isMethod, type, ref)
        }
    }

    private fun descriptorToDot(ref: HasDeclaringClass): String {
        val descriptor = ref.getDeclaringClassName()
        val dot = Printer.descriptorToDot(descriptor)
        val deobfuscated = deobfuscator.deobfuscator(dot)
        return if (deobfuscated.indexOf('.') == -1) {
            "<unnamed>.$deobfuscated"
        } else deobfuscated
    }

    fun addMethodRef(ref: MethodRef) {
        addInternal(descriptorToDot(ref), 0, true, Type.REFERENCED, ref)
    }

    fun addFieldRef(ref: FieldRef) {
        addInternal(descriptorToDot(ref), 0, false, Type.REFERENCED, ref)
    }

    fun addDeclaredMethodRef(ref: MethodRef) {
        addInternal(descriptorToDot(ref), 0, true, Type.DECLARED, ref)
    }

    fun addDeclaredFieldRef(ref: FieldRef) {
        addInternal(descriptorToDot(ref), 0, false, Type.DECLARED, ref)
    }

    @Throws(IOException::class)
    fun print(out: Appendable, format: OutputFormat, opts: PrintOptions) {
        when (format) {
            OutputFormat.LIST -> printPackageList(out, opts)
            OutputFormat.TREE -> printTree(out, opts)
            OutputFormat.JSON -> printJson(out, opts)
            OutputFormat.YAML -> printYaml(out, opts)
            else -> throw IllegalArgumentException("Unexpected OutputFormat: $format")
        }
    }

    @Throws(IOException::class)
    fun printPackageList(out: Appendable, opts: PrintOptions) {
        val sb = StringBuilder(64)
        if (opts.getIncludeTotalMethodCount()) {
            if (opts.isAndroidProject()) {
                out.append("Total methods: ").append(getMethodCount().toString()).append("\n")
            }
            if (opts.getPrintDeclarations()) {
                out.append("Total declared methods: ").append(getClassCountDeclared().toString()).append("\n")
            }
        }
        if (opts.getPrintHeader()) {
            printPackageListHeader(out, opts)
        }
        for (child in getChildren(opts)) {
            child.printPackageListRecursively(out, sb, 0, opts)
        }
    }

    @Throws(IOException::class)
    private fun printPackageListHeader(out: Appendable, opts: PrintOptions) {
        if (opts.getIncludeClassCount()) {
            out.append("%-8s ".format("classes"))
        }
        if (opts.isAndroidProject()) {
            if (opts.getIncludeMethodCount()) {
                out.append("%-8s ".format("methods"))
            }
            if (opts.getIncludeFieldCount()) {
                out.append("%-8s ".format("fields"))
            }
        }
        if (opts.getPrintDeclarations()) {
            out.append("%-16s ".format("declared methods"))
            out.append("%-16s ".format("declared fields"))
        }
        out.append("package/class name\n")
    }

    @Throws(IOException::class)
    private fun printPackageListRecursively(out: Appendable, sb: StringBuilder, depth: Int, opts: PrintOptions) {
        if (depth >= opts.getMaxTreeDepth()) {
            return
        }
        check(isPrintable(opts)) {
            // Should be guaranteed by `getChildren()`
            "We should never recursively print a non-printable"
        }
        val len = sb.length
        if (len > 0) {
            sb.append('.')
        }
        sb.append(name)
        if (opts.getIncludeClassCount()) {
            out.append(String.format("%-8d ", getClassCount()))
        }
        if (opts.isAndroidProject()) {
            if (opts.getIncludeMethodCount()) {
                out.append(String.format("%-8d ", getMethodCount()))
            }
            if (opts.getIncludeFieldCount()) {
                out.append(String.format("%-8d ", getFieldCount()))
            }
        }
        if (opts.getPrintDeclarations()) {
            if (opts.getPrintHeader()) {
                // The header for the these two columns uses more space.
                out.append(String.format("%-16d ", getMethodCountDeclared()))
                out.append(String.format("%-16d ", getFieldCountDeclared()))
            } else {
                out.append(String.format("%-8d ", getMethodCountDeclared()))
                out.append(String.format("%-8d ", getFieldCountDeclared()))
            }
        }
        out.append(sb.toString()).append("\n")
        for (child in getChildren(opts)) {
            child.printPackageListRecursively(out, sb, depth + 1, opts)
        }
        sb.setLength(len)
    }

    @Throws(IOException::class)
    fun printTree(out: Appendable, opts: PrintOptions) {
        for (child in getChildren(opts)) {
            child.printTreeRecursively(out, 0, opts)
        }
    }

    @Throws(IOException::class)
    private fun printTreeRecursively(out: Appendable, depth: Int, opts: PrintOptions) {
        if (depth >= opts.getMaxTreeDepth()) {
            return
        }
        for (i in 0 until depth) {
            out.append("  ")
        }
        out.append(name)
        if (opts.getIncludeFieldCount() || opts.getIncludeMethodCount() || opts.getIncludeClassCount()) {
            out.append(" (")
            var appended = false
            if (opts.getIncludeClassCount()) {
                out.append(getClassCount().toString())
                    .append(" ")
                    .append(pluralizedClasses(getClassCount()))
                appended = true
            }
            if (opts.isAndroidProject()) {
                if (opts.getIncludeMethodCount()) {
                    if (appended) {
                        out.append(", ")
                    }
                    out.append(getMethodCount().toString())
                        .append(" ")
                        .append(pluralizedMethods(getMethodCount()))
                    appended = true
                }
                if (opts.getIncludeFieldCount()) {
                    if (appended) {
                        out.append(", ")
                    }
                    out.append(getFieldCount().toString())
                        .append(" ")
                        .append(pluralizedFields(getFieldCount()))
                    appended = true
                }
            }
            if (opts.getPrintDeclarations()) {
                if (appended) {
                    out.append(", ")
                }
                out.append(getMethodCountDeclared().toString())
                    .append(" declared ")
                    .append(pluralizedMethods(getMethodCountDeclared()))
                    .append(", ")
                    .append(getFieldCountDeclared().toString())
                    .append(" declared ")
                    .append(pluralizedFields(getFieldCountDeclared()))
            }
            out.append(")\n")
        }
        for (child in getChildren(opts)) {
            child.printTreeRecursively(out, depth + 1, opts)
        }
    }

    @Throws(IOException::class)
    fun printJson(out: Appendable, opts: PrintOptions) {
        val json = JsonWriter(object : Writer() {
            @Throws(IOException::class)
            override fun write(@NotNull chars: CharArray, offset: Int, length: Int) {
                out.append(CharBuffer.wrap(chars, offset, length))
            }

            override fun flush() {
                // no-op
            }

            override fun close() {
                // no-op
            }
        })
        json.setIndent("  ")
        printJsonRecursively(json, 0, opts)
    }

    @Throws(IOException::class)
    private fun printJsonRecursively(json: JsonWriter, depth: Int, opts: PrintOptions) {
        if (depth >= opts.getMaxTreeDepth()) {
            return
        }
        json.beginObject()
        json.name("name").value(name)
        if (opts.getIncludeClassCount()) {
            json.name("classes").value(getClassCount())
        }
        if (opts.isAndroidProject()) {
            if (opts.getIncludeMethodCount()) {
                json.name("methods").value(getMethodCount())
            }
            if (opts.getIncludeFieldCount()) {
                json.name("fields").value(getFieldCount())
            }
        }
        if (opts.getPrintDeclarations()) {
            json.name("declared_methods").value(getMethodCountDeclared())
            json.name("declared_fields").value(getFieldCountDeclared())
        }
        json.name("children")
        json.beginArray()
        for (child in getChildren(opts)) {
            child.printJsonRecursively(json, depth + 1, opts)
        }
        json.endArray()
        json.endObject()
    }

    @Throws(IOException::class)
    fun printYaml(out: Appendable, opts: PrintOptions) {
        out.append("---\n")
        if (opts.getIncludeClassCount()) {
            out.append("classes: ").append(getClassCount().toString()).append("\n")
        }
        if (opts.isAndroidProject()) {
            if (opts.getIncludeMethodCount()) {
                out.append("methods: ").append(getMethodCount().toString()).append("\n")
            }
            if (opts.getIncludeFieldCount()) {
                out.append("fields: ").append(getFieldCount().toString()).append("\n")
            }
        }
        if (opts.getPrintDeclarations()) {
            out.append("declared_methods: ").append(getMethodCountDeclared().toString()).append("\n")
            out.append("declared_fields: ").append(getFieldCountDeclared().toString()).append("\n")
        }
        out.append("counts:\n")
        for (child in getChildren(opts)) {
            child.printYamlRecursively(out, 0, opts)
        }
    }

    @Throws(IOException::class)
    private fun printYamlRecursively(out: Appendable, depth: Int, opts: PrintOptions) {
        if (depth > opts.getMaxTreeDepth()) {
            return
        }
        val indentBuilder = StringBuilder()
        for (i in 0 until depth * 2 + 1) {
            indentBuilder.append("  ")
        }
        var indent = indentBuilder.toString()
        out.append(indent).append("- name: ").append(name).append("\n")
        indent += "  "
        if (opts.getIncludeClassCount()) {
            out.append(indent).append("classes: ").append(getClassCount().toString()).append("\n")
        }
        if (opts.isAndroidProject()) {
            if (opts.getIncludeMethodCount()) {
                out.append(indent).append("methods: ").append(getMethodCount().toString()).append("\n")
            }
            if (opts.getIncludeFieldCount()) {
                out.append(indent).append("fields: ").append(getFieldCount().toString()).append("\n")
            }
        }
        if (opts.getPrintDeclarations()) {
            out.append(indent).append("declared_methods: ").append(getMethodCountDeclared().toString()).append("\n")
            out.append(indent).append("declared_fields: ").append(getFieldCountDeclared().toString()).append("\n")
        }
        val childNodes = if (depth + 1 == opts.getMaxTreeDepth()) {
            emptyList()
        } else {
            getChildren(opts)
        }
        if (childNodes.isEmpty()) {
            out.append(indent).append("children: []\n")
            return
        }
        out.append(indent).append("children:\n")
        for (child in getChildren(opts)) {
            child.printYamlRecursively(out, depth + 1, opts)
        }
    }

    private fun getChildren(opts: PrintOptions): List<PackageTree> {
        var result = children.values.filter { it.isPrintable(opts) }
        if (opts.getOrderByMethodCount()) {
            result = result.sortedBy { it.getMethodCount() }
        }
        return result
    }

    private fun isPrintable(opts: PrintOptions): Boolean {
        return opts.getIncludeClasses() || !isClass
    }

    private fun pluralizedClasses(n: Int): String {
        return if (n == 1) {
            "class"
        } else {
            "classes"
        }
    }

    private fun pluralizedMethods(n: Int): String {
        return if (n == 1) {
            "method"
        } else {
            "methods"
        }
    }

    private fun pluralizedFields(n: Int): String {
        return if (n == 1) {
            "field"
        } else {
            "fields"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PackageTree

        if (name != other.name) return false
        if (isClass != other.isClass) return false
        if (children != other.children) return false
        if (methods != other.methods) return false
        if (fields != other.fields) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + isClass.hashCode()
        result = 31 * result + children.hashCode()
        result = 31 * result + methods.hashCode()
        result = 31 * result + fields.hashCode()
        return result
    }


    private enum class Type {
        DECLARED, REFERENCED
    }

    companion object {
        private fun isClassName(name: String): Boolean =
            name.isNotEmpty() && name[0].isUpperCase() || name.contains("[]")
    }

}