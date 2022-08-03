import com.kinemaster.decoder.KineUnZipHelper
import com.kinemaster.decoder.data.KineFileInfo
import java.io.File

class KmProjectReader(private val file: File) : IKmProjectReader {
    override fun getKineFileInfo(): KineFileInfo {
        return KineUnZipHelper.unzipKine(file)
    }

    override fun getKMProjectName(): String = file.name

}

interface IKmProjectReader {
    fun getKineFileInfo(): KineFileInfo
    fun getKMProjectName(): String
}