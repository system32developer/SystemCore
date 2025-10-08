package com.system32dev.systemCore.utils

import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL

data class Debug(private val sections: MutableList<Section> = mutableListOf()) {
    fun addSection(name: String): Section {
        val section = Section(name)
        sections.add(section)
        return section
    }

    private fun buildContent(): String {
        val builder = StringBuilder()
        for (section in sections) {
            builder.appendLine("${section.name}:")
            for ((key, value) in section.data) {
                builder.appendLine("  $key: $value")
            }
            builder.appendLine()
        }
        return builder.toString()
    }

    fun send(): String {
        val content = buildContent()
        val url = URL("https://api.pastes.dev/post")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "text/yaml")
        connection.setRequestProperty("User-Agent", "DebugUploader (github.com/system32developer)")

        connection.outputStream.use { output ->
            output.write(content.toByteArray())
        }

        val responseText = connection.inputStream.bufferedReader().readText()
        connection.disconnect()

        val gson = Gson()
        val responseMap: Map<String, String> = gson.fromJson(responseText, Map::class.java) as Map<String, String>
        val key = responseMap["key"] ?: throw IllegalStateException("No key in response")

        return "https://pastes.dev/$key"
    }

    data class Section(val name: String) {
        val data = mutableMapOf<String, Any>()

        fun addData(key: String, value: Any) {
            data[key] = value
        }
    }
}
