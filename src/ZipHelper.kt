package com.kinemaster

import com.kinemaster.constants.FILE_EXTENSION_KMPROJECT
import com.kinemaster.data.KineFileInfo
import io.ktor.http.content.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipHelper {
    fun unzipKine(fileItem: PartData.FileItem): KineFileInfo {
        val kineFileInfo = KineFileInfo(kmProjectInputStream = null)

        try {
            val zipInputStream = ZipInputStream(fileItem.streamProvider())
            var zipEntry: ZipEntry?

            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                if (zipEntry?.name?.endsWith(FILE_EXTENSION_KMPROJECT) == false) {
                    kineFileInfo.addContent(zipEntry?.name ?: "unknown")
                    continue
                }
                val name = zipEntry?.name ?: return kineFileInfo
                val file = File(name)
                file.writeBytes(zipInputStream.readAllBytes())
                kineFileInfo.setKMProjectInputStream(FileInputStream(file))
            }

        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            return kineFileInfo
        }

    }
}