package me.bytebeats.agp.dex.analyzer


/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/21 20:25
 * @Version 1.0
 * @Description TO-DO
 */
object Printer {
    private const val IN0 = ""
    private const val IN1 = "  "
    private const val IN2 = "    "
    private const val IN3 = "      "
    private const val IN4 = "        "

    private fun generateHeader0(fileName: String?, format: String) {
        if (format == "brief") {
            println("File: $fileName")
        } else if (format == "xml") {
            if (fileName.isNullOrBlank()) {
                println("$IN0<external>")
            } else {
                println("$IN0<external file=\"$fileName\">")
            }
        } else {
            /* should've been trapped in arg handler */
            throw RuntimeException("Unknown output format")
        }
    }

    fun generateFirstHeader(fileName: String?, format: String) {
        generateHeader0(fileName, format)
    }

    fun generateHeader(fileName: String?, format: String) {
        println()
        generateHeader0(fileName, format)
    }

    fun generateFooter(format: String) {
        if (format == "brief") {
            // do nothing here
        } else if (format == "xml") {
            println("</external>")
        } else {
            /* should've been trapped in arg handler */
            throw RuntimeException("Unknown output format")
        }
    }

    fun generate(
        parser: DexParser, format: String,
        onlyPrintClass: Boolean
    ) {
        if (format == "brief") {
            printBrief(parser, onlyPrintClass)
        } else if (format == "xml") {
            printXml(parser, onlyPrintClass)
        } else {
            /* should've been trapped in arg handler */
            throw RuntimeException("unknown output format")
        }
    }

    /**
     * Prints the data in a simple human-readable format.
     */
    fun printBrief(parser: DexParser, onlyPrintClass: Boolean) {
        val externClassRefs = parser.getExternalReferences()
        printClassRefs(externClassRefs, onlyPrintClass)
        if (!onlyPrintClass) {
            printFieldRefs(externClassRefs)
            printMethodRefs(externClassRefs)
        }
    }

    /**
     * Prints the list of classes in a simple human-readable format.
     */
    fun printClassRefs(classes: Array<ClassRef>, onlyPrintClass: Boolean) {
        if (!onlyPrintClass) {
            println("Classes:")
        }
        for (i in classes.indices) {
            val ref = classes[i]
            println(descriptorToDot(ref.name))
        }
    }

    /**
     * Prints the list of fields in a simple human-readable format.
     */
    fun printFieldRefs(classes: Array<ClassRef>) {
        println("\nFields:")
        for (i in classes.indices) {
            val fields = classes[i].fieldArray()
            for (ref in fields) {
                println(descriptorToDot(ref.getDeclaringClassName()) + "." + ref.name + " : " + ref.type)
            }
        }
    }

    /**
     * Prints the list of methods in a simple human-readable format.
     */
    fun printMethodRefs(classes: Array<ClassRef>) {
        println("\nMethods:")
        for (i in classes.indices) {
            val methods = classes[i].methodArray()
            for (ref in methods) {
                println(descriptorToDot(ref.declaringClassName) + "." + ref.name + " : " + ref.descriptor())
            }
        }
    }

    /**
     * Prints the output in XML format.
     *
     * We shouldn't need to XML-escape the field/method info.
     */
    fun printXml(parser: DexParser, onlyPrintClass: Boolean) {
        val externClassRefs = parser.getExternalReferences()

        /*
         * Iterate through externClassRefs.  For each class, dump all the matching fields and methods.
         */
        var prevPackage: String? = null
        for (i in externClassRefs.indices) {
            val ref = externClassRefs[i]
            val declaredClassName = ref.name
            val className = classNameOnly(declaredClassName)
            val packageName = packageNameOnly(declaredClassName)

            /*
             * If we're in a different package, emit the appropriate tags.
             */if (packageName != prevPackage) {
                if (prevPackage != null) {
                    println("$IN1</package>")
                }
                println("$IN1<package name=\"$packageName\">")
                prevPackage = packageName
            }
            println("$IN2<class name=\"$className\">")
            if (!onlyPrintClass) {
                printXmlFields(ref)
                printXmlMethods(ref)
            }
            println("$IN2</class>")
        }
        if (prevPackage != null) println("$IN1</package>")
    }

    /**
     * Prints the externally-visible fields in XML format.
     */
    private fun printXmlFields(classRef: ClassRef) {
        val fields = classRef.fieldArray()
        for (fieldRef in fields) {
            println("$IN3<field name=\"${fieldRef.getDeclaringClassName()}\" type=\"${descriptorToDot(fieldRef.type)}\"/>")
        }
    }

    /**
     * Prints the externally-visible methods in XML format.
     */
    private fun printXmlMethods(classRef: ClassRef) {
        val methods = classRef.methodArray()
        for (i in methods.indices) {
            val methodRef = methods[i]
            val declaredClassName = methodRef.declaringClassName
            val isConstructor = methodRef.name == "<init>"
            if (isConstructor) {
                // use class name instead of method name
                println("$IN3<constructor name=\"${classNameOnly(declaredClassName)}\">")
            } else {
                println("$IN3<method name=\"${methodRef.name}\" return=\"${descriptorToDot(methodRef.returnType)}\">")
            }
            val args = methodRef.parameterTypes
            for (j in args.indices) {
                println("$IN4<parameter type=\"${descriptorToDot(args[j])}\"/>")
            }
            if (isConstructor) {
                println("$IN3</constructor>")
            } else {
                println("$IN3</method>")
            }
        }
    }


    /*
   * =======================================================================
   *      Utility functions
   * =======================================================================
   */


    /**
     * Converts a single-character primitive type into its human-readable
     * equivalent.
     */
    fun primitiveTypeLabel(typeChar: Char): String {
        /* primitive type; substitute human-readable name in */
        return when (typeChar) {
            'B' -> "byte"
            'C' -> "char"
            'D' -> "double"
            'F' -> "float"
            'I' -> "int"
            'J' -> "long"
            'S' -> "short"
            'V' -> "void"
            'Z' -> "boolean"
            else -> {
                /* huh? */System.err.println("Unexpected class char $typeChar")
                assert(false)
                "UNKNOWN"
            }
        }
    }

    /**
     * Converts a type descriptor to human-readable "dotted" form.  For
     * example, "Ljava/lang/String;" becomes "java.lang.String", and
     * "[I" becomes "int[].
     */
    fun descriptorToDot(descriptor: String): String {
        var d8R = descriptor
        var targetLen = d8R.length
        var offset = 0
        var arrayDepth = 0

        /* strip leading [s; will be added to end */
        while (targetLen > 1 && d8R[offset] == '[') {
            offset++
            targetLen--
        }
        arrayDepth = offset
        if (targetLen == 1) {
            d8R = primitiveTypeLabel(d8R[offset])
            offset = 0
            targetLen = d8R.length
        } else {
            /* account for leading 'L' and trailing ';' */
            if (targetLen >= 2 && d8R[offset] == 'L' && d8R[offset + targetLen - 1] == ';') {
                targetLen -= 2 /* two fewer chars to copy */
                offset++ /* skip the 'L' */
            }
        }
        val buf = CharArray(targetLen + arrayDepth * 2)

        /* copy class name over */
        var i = 0
        while (i < targetLen) {
            val ch = d8R[offset + i]
            buf[i] = if (ch == '/') '.' else ch
            i++
        }

        /* add the appropriate number of brackets for arrays */while (arrayDepth-- > 0) {
            buf[i++] = '['
            buf[i++] = ']'
        }
        assert(i == buf.size)
        return String(buf)
    }

    /**
     * Extracts the class name from a type descriptor.
     */
    fun classNameOnly(typeName: String): String {
        val dotted = descriptorToDot(typeName)
        val start = dotted!!.lastIndexOf(".")
        return if (start < 0) {
            dotted
        } else {
            dotted.substring(start + 1)
        }
    }

    /**
     * Extracts the package name from a type descriptor, and returns it in
     * dotted form.
     */
    fun packageNameOnly(typeName: String): String {
        val dotted = descriptorToDot(typeName)
        val end = dotted!!.lastIndexOf('.')
        return if (end < 0) {
            /* lives in default package */
            ""
        } else {
            dotted.substring(0, end)
        }
    }


}