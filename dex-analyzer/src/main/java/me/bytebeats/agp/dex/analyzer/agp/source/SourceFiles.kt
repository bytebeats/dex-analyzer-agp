package me.bytebeats.agp.dex.analyzer.agp.source

import javassist.ByteArrayClassPath
import javassist.ClassPool
import javassist.CtBehavior
import javassist.CtClass
import javassist.CtMethod
import javassist.NotFoundException
import me.bytebeats.agp.dex.analyzer.FieldRef
import me.bytebeats.agp.dex.analyzer.MethodRef
import me.bytebeats.agp.dex.analyzer.agp.DexAnalyzeFailException
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.regex.Pattern
import java.util.zip.ZipException
import java.util.zip.ZipFile

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/29 21:48
 * @Version 1.0
 * @Description TO-DO
 */


private val CLASSES_DEX = Pattern.compile("(.*/)*classes.*\\.dex")
private val CLASSES_JAR = Pattern.compile("(.*/)*classes\\.jar")
private val MIN_SDK_VERSION = Pattern.compile("android:minSdkVersion=\"(\\d+)\"")


@Throws(IOException::class)
fun extractDex(file: File?): List<SourceFile> {
    if (file?.exists() != true) return emptyList()
    if (file.name.endsWith(".aar")) {
        return extractDexFromAar(file)
    }
    return try {
        extractDexFromZip(file)
    } catch (ignore: ZipException) {
        listOf(DexFile(file, false))
    }
}

@Throws(IOException::class)
fun extractDexFromAar(aar: File): List<SourceFile> {

    return emptyList()
}

@Throws(IOException::class)
fun extractDexFromZip(zip: File): List<SourceFile> {
    return emptyList()
}

fun extractJarFromJar(jarFile: File): SourceFile {
    val classFilesDir = Files.createTempDirectory("classFilesDir").toFile()
    classFilesDir.deleteOnExit()
    val path = classFilesDir.toPath()
    try {
        val zip = ZipFile(jarFile)
        zip.stream().filter { it.name.endsWith(".class") }.forEach { entry ->
            val file = File(classFilesDir, entry.name)
            FileUtils.createParentDirectories(file)
            val inputStream = zip.getInputStream(entry)
            FileUtils.copyInputStreamToFile(inputStream, file)
        }
    } catch (io: IOException) {
        throw DexAnalyzeFailException("Fail to unzip a class.jar file", io)
    }
    val classPool = ClassPool()
    classPool.appendSystemPath()
    val classes = mutableListOf<CtClass>()
    Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
        override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
            if (attrs?.isRegularFile == true && file != null) {
                val qualifiedClassName = path.relativize(file)
                    .toFile()
                    .path
                    .replace('/', '.')
                    .replace(".class", "")
                val byteArrayClassPath = ByteArrayClassPath(qualifiedClassName, Files.readAllBytes(file))
                classPool.appendClassPath(byteArrayClassPath)
                try {
                    classes.add(classPool.get(qualifiedClassName))
                } catch (e: NotFoundException) {
                    throw AssertionError("We literally just added this class to the pool", e)
                }
            }
            return FileVisitResult.CONTINUE
        }
    })
    val methodRefs = classes.flatMap { extractMethodRefs(it) }
    val fieldRefs = classes.flatMap { extractFieldRefs(it) }
    return JarFile(methodRefs, fieldRefs)
}

private fun extractMethodRefs(klazz: CtClass): List<MethodRef> {
    val declaringClass = "L${klazz.name.replace('.', '/')};"
    // Unfortunately, it's necessary to parse the types from the strings manually.
    // We can't use the proper API because this requires all classes that are used
    // in parameters and return types to be loaded in the classpath. However,
    // that's not the case when we analyze a single jar file.
    val methodRefs = mutableListOf<MethodRef>()
    klazz.classInitializer?.let {
        methodRefs.add(MethodRef(declaringClass, "<clinit>", Array(0) { "" }, "V"))
    }
    for (constructor in klazz.declaredConstructors) {
        val params = parseBehaviorParameters(constructor)
        methodRefs.add(MethodRef(declaringClass, "<init>", params, "V"))
    }
    for (method in klazz.declaredMethods) {
        val params = parseBehaviorParameters(method)
        val returnType = parseMethodReturnType(method)
        methodRefs.add(MethodRef(declaringClass, method.name, params, returnType))
    }
    return methodRefs
}

private fun extractFieldRefs(klazz: CtClass): List<FieldRef> =
    klazz.declaredFields.map { f ->
        val type = f.fieldInfo.descriptor
        FieldRef(klazz.simpleName, f.name, type)
    }

private fun parseBehaviorParameters(behavior: CtBehavior): Array<String> {
    val signature = behavior.signature
    val startIdx = signature.indexOf('(')
    val endIdx = signature.indexOf(')')
    val parameters = signature.substring(startIdx + 1, endIdx)
    return parameters.split(";").toTypedArray()
}

private fun parseMethodReturnType(method: CtMethod): String {
    val idx = method.signature.indexOf(')')
    return method.signature.substring(idx + 1)
}

private fun makeTempFile(pattern: String): File = try {
    val idx = pattern.indexOf('.')
    val prefix = pattern.substring(0, idx)
    val suffix = pattern.substring(idx)
    val tmp = File.createTempFile(prefix, suffix)
    tmp.deleteOnExit()
    tmp
} catch (e: IOException) {
    throw   DexAnalyzeFailException("Fail to create temporary file", e)
}

