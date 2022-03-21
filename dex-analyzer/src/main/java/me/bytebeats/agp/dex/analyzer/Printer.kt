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


}