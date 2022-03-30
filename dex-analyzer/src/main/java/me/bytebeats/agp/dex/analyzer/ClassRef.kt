package me.bytebeats.agp.dex.analyzer


/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/21 20:25
 * @Version 1.0
 * @Description TO-DO
 */
data class ClassRef(
    val name: String,
    val fields: MutableList<FieldRef> = mutableListOf(),
    val methods: MutableList<MethodRef> = mutableListOf()
) {
    fun addField(fieldRef: FieldRef) {
        fields.add(fieldRef)
    }

    fun addMethod(methodRef: MethodRef) {
        methods.add(methodRef)
    }

    fun fieldArray(): Array<FieldRef> = fields.toTypedArray()
    fun methodArray(): Array<MethodRef> = methods.toTypedArray()
}
