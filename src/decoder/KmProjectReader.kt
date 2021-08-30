import com.kinemaster.decoder.constants.*
import com.kinemaster.decoder.constants.FOURCC_FTRX
import com.kinemaster.decoder.constants.FOURCC_KHDR
import com.kinemaster.decoder.data.KMProtoBuffer
import com.kinemaster.decoder.data.KineFileInfo
import com.kinemaster.getSigBytes
import decoder.protobuffer.HeaderDelimitedInputStream
import decoder.protobuffer.KinemasterProjectWire
import java.io.BufferedInputStream
import java.io.File

class KmProjectReader(private val file: File) : IKmProjectReader {
    private var kineFileInfo: KineFileInfo? = null

    override fun getContentsList(): ArrayList<String> {
        val name = file.name

        if (name.endsWith(FILE_EXTENSION_KINE)) {
            if (kineFileInfo == null) {
                kineFileInfo = KineUnZipHelper.unzipKine(file)
            }

            return kineFileInfo?.getContentList() ?: arrayListOf()
        }

        return arrayListOf()
    }

    override fun getKMProtoBufferJson(): String {
        val name = file.name
        val inputStream = when {
            name.endsWith(FILE_EXTENSION_KMPROJECT) -> {
                file.inputStream()
            }
            name.endsWith(FILE_EXTENSION_KINE) -> {
                kineFileInfo = KineUnZipHelper.unzipKine(file)
                kineFileInfo?.getKMProjectInputStream()
            }
            else -> {
                null
            }
        }

        inputStream ?: return "invalid project format"


        val sig = ByteArray(4)
        val kmProtoBuffer: KMProtoBuffer = BufferedInputStream(inputStream).use { bis ->
            getSigBytes(bis, sig)

            val delimitedInputStream = HeaderDelimitedInputStream(bis)
            delimitedInputStream.parseFileHeader()
            lateinit var header: KinemasterProjectWire.KMProto.KMProjectHeader
            lateinit var timeline: KinemasterProjectWire.KMProto.KMProject

            while (delimitedInputStream.nextSection()) {
                val fourcc = delimitedInputStream.sectionFourCC

                if (fourcc == FOURCC_FTRX) break
                if (fourcc == FOURCC_KHDR) {
                    header =
                        KinemasterProjectWire.KMProto.KMProjectHeader.parseFrom(delimitedInputStream)
                }
                if (fourcc == FOURCC_TLIN) {
                    timeline = KinemasterProjectWire.KMProto.KMProject.parseFrom(delimitedInputStream)
                    break
                }
            }

            KMProtoBuffer(header, timeline)
        }

        var result = kmProtoBuffer.toString()
        val startIdx = result.indexOf("jpeg_thumbnail:")
        val endIdx = result.indexOf("project_uuid_lsb") - 1
        result = result.replaceRange(startIdx, endIdx, "")

        return result
    }

    override fun getKMProjectName(): String = file.name

}

interface IKmProjectReader {
    fun getContentsList(): ArrayList<String>
    fun getKMProtoBufferJson(): String
    fun getKMProjectName(): String
}