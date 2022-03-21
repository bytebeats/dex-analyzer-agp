package me.bytebeats.agp.dex.analyzer

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/21 20:20
 * @Version 1.0
 * @Description TO-DO
 */

/**
 * @param parameterTypes like [Ljava/lang/String;I]
 * @param returnType "V"
 *
 */
data class MethodRef(
    val declaredClass: String,
    val methodName: String,
    val parameterTypes: Array<String>,
    val returnType: String,
) : HasClassDeclared {
    override fun getDeclaredClassName(): String {
        return declaredClass
    }

    /**
     * like (Ljava/lang/String;I)V
     */
    fun descriptor(): String = descriptor(parameterTypes, returnType)

    private fun descriptor(parameterTypes: Array<String>, returnType: String): String {
        val builder = StringBuilder()
        builder.append("(")
        parameterTypes.forEach { type ->
            builder.append(type)
        }
        builder.append(")")
        builder.append(returnType)
        return builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MethodRef

        if (declaredClass != other.declaredClass) return false
        if (methodName != other.methodName) return false
        if (!parameterTypes.contentEquals(other.parameterTypes)) return false
        if (returnType != other.returnType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = declaredClass.hashCode()
        result = 31 * result + methodName.hashCode()
        result = 31 * result + parameterTypes.contentHashCode()
        result = 31 * result + returnType.hashCode()
        return result
    }
}
