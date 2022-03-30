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
 * @param type like "Ljava/lang/String;"
 */
data class FieldRef(val declaringClass: String, val name: String, val type: String) : HasDeclaringClass {
    override fun getDeclaringClassName(): String = declaringClass
}
