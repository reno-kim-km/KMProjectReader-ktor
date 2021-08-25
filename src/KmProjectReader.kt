package com.kinemaster

import com.kinemaster.constants.*
import com.kinemaster.data.KMProtoBuffer
import com.kinemaster.data.KineFileInfo
import io.ktor.http.content.*
import protobuffer.HeaderDelimitedInputStream
import protobuffer.KinemasterProjectWire
import java.io.BufferedInputStream

class KtorKmProjectReader(private val fileItem: PartData.FileItem) : KmProjectReader {
    private var kineFileInfo: KineFileInfo? = null

    override fun getContentsList(): ArrayList<String> {
        val name = fileItem.originalFileName!!

        if (name.endsWith(FILE_EXTENSION_KINE)) {
            if (kineFileInfo == null) {
                kineFileInfo = ZipHelper.unzipKine(fileItem)
            }

            return kineFileInfo?.getContentList() ?: arrayListOf()
        }

        return arrayListOf()
    }

    override fun getKMProtoBufferJson(): String {
        val name = fileItem.originalFileName!!
        val inputStream = when {
            name.endsWith(FILE_EXTENSION_KMPROJECT) -> {
                fileItem.streamProvider()
            }
            name.endsWith(FILE_EXTENSION_KINE) -> {
                kineFileInfo = ZipHelper.unzipKine(fileItem)
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

    override fun getKMProjectName(): String = fileItem.originalFileName!!

}

interface KmProjectReader {
    fun getContentsList(): ArrayList<String>
    fun getKMProtoBufferJson(): String
    fun getKMProjectName(): String
}