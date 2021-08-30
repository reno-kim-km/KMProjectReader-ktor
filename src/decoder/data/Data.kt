package com.kinemaster.decoder.data


import decoder.protobuffer.KinemasterProjectWire.KMProto
import decoder.protobuffer.KinemasterProjectWire.KMProto.KMProjectHeader
import java.io.InputStream


data class KineFileInfo(
    private val contentList: ArrayList<String> = arrayListOf(),
    private var kmProjectInputStream: InputStream?
) {
    fun addContent(name: String) {
        contentList.add(name)
    }

    fun getContentList() = contentList

    fun setKMProjectInputStream(inputStream: InputStream) {
        kmProjectInputStream = inputStream
    }

    fun getKMProjectInputStream(): InputStream? = kmProjectInputStream
}

data class KMProtoBuffer(
    val header: KMProjectHeader,
    val project: KMProto.KMProject
)