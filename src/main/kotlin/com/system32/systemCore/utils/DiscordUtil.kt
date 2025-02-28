package com.system32.systemCore.utils

import java.awt.Color
import java.io.OutputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class DiscordUtil (private val url: String) {
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

    class EmbedObject {
        var title: String? = null
        var description: String? = null
        var url: String? = null
        var color: Color? = null
        var footer: Footer? = null
        var thumbnail: Thumbnail? = null
        var image: Image? = null
        var author: Author? = null
        val fields = mutableListOf<Field>()

        fun setTitle(title: String) = apply { this.title = title }
        fun setDescription(description: String) = apply { this.description = description }
        fun setUrl(url: String) = apply { this.url = url }
        fun setColor(color: Color) = apply { this.color = color }
        fun setFooter(text: String, icon: String?) = apply { this.footer = Footer(text, icon) }
        fun setThumbnail(url: String) = apply { this.thumbnail = Thumbnail(url) }
        fun setImage(url: String) = apply { this.image = Image(url) }
        fun setAuthor(name: String, url: String?, icon: String?) = apply { this.author = Author(name, url, icon) }
        fun addField(name: String, value: String, inline: Boolean) = apply { fields.add(Field(name, value, inline)) }

        data class Footer(val text: String, val iconUrl: String?)
        data class Thumbnail(val url: String)
        data class Image(val url: String)
        data class Author(val name: String, val url: String?, val iconUrl: String?)
        data class Field(val name: String, val value: String, val inline: Boolean)
    }

    class JSONObject {
        private val map = mutableMapOf<String, Any?>()

        fun put(key: String, value: Any?) {
            value?.let { map[key] = it }
        }

        override fun toString(): String {
            return buildString {
                append("{")
                map.entries.joinToString(",") { entry ->
                    val value = when (val v = entry.value) {
                        is String -> "\"$v\""
                        is JSONObject -> v.toString()
                        is List<*> -> v.joinToString(",", prefix = "[", postfix = "]")
                        else -> v.toString()
                    }
                    "\"${entry.key}\":$value"
                }.let { append(it) }
                append("}")
            }
        }
    }
}
