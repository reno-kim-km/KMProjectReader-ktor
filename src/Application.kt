package com.kinemaster

import KmProjectReader
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }
    }

    routing {
        get("/") {
            call.respondHtml {
                createHomeScreen()
            }
        }
        post("/json") {
            call.receiveMultipart().forEachPart { part ->
                createKMProtoJsonScreen(part)
            }
        }
        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }
    }
}

private fun HTML.createHomeScreen() {
    head {
        link(rel = "stylesheet", href = "/static/styles.css")
    }
    body {
        h1 { +"KMProject Reader" }
        br
        h2 {
            +"Please select Kine file"
        }
        br
        form(action = "/json", encType = FormEncType.multipartFormData, method = FormMethod.post) {
            input(type = InputType.file, name = "file")
            input(type = InputType.submit)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.createKMProtoJsonScreen(
    part: PartData
) {
    if (part is PartData.FileItem) {
        var tmpFile: File? = null
        try {
            tmpFile = File("/tmp/${part.originalFileName}")
            part.streamProvider().copyTo(tmpFile.outputStream())

            val kmProjectReader = KmProjectReader(tmpFile)
            val kineFileInfo = kmProjectReader.getKineFileInfo()

            val kmProjectName = kmProjectReader.getKMProjectName()
            val thumbnailSrc = kineFileInfo.kmProtoBuffer.getThumbnailSrc()
            val header = kineFileInfo.kmProtoBuffer.header.convertPrettyJson()
            val project = kineFileInfo.kmProtoBuffer.project.convertPrettyJson()
            val contentsList = kineFileInfo.contentList.convertPrettyJson()

            call.respondHtml {
                head {
                    link(rel = "stylesheet", href = "/static/styles.css")
                }
                body {
                    h1 {
                        +"KMProject Name"
                    }
                    p {
                        +kmProjectName
                    }
                    h1 {
                        +"KMProject Json Result"
                    }
                    h2 {
                        +"Project Header"
                    }
                    textArea {
                        readonly = true
                        rows = "20"
                        +header
                    }
                    h2 {
                        +"Project Thumbnail"
                    }
                    img {
                        src = thumbnailSrc
                    }
                    h2 {
                        +"Project Body"
                    }
                    textArea {
                        readonly = true
                        rows = "30"
                        +project
                    }
                    h2 {
                        +"KMProject Content list"
                    }
                    textArea {
                        rows = "10"
                        +contentsList
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            call.respondHtml {
                head {
                    link(rel = "stylesheet", href = "/static/styles.css")
                }
                body {
                    h1 { +"KMProject Json Download Fail" }
                    p {
                        +"${exception.printStackTrace()}"
                    }
                }
            }
        } finally {
            tmpFile?.delete()
        }
    }
}


fun Any.convertPrettyJson(): String {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(ByteArray::class.java, com.google.gson.JsonSerializer<ByteArray> { src, _, _ ->
            JsonPrimitive(String(src))
        })
        .registerTypeAdapter(ByteArray::class.java, JsonDeserializer<ByteArray> { json, _, _ ->
            if (json == null || json.asString == null) null
            else json.asString.encodeToByteArray()
        })
        .create()
    return gson.toJson(this)
}