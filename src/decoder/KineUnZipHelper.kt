import com.kinemaster.decoder.constants.FILE_EXTENSION_KMPROJECT
import com.kinemaster.decoder.data.KineFileInfo
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object KineUnZipHelper {
    fun unzipKine(file: File): KineFileInfo {
        val kineFileInfo = KineFileInfo(kmProjectInputStream = null)
        var tmpFile: File? = null

        try {
            val zipInputStream = ZipInputStream(file.inputStream())
            var zipEntry: ZipEntry?

            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                if (zipEntry?.name?.endsWith(FILE_EXTENSION_KMPROJECT) == false) {
                    kineFileInfo.addContent(zipEntry?.name ?: "unknown")
                    continue
                }
                val name = zipEntry?.name ?: return kineFileInfo
                tmpFile = File(name)
                zipInputStream.copyTo(tmpFile.outputStream())
                kineFileInfo.setKMProjectInputStream(FileInputStream(tmpFile))
            }

        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            tmpFile?.delete()
            return kineFileInfo
        }

    }
}