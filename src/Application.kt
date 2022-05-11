package com.kinemaster

import KmProjectReader
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.*
import java.io.File

const val HOST_URL = "https://kmproject-reader-ktor.herokuapp.com/"//"http://localhost:8080"

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
//    install(AutoHeadResponse)
//    install(Sessions) {
//        cookie<UserSession>("user_session")
//    }

    val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

//    install(Authentication) {
//        oauth("auth-oauth-google") {
//            urlProvider = { "$HOST_URL/callback" }
//            providerLookup = {
//                OAuthServerSettings.OAuth2ServerSettings(
//                    name = "google",
//                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
//                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
//                    requestMethod = HttpMethod.Post,
//                    clientId = "1024719495486-dpkhogop1csl1j56074s0tuq1ovoai34.apps.googleusercontent.com",
//                    clientSecret = "4aH_qwE5lHn5szMQTpnbpH6j",
//                    defaultScopes = listOf(
//                        "https://www.googleapis.com/auth/userinfo.profile",
//                        "https://www.googleapis.com/auth/userinfo.email"
//                    )
//                )
//            }
//            client = httpClient
//        }
//    }

    routing {
//        routing {
//            authenticate("auth-oauth-google") {
//                get("/login") {
//                    // Redirects to 'authorizeUrl' automatically
//                }
//
//                get("/callback") {
//                    val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
//                    call.sessions.set(UserSession(principal?.accessToken.toString()))
//                    call.respondRedirect("/checkKineMasterUser")
//                }
//            }
//        }
//
//        get("/checkKineMasterUser") {
//            val userSession: UserSession? = call.sessions.get<UserSession>()
//            if (userSession != null) {
//                val userInfo: UserInfo = httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo") {
//                    headers {
//                        append(HttpHeaders.Authorization, "Bearer ${userSession.token}")
//                    }
//                }
//                if (userInfo.email.contains("kinemaster.com")) {
//                    call.respondRedirect("/input")
//                    return@get
//                }
//            }
//
//            call.respondText("Sorry, you can't use this service (only KineMaster Employees")
//        }


//        get("/") {
//            call.respondHtml {
//                body {
//                    h1 { +"KMProject Reader" }
//                    div {
//                        +"please login first!"
//                        br
//                        a(href = "/login") {
//                            +"Google Sign-In"
//                        }
//                    }
//                }
//            }
//        }
        get("/") {
            call.respondHtml {
                body {
                    h1 { +"KMProject Reader" }
                    br
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


