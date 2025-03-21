package com.system32.systemCore.utils

import java.awt.Color
import java.io.OutputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class DiscordUtil {
    companion object {
        fun convertToColor(r: Int, g: Int, b: Int): Color {
            return Color(r, g, b)
        }
        fun convertToColor(r: String, g: String, b: String): Color {
            return try{
                Color(r.toInt(), g.toInt(), b.toInt())
            } catch (e: NumberFormatException) {
                Color(255, 0, 0)
            }
        }
    }
}
