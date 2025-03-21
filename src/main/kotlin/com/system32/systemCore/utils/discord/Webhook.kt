package com.system32.systemCore.utils.discord

import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class Webhook (private val url: String) {
    private var content: String? = null
    private var username: String? = null
    private var avatarUrl: String? = null
    private var tts: Boolean = false
    private val embeds = mutableListOf<EmbedObject>()

    fun setContent(content: String) {
        this.content = content
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun setAvatarUrl(avatarUrl: String) {
        this.avatarUrl = avatarUrl
    }

    fun setTts(tts: Boolean) {
        this.tts = tts
    }

    fun addEmbed(embed: EmbedObject) {
        this.embeds.add(embed)
    }

    fun execute() {
        require(content != null || embeds.isNotEmpty()) { "Set content or add at least one EmbedObject" }

        val json = JSONObject().apply {
            put("content", content)
            put("username", username)
            put("avatar_url", avatarUrl)
            put("tts", tts)
        }

        if (embeds.isNotEmpty()) {
            val embedObjects = embeds.map { embed ->
                JSONObject().apply {
                    put("title", embed.title)
                    put("description", embed.description)
                    put("url", embed.url)
                    embed.color?.let { color ->
                        val rgb = (color.red shl 16) + (color.green shl 8) + color.blue
                        put("color", rgb)
                    }
                    embed.footer?.let { put("footer", JSONObject().apply { put("text", it.text); put("icon_url", it.iconUrl) }) }
                    embed.image?.let { put("image", JSONObject().apply { put("url", it.url) }) }
                    embed.thumbnail?.let { put("thumbnail", JSONObject().apply { put("url", it.url) }) }
                    embed.author?.let { put("author", JSONObject().apply { put("name", it.name); put("url", it.url); put("icon_url", it.iconUrl) }) }
                    put("fields", embed.fields.map { field -> JSONObject().apply {
                        put("name", field.name)
                        put("value", field.value)
                        put("inline", field.inline)
                    } })
                }
            }
            json.put("embeds", embedObjects)
        }

        with(URL(url).openConnection() as HttpsURLConnection) {
            addRequestProperty("Content-Type", "application/json")
            addRequestProperty("User-Agent", "Kotlin-DiscordWebhook")
            doOutput = true
            requestMethod = "POST"

            outputStream.use { stream ->
                stream.write(json.toString().toByteArray(StandardCharsets.UTF_8))
                stream.flush()
            }
            inputStream.close()
            disconnect()
        }
    }
}