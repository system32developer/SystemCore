package com.system32.systemCore.utils.discord

import java.awt.Color

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