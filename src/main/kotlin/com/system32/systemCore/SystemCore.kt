package com.system32.systemCore

import com.system32.systemCore.managers.chat.ChatAwaiterManager
import com.system32.systemCore.managers.chat.ChatTriggerManager
import com.system32.systemCore.managers.cooldown.CooldownManager
import com.system32.systemCore.managers.usableItems.UsableItemsManager
import com.system32.systemCore.utils.minecraft.ChatHeadUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor

/**
 * Main system core class providing utilities and core functionalities.
 * This includes cooldown management, plugin reference, and event registration.
 */
object SystemCore {
    /**
     * Provides the main plugin instance.
     */
    private val _plugin: Lazy<JavaPlugin> = lazy {
        JavaPlugin.getProvidingPlugin(SystemCore::class.java)
    }

    val plugin: JavaPlugin
        get() = _plugin.value

    private var _lamp: Lamp<BukkitCommandActor>? = null

    private val lamp: Lamp<BukkitCommandActor>
        get() {
            if (_lamp == null) {
                _lamp = BukkitLamp.builder(plugin).build()
            }
            return _lamp!!
        }

    /**
     * Indicates whether PlaceholderAPI support is enabled.
     */
    var placeholderAPISupport: Boolean = false
        private set
        get() = plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")

    var githubUser = "System32"
    var githubRepo = "SystemCore"

    var githubSupport: Boolean = false
        private set
        get() = !(githubUser == "System32" && githubRepo == "SystemCore")

    /**
     * Manages cooldowns for various actions.
     */
    val cooldownManager = CooldownManager()

    /**
     * Utility class for managing chat heads.
     */

    val chatHeadUtil = ChatHeadUtil()


    /**
     * Provides a debugger for logging and debugging purposes.
     */

    lateinit var debugger : Debugger

    /**
     * Manages chat awaiters and triggers.
     */
    val chatAwaiterManager: ChatAwaiterManager by lazy {
        ChatAwaiterManager().also { event(it) }
    }

    /**
     * Manages chat triggers for specific keywords.
     */
    val chatTriggerManager: ChatTriggerManager by lazy {
        ChatTriggerManager().also { event(it) }
    }

    /**
     * Manages usable items and their interactions.
     */

    val usableItemsManager: UsableItemsManager by lazy {
        UsableItemsManager().also { event(it) }
    }

    /**
     * Registers an event listener to the plugin's event system.
     *
     * @param event The event listener to register.
     */
    fun event(vararg listeners: Listener) {
        listeners.forEach { plugin.server.pluginManager.registerEvents(it, plugin) }
    }

    /**
     * Registers a command executor to the plugin's command system.
     *
     * @param command The command executor to register.
     */

    fun legacyCommand(commandName: String, command: CommandExecutor) {
        plugin.getCommand(commandName)?.setExecutor(command)
    }

    /**
     * Registers a command to the plugin's command system.
     *
     * @param command The command to register.
     */

    fun command(command: Any) {
        lamp.register(command)
    }
}