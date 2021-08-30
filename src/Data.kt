package com.kinemaster

import com.google.gson.annotations.SerializedName

data class UserSession(val token: String)

data class UserInfo(
    val id: String,
    val name: String,
    val email: String,
    @SerializedName("verified_email")
    val isVerifiedEmail: Boolean
)

/**
 * UserInfo Example
 *
 * {
"id": "115304003783812593570",
"email": "reno.kim@kinemaster.com",
"verified_email": true,
"name": "Reno KIM",
"given_name": "Reno",
"family_name": "KIM",
"link": "https://plus.google.com/115304003783812593570",
"picture": "https://lh3.googleusercontent.com/a-/AOh14Gg3K1Pd0blW_BRGcbjLWu5rnspZmQAiDidnxSrC4w=s96-c",
"locale": "ko",
"hd": "kinemaster.com"
}
 * */
