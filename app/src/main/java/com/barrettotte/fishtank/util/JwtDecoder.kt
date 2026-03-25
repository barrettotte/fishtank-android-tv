package com.barrettotte.fishtank.util

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject

/** Utility for decoding JWT tokens to extract claims like user ID. */
object JwtDecoder {

    /** Extract the "sub" (subject/user ID) claim from a JWT access token. */
    fun getUserId(jwt: String): String? {
        return try {
            val parts = jwt.split(".")
            if (parts.size < 2) {
                return null
            }

            // Decode the payload (second part) from base64url
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val json = Gson().fromJson(payload, JsonObject::class.java)
            json.get("sub")?.asString

        } catch (e: Exception) {
            null
        }
    }
}
