package me.bytebeats.agp.dex.analyzer

import java.io.IOException
import java.io.RandomAccessFile


/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/3/21 20:58
 * @Version 1.0
 * @Description TO-DO
 */

class Dex(private val dexFile: RandomAccessFile) {
    private lateinit var mHeaderItem: HeaderItem
    private lateinit var mStrings: Array<String>// strings from string_data_*
    private lateinit var mTypeIds: Array<TypeIdItem>
    private lateinit var mProtoIds: Array<ProtoIdItem>
    private lateinit var mFieldIds: Array<FieldIdItem>
    private lateinit var mMethodIds: Array<MethodIdItem>
    private lateinit var mClassDefs: Array<ClassDefItem>

    private val tmpBuf = ByteArray(4)
    private val isBigEndian = false

    /**
     * Loads the contents of the DEX file into our data structures.
     *
     * @throws IOException if we encounter a problem while reading
     * @throws DexNotParsedException if the DEX contents look bad
     */
    @Throws(IOException::class)
    fun parse() {
        parseHeaderItem()

        parseStrings()
        parseTypeIds()
        parseProtoIds()
        parseFieldIds()
        parseMethodIds()
        parseClassDefs()

        markInternalClasses()
    }

    /**
     * Parses the interesting bits out of the header.
     */
    @Throws(IOException::class)
    internal fun parseHeaderItem() {
        mHeaderItem = HeaderItem()
    }

    /**
     * Loads the string table out of the DEX.
     *
     * First we read all the string_id_items, then we read all the
     * string_data_item.  Doing it this way should allow us to avoid
     * seeking around in the file.
     */
    @Throws(IOException::class)
    internal fun parseStrings() {
    }

    /**
     * Loads the type ID list.
     */
    @Throws(IOException::class)
    internal fun parseTypeIds() {
    }

    /**
     * Loads the proto ID list.
     */
    @Throws(IOException::class)
    internal fun parseProtoIds() {
    }

    /**
     * Loads the field ID list.
     */
    @Throws(IOException::class)
    internal fun parseFieldIds() {
    }

    /**
     * Loads the method ID list.
     */
    @Throws(IOException::class)
    internal fun parseMethodIds() {
    }

    /**
     * Loads the class defs list.
     */
    @Throws(IOException::class)
    internal fun parseClassDefs() {
    }

    /**
     * Sets the "internal" flag on type IDs which are defined in the
     * DEX file or within the VM (e.g. primitive classes and arrays).
     */
    internal fun markInternalClasses() {

    }

    /*
  * =======================================================================
  *      Queries
  * =======================================================================
  */

    /**
     * Returns the class name, given an index into the type_ids table.
     */
    private fun classNameFromTypeIndex(idx: Int): String {
        return ""
    }

    /**
     * Returns an array of method argument type strings, given an index
     * into the proto_ids table.
     */
    private fun argArrayFromProtoIndex(idx: Int): Array<String> {
        return emptyArray()
    }

    /**
     * Returns a string representing the method's return type, given an
     * index into the proto_ids table.
     */
    private fun returnTypeFromProtoIndex(idx: Int): String {
        return ""
    }

    /**
     * Returns an array with all the class references that don't
     * correspond to classes in the DEX file.  Each class reference has
     * a list of the referenced fields and methods associated with
     * that class.
     */
    private fun getExternalReferences(): Array<ClassRef> {
        return emptyArray()
    }

    /**
     * Runs through the list of field references, inserting external
     * references into the appropriate ClassRef.
     */
    private fun addExternalFieldReferences(refs: Array<ClassRef>) {

    }

    /**
     * Runs through the list of method references, inserting external
     * references into the appropriate ClassRef.
     */
    private fun addExternalMethodReferences(refs: Array<ClassRef>) {

    }

    /*
    * BEGIN MODIFIED SECTION
    */

    /**
     * Returns the array of all method references.
     * @return method references
     */
    fun getMethodRefs(): Array<MethodRef> {
        return emptyArray()
    }

    /**
     * Returns the array of all field references.
     * @return field references
     */
    fun getFieldRefs(): Array<FieldRef> {
        return emptyArray()
    }

    /*
     * END MODIFIED SECTION
     */

    /*
    * =======================================================================
    *      Basic I/O functions
    * =======================================================================
    */

    /**
     * Seeks the DEX file to the specified absolute position.
     */
    @Throws(IOException::class)
    internal fun seek(position: Int) {

    }

    /**
     * Fills the buffer by reading bytes from the DEX file.
     */
    @Throws(IOException::class)
    internal fun readBytes(buffer: ByteArray) {

    }

    /**
     * Reads a single signed byte value.
     */
    @Throws(IOException::class)
    internal fun readByte(): Byte {
        return 0.toByte()
    }

    /**
     * Reads a signed 16-bit integer, byte-swapping if necessary.
     */
    @Throws(IOException::class)
    internal fun readShort(): Short {
        return 0.toShort()
    }

    /**
     * Reads a signed 32-bit integer, byte-swapping if necessary.
     */
    @Throws(IOException::class)
    internal fun readInt(): Int {
        return 0
    }

    /**
     * Reads a variable-length unsigned LEB128 value.  Does not attempt to
     * verify that the value is valid.
     *
     * @throws java.io.EOFException if we run off the end of the file
     */
    @Throws(IOException::class)
    internal fun readUnsignedLeb128() {

    }

    /**
     * Reads a UTF-8 string.
     *
     * We don't know how long the UTF-8 string is, so we have to read one
     * byte at a time.  We could make an educated guess based on the
     * utf16_size and seek back if we get it wrong, but seeking backward
     * may cause the underlying implementation to reload I/O buffers.
     */
    @Throws(IOException::class)
    internal fun readString(): String {
        return ""
    }


    /*
     * =======================================================================
     *      Internal "structure" declarations
     * =======================================================================
     */

    /**
     * Holds the contents of a header_item.
     */
    internal class HeaderItem {
        var fileSize = 0
        var headerSize = 0
        var endianTag = 0
        var stringIdsSize = 0
        var stringIdsOff = 0
        var typeIdsSize = 0
        var typeIdsOff = 0
        var protoIdsSize = 0
        var protoIdsOff = 0
        var fieldIdsSize = 0
        var fieldIdsOff = 0
        var methodIdsSize = 0
        var methodIdsOff = 0
        var classDefsSize = 0
        var classDefsOff = 0

        companion object {
            /* expected magic values */
            private val DEX_FILE_MAGIC_v035: ByteArray = "dex\n035\u0000".toByteArray(Charsets.US_ASCII)

            // Dex version 036 skipped because of an old dalvik bug on some versions
            // of android where dex files with that version number would erroneously
            // be accepted and run. See: art/runtime/dex_file.cc
            // V037 was introduced in API LEVEL 24
            private val DEX_FILE_MAGIC_v037: ByteArray = "dex\n037\u0000".toByteArray(Charsets.US_ASCII)

            // V038 was introduced in API LEVEL 26
            private val DEX_FILE_MAGIC_v038: ByteArray = "dex\n038\u0000".toByteArray(Charsets.US_ASCII)

            // V039 was introduced in API LEVEL 28
            private val DEX_FILE_MAGIC_v039: ByteArray = "dex\n039\u0000".toByteArray(Charsets.US_ASCII)
            const val ENDIAN_CONSTANT = 0x12345678
            const val REVERSE_ENDIAN_CONSTANT = 0x78563412

            fun verifyMagic(magic: ByteArray): Boolean {
                return magic.contentEquals(DEX_FILE_MAGIC_v035)
                        || magic.contentEquals(DEX_FILE_MAGIC_v037)
                        || magic.contentEquals(DEX_FILE_MAGIC_v038)
                        || magic.contentEquals(DEX_FILE_MAGIC_v039)
            }
        }
    }


    /**
     * Holds the contents of a type_id_item.
     *
     * This is chiefly a list of indices into the string table.  We need
     * some additional bits of data, such as whether the type ID
     * represents a class defined in this DEX, so we use an object for
     * each instead of a simple integer.  (Could use a parallel array, but
     * since this is a desktop app it's not essential.)
     *
     * @param descriptorIdx index into string_ids
     * @param internal defined within this DEX file?
     */
    data class TypeIdItem(val descriptorIdx: Int, val internal: Boolean)

    /**
     * Holds the contents of a proto_id_item.
     *
     * @param shortyIdx index into string_ids
     * @param returnTypeIdx index into type_ids
     * @param parametersOff file offset to a type_list
     * @param types contents of type list
     */
    data class ProtoIdItem(val shortyIdx: Int, val returnTypeIdx: Int, val parametersOff: Int, val types: IntArray)

    /**
     * Holds the contents of a field_id_item.
     * @param classIdx index into type_ids (defining class)
     * @param typeIdx index into type_ids (field type)
     * @param nameIdx index into string_ids
     */
    data class FieldIdItem(val classIdx: Int, val typeIdx: Int, val nameIdx: Int)

    /**
     * Holds the contents of a method_id_item.
     * @param classIdx index into type_ids
     * @param protoIdx index into proto_ids
     * @param nameIdx index into string_ids
     */
    data class MethodIdItem(val classIdx: Int, val protoIdx: Int, val nameIdx: Int)

    /**
     * Holds the contents of a class_def_item.
     *
     * We don't really need a class for this, but there's some stuff in
     * the class_def_item that we might want later.
     *
     * @param classIdx index into type_ids
     */
    data class ClassDefItem(val classIdx: Int)

}