package com.system32.systemCore

import com.system32.systemCore.utils.DiscordUtil
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * Clase principal de utilidad para obtener la instancia del plugin de Bukkit.
 *
 * Esta clase utiliza un método de inicialización diferida (`lazy`) para obtener
 * automáticamente la instancia del plugin de Bukkit sin necesidad de pasarla manualmente.
 */
class SystemCore {
    companion object {
        /**
         * Instancia del plugin de Bukkit, obtenida automáticamente usando `JavaPlugin.getProvidingPlugin`.
         */
        private val plugin: Plugin by lazy {
            JavaPlugin.getProvidingPlugin(TestEvent::class.java)
        }

        /**
         * Obtiene la instancia del plugin.
         *
         * @return La instancia del plugin de Bukkit.
         */
        fun getInstance(): Plugin {
            return plugin
        }
    }
}
