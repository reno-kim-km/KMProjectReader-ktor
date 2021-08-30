package com.kinemaster

import KmProjectReader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import java.io.File


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(AutoHeadResponse)

    routing {
        get("/") {
            call.respondHtml {
                body {
                    h1 { +"KMProject Reader" }
                    form(action = "/json", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                        input(type = InputType.file, name = "file")
                        input(type = InputType.submit)
                    }
                }
            }
        }
        // for test
        post("/json") {
            val multipart = call.receiveMultipart()
            var name = "Unknown"
            val sig = ByteArray(4)

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    var inputFile: File? = null
                    var resultFile: File? = null
                    try {
                        inputFile = File("/tmp/${part.originalFileName}")
                        part.streamProvider().copyTo(inputFile.outputStream())

                        val kmProjectReader = KmProjectReader(inputFile)
                        val kmProtoBuffer = kmProjectReader.getKMProtoBufferJson()
                        val contentsList = kmProjectReader.getContentsList()
                        val kmProjectName = kmProjectReader.getKMProjectName()

                        resultFile = File("/tmp/${kmProjectName}.txt")
                        resultFile.writeText(kmProtoBuffer)
                        call.response.header("Content-Disposition", "attachment; filename=\"${resultFile.name}\"")
                        call.respondFile(resultFile)
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        call.respondHtml {
                            body {
                                h1 { +"KMProject Json Download Fail" }
                                p {
                                    +"${exception.printStackTrace()}"
                                }
                            }
                        }
                    } finally {
                        inputFile?.delete()
                        resultFile?.delete()
                    }
                }
            }
        }
        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }
    }
}


