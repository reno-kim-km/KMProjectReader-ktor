import com.kinemaster.decoder.constants.FILE_EXTENSION_KMPROJECT
import com.kinemaster.decoder.data.KineFileInfo
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object KineUnZipHelper {
    fun unzipKine(file: File): KineFileInfo {
        val kineFileInfo = KineFileInfo(kmProjectInputStream = null)

        try {
            val zipInputStream = ZipInputStream(file.inputStream())
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