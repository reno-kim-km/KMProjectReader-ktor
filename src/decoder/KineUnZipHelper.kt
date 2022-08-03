package com.kinemaster.decoder

import com.kinemaster.decoder.constants.*
import com.kinemaster.decoder.constants.FOURCC_FTRX
import com.kinemaster.decoder.constants.FOURCC_KHDR
import com.kinemaster.decoder.constants.FOURCC_TLIN
import com.kinemaster.decoder.data.KMProtoBuffer
import com.kinemaster.decoder.data.KineFileInfo
import decoder.protobuffer.KinemasterProjectWire
import com.kinemaster.getSigBytes
import decoder.protobuffer.HeaderDelimitedInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object KineUnZipHelper {

    private fun getContentList(file: File): List<ZipEntry> {
        return try {
            val zipInputStream = ZipInputStream(file.inputStream())
            var zipEntry: ZipEntry?
            val contentList = mutableListOf<ZipEntry>()

            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                if (zipEntry?.name?.endsWith(FILE_EXTENSION_KMPROJECT) == false) {
                    val _zipEntry = zipEntry ?: continue
                    contentList.add(_zipEntry)
                    continue
                }
            }
            contentList
        } catch (exception: Exception) {
            exception.printStackTrace()
            return emptyList()
        }
    }

    private fun getKineUnzipInputStream(file: File): InputStream? {
        var tmpFile: File? = null

        return try {
            val zipInputStream = ZipInputStream(file.inputStream())
            var zipEntry: ZipEntry?

            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                if (zipEntry?.name?.endsWith(FILE_EXTENSION_KMPROJECT) == false) {
                    continue
                }
                val name = zipEntry?.name ?: return null
                tmpFile = File(name)
                zipInputStream.copyTo(tmpFile.outputStream())
            }
            FileInputStream(tmpFile)
        } catch (exception: Exception) {
            exception.printStackTrace()
            return null
        } finally {
            tmpFile?.delete()
        }
    }

    fun unzipKine(file: File): KineFileInfo {
        val name = file.name
        val contentList = getContentList(file)
        val zipInputStream = when {
            name.endsWith(FILE_EXTENSION_KMPROJECT) -> {
                file.inputStream()
            }
            name.endsWith(FILE_EXTENSION_KINE) -> {
                getKineUnzipInputStream(file)
            }
            else -> {
                null
            }
        }

        zipInputStream ?: throw IllegalStateException("the file is invalid. the extension of file should be $FILE_EXTENSION_KINE or $FILE_EXTENSION_KMPROJECT")

        val sig = ByteArray(4)
        val kmProtoBuffer: KMProtoBuffer = BufferedInputStream(zipInputStream).use { bis ->
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

        return KineFileInfo(contentList, kmProtoBuffer)
    }
}