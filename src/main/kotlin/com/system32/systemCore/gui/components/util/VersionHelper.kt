package com.system32.systemCore.gui.components.util

import com.google.common.primitives.Ints
import com.system32.systemCore.gui.components.exception.GuiException
import org.bukkit.Bukkit
import java.util.regex.Pattern

public final class VersionHelper {
    companion object {
        private val CRAFTBUKKIT_PACKAGE: String = Bukkit.getServer().javaClass.getPackage().getName()

        // Unbreakable change
        private const val V1_11: Int = 1110
        // Material and components on items change
        private const val V1_13: Int = 1130
        // PDC and customModelData
        private const val V1_14: Int = 1140
        // Paper adventure changes
        private const val V1_16_5: Int = 1165
        // SkullMeta#setOwningPlayer was added
        private const val V1_12_1: Int = 1121
        // PlayerProfile API
        private const val V1_20_1: Int = 1201
        private const val V1_20_5: Int = 1205

        private val CURRENT_VERSION: Int = getCurrentVersion()

        val IS_COMPONENT_LEGACY: Boolean = CURRENT_VERSION < V1_16_5

        val IS_ITEM_LEGACY: Boolean = CURRENT_VERSION < V1_13

        val IS_UNBREAKABLE_LEGACY: Boolean = CURRENT_VERSION < V1_11

        val IS_PDC_VERSION: Boolean = CURRENT_VERSION >= V1_14

        val IS_SKULL_OWNER_LEGACY: Boolean = CURRENT_VERSION < V1_12_1

        val IS_CUSTOM_MODEL_DATA: Boolean = CURRENT_VERSION >= V1_14

        val IS_PLAYER_PROFILE_API: Boolean = CURRENT_VERSION >= V1_20_1

        val IS_ITEM_NAME_COMPONENT: Boolean = CURRENT_VERSION >= V1_20_5

        private val IS_PAPER: Boolean = checkPaper()

        val IS_FOLIA: Boolean = checkFolia()

        private fun checkPaper(): Boolean {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig")
                return true
            } catch (ignored: ClassNotFoundException) {
                return false
            }
        }

        private fun checkFolia(): Boolean {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
                return true
            } catch (ignored: ClassNotFoundException) {
                return false
            }
        }

        private fun getCurrentVersion(): Int {
            val matcher =
                Pattern.compile("(?<version>\\d+\\.\\d+)(?<patch>\\.\\d+)?").matcher(Bukkit.getBukkitVersion())

            val stringBuilder = StringBuilder()
            if (matcher.find()) {
                stringBuilder.append(matcher.group("version").replace(".", ""))
                val patch = matcher.group("patch")
                if (patch == null) stringBuilder.append("0")
                else stringBuilder.append(patch.replace(".", ""))
            }

            val version = Ints.tryParse(stringBuilder.toString())

            if (version == null) throw GuiException("Could not retrieve server version!")

            return version
        }

        @Throws(ClassNotFoundException::class)
        fun craftClass(name: String): Class<*> {
            return Class.forName("$CRAFTBUKKIT_PACKAGE.$name")
        }
    }
}