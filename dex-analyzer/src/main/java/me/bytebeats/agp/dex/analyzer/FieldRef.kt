package me.bytebeats.agp.dex.analyzer

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/21 20:22
 * @Version 1.0
 * @Description TO-DO
 */

/**
 * @param fieldType like "Ljava/lang/String;"
 */
data class FieldRef(val declaredClass: String, val fieldName: String, val fieldType: String) : HasClassDeclared {
    override fun getDeclaredClassName(): String = declaredClass
}
