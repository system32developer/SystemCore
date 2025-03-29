package com.system32.systemCore.utils.minecraft

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class ItemUtil {
    companion object{
        fun fromBase64(base64: String): ItemStack? {
            try {
                val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(base64))
                val bukkitInputStream = BukkitObjectInputStream(inputStream)
                val item = bukkitInputStream.readObject() as ItemStack?
                bukkitInputStream.close()
                return item
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            return null
        }

        fun toBase64(item: ItemStack?): String? {
            try {
                val outputStream = ByteArrayOutputStream()
                val bukkitOutputStream = BukkitObjectOutputStream(outputStream)
                bukkitOutputStream.writeObject(item)
                bukkitOutputStream.close()
                return Base64Coder.encodeLines(outputStream.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    }
}