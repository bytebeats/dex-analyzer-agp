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
            val DEX_FILE_MAGIC_v035: ByteArray = "dex\n035\u0000".toByteArray(Charsets.US_ASCII)

            // Dex version 036 skipped because of an old dalvik bug on some versions
            // of android where dex files with that version number would erroneously
            // be accepted and run. See: art/runtime/dex_file.cc
            // V037 was introduced in API LEVEL 24
            val DEX_FILE_MAGIC_v037: ByteArray = "dex\n037\u0000".toByteArray(Charsets.US_ASCII)

            // V038 was introduced in API LEVEL 26
            val DEX_FILE_MAGIC_v038: ByteArray = "dex\n038\u0000".toByteArray(Charsets.US_ASCII)

            // V039 was introduced in API LEVEL 28
            val DEX_FILE_MAGIC_v039: ByteArray = "dex\n039\u0000".toByteArray(Charsets.US_ASCII)
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