package com.kinemaster.decoder.data


import decoder.protobuffer.KinemasterProjectWire.KMProto
import decoder.protobuffer.KinemasterProjectWire.KMProto.KMProjectHeader
import java.util.zip.ZipEntry
import com.kinemaster.convertPrettyJson
import java.util.Base64


data class KineFileInfo(
    val contentList: List<ZipEntry> = arrayListOf(),
    val kmProtoBuffer: KMProtoBuffer,
)

data class KMProtoBuffer(
    val header: KMProjectHeader,
    val project: KMProto.KMProject
) {
    fun getThumbnailSrc(): String {
        val imgSrc = String(Base64.getEncoder().encode(header.jpegThumbnail.toByteArray()))
        return "data: image/png;base64,$imgSrc"
    }
}