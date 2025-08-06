package com.system32.systemCore.utils.minecraft

import com.system32.systemCore.utils.text.TextUtil.color
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

class ChatHeadUtil {

    val headsCache = mutableMapOf<String, List<String>>()

    /**
     * Generates a chat head image from a Minecraft player's skin.
     *
     * @param user The Minecraft player.
     * @param scale The scale of the image (default is 8).
     * @param character The character to use for each pixel (default is "⬛").
     * @param addNewLine Whether to add a new line after each pixel (default is false).
     * @param cached Whether to cache the generated image (default is false).
     * @return A list of strings representing the chat head image.
     *
     * ### Example usage:
     * ```kotlin
     * val chatHead = ChatHeadUtil.generateChatHead(player)
     * for (line in chatHead) {
     *    player.sendMessage(color(line))
     * }
     * ```
     */

    fun generateChatHead(user: Player, scale: Int = 8, character: String = "⬛", addNewLine: Boolean = false, cached: Boolean= false): List<String> {
        return generateChatHead(user.name, scale, character, addNewLine, cached)
    }

    /**
     * Generates a chat head image from a Minecraft player's skin.
     *
     * @param user The username of the Minecraft player.
     * @param scale The scale of the image (default is 8).
     * @param character The character to use for each pixel (default is "⬛").
     * @param addNewLine Whether to add a new line after each pixel (default is false).
     * @param cached Whether to cache the generated image (default is false).
     * @return A list of strings representing the chat head image.
     *
     * ### Example usage:
     * ```kotlin
     * val chatHead = ChatHeadUtil.generateChatHead("Notch")
     * for (line in chatHead) {
     *    player.sendMessage(color(line))
     * }
     * ```
     */

    fun generateChatHead(user: String, scale: Int = 8, character: String = "⬛", addNewLine: Boolean = false, cached: Boolean= false): List<String> {
        return generateChatHead("https://minotar.net/helm/$user/$scale.png", character, addNewLine, cached)
    }

    /**
     * Generates a chat head image from a URL.
     *
     * @param image The URL of the image.
     * @param character The character to use for each pixel (default is "⬛").
     * @param addNewLine Whether to add a new line after each pixel (default is false).
     * @param cached Whether to cache the generated image (default is false).
     * @return A list of strings representing the chat head image.
     *
     * ### Example usage:
     * ```kotlin
     * val chatHead = ChatHeadUtil.generateChatHead("https://example.com/image.png")
     * for (line in chatHead) {
     *     player.sendMessage(color(line))
     * }
     * ```
     */

    fun generateChatHead(image: String, character: String = "⬛", addNewLine: Boolean = false, cached: Boolean = false): List<String>{
        if (headsCache.containsKey(image)) {
            return headsCache[image]!!
        }
        val chatHeadBuilder = mutableListOf<String>()
        try {
            val url = URL(image)
            val bufferedImage: BufferedImage = ImageIO.read(url)
            for (i in 0 until bufferedImage.height) {
                val chatHeadLine = StringBuilder()
                for (j in 0 until bufferedImage.width) {
                    val color = Color(bufferedImage.getRGB(j, i))
                    val chatColor = fromRGB(color.red, color.green, color.blue)
                    val line = "$chatColor$character</color>${if (addNewLine) "\n" else ""}"
                    chatHeadLine.append(line)
                }
                chatHeadBuilder.add(chatHeadLine.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (cached) headsCache[image] = chatHeadBuilder
        return chatHeadBuilder
    }

    private fun fromRGB(r: Int, g: Int, b: Int): String {
        return "<color:${String.format("#%02x%02x%02x", r, g, b)}>"
    }
}