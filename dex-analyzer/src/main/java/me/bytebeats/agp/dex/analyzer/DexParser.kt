package me.bytebeats.agp.dex.analyzer

import me.bytebeats.agp.dex.analyzer.DexParser.HeaderItem.Companion.ENDIAN_CONSTANT
import me.bytebeats.agp.dex.analyzer.DexParser.HeaderItem.Companion.REVERSE_ENDIAN_CONSTANT
import me.bytebeats.agp.dex.analyzer.DexParser.HeaderItem.Companion.verifyMagic
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

class DexParser(private val dexFile: RandomAccessFile) {
    private lateinit var mHeaderItem: HeaderItem
    private lateinit var mStrings: Array<String>// strings from string_data_*
    private lateinit var mTypeIds: Array<TypeIdItem>
    private lateinit var mProtoIds: Array<ProtoIdItem>
    private lateinit var mFieldIds: Array<FieldIdItem>
    private lateinit var mMethodIds: Array<MethodIdItem>
    private lateinit var mClassDefs: Array<ClassDefItem>

    private val bufHelper = ByteArray(4)
    private var isBigEndian = false

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

        // magic number
        seek(0)
        val magic = ByteArray(8)
        readBytes(magic)
        if (!verifyMagic(magic)) {
            println("Magic number is wrong. Are you sure this is a DEX file?")
            throw DexNotParsedException("Wrong magic number")
        }

        /*
         * Read the endian tag, so we properly swap things as we read
         * them from here on.
         */
        //endian tag
        seek(8 + 4 + 20 + 4 + 4)
        mHeaderItem.endianTag = readInt()
        if (mHeaderItem.endianTag == ENDIAN_CONSTANT) {
            // do nothing here
            isBigEndian = false
        } else if (mHeaderItem.endianTag == REVERSE_ENDIAN_CONSTANT) {
            isBigEndian = true
        } else {
            println("Endian constant has unexpected value: ${Integer.toHexString(mHeaderItem.endianTag)}")
            throw DexNotParsedException("Unexpected endian constant: ${Integer.toHexString(mHeaderItem.endianTag)}")
        }
        //magic, checksum, signature
        seek(8 + 4 + 20)
        mHeaderItem.fileSize = readInt()
        mHeaderItem.headerSize = readInt()
        mHeaderItem.endianTag = readInt()
        mHeaderItem.linkSize = readInt()
        mHeaderItem.linkOff = readInt()
        mHeaderItem.mapOff = readInt()
        mHeaderItem.stringIdsSize = readInt()
        mHeaderItem.stringIdsOff = readInt()
        mHeaderItem.typeIdsSize = readInt()
        mHeaderItem.typeIdsOff = readInt()
        mHeaderItem.protoIdsSize = readInt()
        mHeaderItem.protoIdsOff = readInt()
        mHeaderItem.fieldIdsSize = readInt()
        mHeaderItem.fieldIdsOff = readInt()
        mHeaderItem.methodIdsSize = readInt()
        mHeaderItem.methodIdsOff = readInt()
        mHeaderItem.classDefsSize = readInt()
        mHeaderItem.classDefsOff = readInt()
        mHeaderItem.dataSize = readInt()
        mHeaderItem.dataOff = readInt()
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
        val count = mHeaderItem.stringIdsSize
        val strIdxOffsets = IntArray(count)

        println("Try to read $count Strings")

        seek(mHeaderItem.stringIdsOff)
        for (i in 0 until count) {
            strIdxOffsets[i] = readInt()
        }
        seek(strIdxOffsets[0])
        mStrings = Array(count) { i ->
            seek(strIdxOffsets[i])
            readString()
        }
    }

    /**
     * Loads the type ID array.
     */
    @Throws(IOException::class)
    internal fun parseTypeIds() {
        val count = mHeaderItem.typeIdsSize
        println("Try to read $count typeIds")
        seek(mHeaderItem.typeIdsOff)
        mTypeIds = Array(count) {
            TypeIdItem(descriptorIdx = readInt(), true)
        }
    }

    /**
     * Loads the proto ID array.
     */
    @Throws(IOException::class)
    internal fun parseProtoIds() {
        val count = mHeaderItem.protoIdsSize
        println("Try to read $count protoIds")
        seek(mHeaderItem.protoIdsOff)
        /*
         * Read the proto ID items.
         */
        mProtoIds = Array(count) {
            ProtoIdItem(shortyIdx = readInt(), returnTypeIdx = readInt(), parametersOff = readInt(), intArrayOf())
        }

        /*
         * Go back through and read the type lists.
         */
        for (protoId in mProtoIds) {
            val offset = protoId.parametersOff
            if (offset == 0) {
                protoId.types = IntArray(0)
            } else {
                seek(offset)
                val size = readInt()//count of entries in list
                protoId.types = IntArray(size) {
                    readShort().toInt() and 0xffff
                }
            }
        }
    }

    /**
     * Loads the field ID array.
     */
    @Throws(IOException::class)
    internal fun parseFieldIds() {
        val count = mHeaderItem.fieldIdsSize
        println("Try to read $count fieldIds")
        seek(mHeaderItem.fieldIdsOff)
        mFieldIds = Array(count) {
            FieldIdItem(
                classIdx = readShort().toInt() and 0xffff,
                typeIdx = readShort().toInt() and 0xffff,
                nameIdx = readInt()
            )
        }
    }

    /**
     * Loads the method ID array.
     */
    @Throws(IOException::class)
    internal fun parseMethodIds() {
        val count = mHeaderItem.methodIdsSize
        println("Try to read $count methodIds")
        seek(mHeaderItem.methodIdsOff)
        mMethodIds = Array(count) {
            MethodIdItem(
                classIdx = readShort().toInt() and 0xffff,
                protoIdx = readShort().toInt() and 0xffff,
                nameIdx = readInt()
            )
        }
    }

    /**
     * Loads the class defs array.
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
        dexFile.seek(position.toLong())
    }

    /**
     * Fills the buffer by reading bytes from the DEX file.
     */
    @Throws(IOException::class)
    internal fun readBytes(buffer: ByteArray) {
        dexFile.readFully(buffer)
    }

    /**
     * Reads a single signed byte value.
     */
    @Throws(IOException::class)
    internal fun readByte(): Byte {
        dexFile.readFully(bufHelper, 0, 1)
        return bufHelper[0]
    }

    /**
     * Reads a signed 16-bit integer, byte-swapping if necessary.
     */
    @Throws(IOException::class)
    internal fun readShort(): Short {
        dexFile.readFully(bufHelper, 0, 2)
        return if (isBigEndian) ((bufHelper[1].toInt() and 0xff) or ((bufHelper[0].toInt() and 0xff) shl 8)).toShort()
        else ((bufHelper[0].toInt() and 0xff) or ((bufHelper[1].toInt() and 0xff) shl 8)).toShort()
    }

    /**
     * Reads a signed 32-bit integer, byte-swapping if necessary.
     */
    @Throws(IOException::class)
    internal fun readInt(): Int {
        dexFile.readFully(bufHelper)
        return if (isBigEndian)
            ((bufHelper[3].toInt() and 0xff) or ((bufHelper[2].toInt() and 0xff) shl 8) or ((bufHelper[1].toInt() and 0xff) shl 16) or ((bufHelper[0].toInt() and 0xff) shl 24))
        else
            ((bufHelper[0].toInt() and 0xff) or ((bufHelper[1].toInt() and 0xff) shl 8) or ((bufHelper[2].toInt() and 0xff) shl 16) or ((bufHelper[3].toInt() and 0xff) shl 24))
    }

    /**
     * Reads a variable-length unsigned LEB128 value.
     * Does not attempt to verify that the value is valid.
     *
     * @throws java.io.EOFException if we run off the end of the file
     */
    @Throws(IOException::class)
    internal fun readUnsignedLeb128(): Int {
        var result = 0
        var b: Byte
        do {
            b = readByte()
            result = (result shl 7) or (b.toInt() and 0x7f)
        } while (b < 0)
        return result
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
        val utf16Len = readUnsignedLeb128()
        val bytes = ByteArray(utf16Len * 3) //worst case
        var idx = 0
        for (i in bytes.indices) {
            idx = i
            val b = readByte()
            if (b.toInt() == 0) break
            bytes[idx] = b
        }
        return String(bytes, 0, idx, Charsets.UTF_8)
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
        var linkSize = 0
        var linkOff = 0
        var mapOff = 0
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
        var dataSize = 0
        var dataOff = 0

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
    data class TypeIdItem(val descriptorIdx: Int, var internal: Boolean)

    /**
     * Holds the contents of a proto_id_item.
     *
     * @param shortyIdx index into string_ids
     * @param returnTypeIdx index into type_ids
     * @param parametersOff file offset to a type_list
     * @param types contents of type list
     */
    data class ProtoIdItem(
        val shortyIdx: Int,
        val returnTypeIdx: Int,
        val parametersOff: Int,
        var types: IntArray
    )

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