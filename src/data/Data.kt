package com.kinemaster.data


import protobuffer.KinemasterProjectWire.KMProto.KMProject
import protobuffer.KinemasterProjectWire.KMProto.KMProjectHeader
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
    val project: KMProject
)