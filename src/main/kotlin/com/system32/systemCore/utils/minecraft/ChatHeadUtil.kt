package com.system32.systemCore.utils.minecraft

import com.system32.systemCore.utils.text.TextUtil.Companion.color
import net.kyori.adventure.text.Component
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

class ChatHeadUtil {
    companion object {
        fun fromRGB(r: Int, g: Int, b: Int): String {
            return "<color:${String.format("#%02x%02x%02x", r, g, b)}>"
        }

        fun generateChatHead(user: String, scale: Int = 8, character: String = "⬛", addNewLine: Boolean = false): List<String> {
            val urlString = "https://minotar.net/helm/$user/$scale.png"
            val chatHeadBuilder = mutableListOf<String>()
            try {
                val url = URL(urlString)
                val image: BufferedImage = ImageIO.read(url)
                for (i in 0 until image.height) {
                    for (j in 0 until image.width) {
                        val color = Color(image.getRGB(j, i))
                        val chatColor = fromRGB(color.red, color.green, color.blue)
                        val line = "$chatColor$character</color>${if (addNewLine) "\n" else ""}"
                        chatHeadBuilder.add(line)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return chatHeadBuilder
        }
    }
}