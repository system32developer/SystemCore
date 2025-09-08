package com.system32.systemCore.managers.commands

import com.system32.systemCore.SystemCore
import com.system32.systemCore.managers.commands.parameterTypes.OnlinePlayer
import com.system32.systemCore.managers.commands.parameterTypes.OnlinePlayerParameter
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.parameter.ParameterType

/**
 * CommandManager is responsible for managing commands in the Bukkit environment.
 * It allows for the registration of command parameters and the building of command lamps.
 * This class is designed to be used within the SystemCore plugin.
 *
 * Usage:
 * ```kotlin
 * CommandManager().apply {
 *      parameter(
 *          KitName::class.java to KitNameParameter(),
 *          OnlinePlayer::class.java to OnlinePlayerParameter()
 *      )
 *      build()
 *      command(KitCommand())
 *      command(AnotherCommand())
 * }
 * ```
 * ** Don´t forget to put this on build.gradle.kts**
 *
 * ```kotlin
 * tasks.withType<KotlinJvmCompile> {
 *     compilerOptions {
 *         javaParameters = true
 *     }
 * }
 * ```
 */
class CommandManager {
    private val plugin = SystemCore.plugin

    private val builder = BukkitLamp.builder(plugin)
    private var _lamp: Lamp<BukkitCommandActor>? = null
    private val parameters = mutableMapOf<Class<*>, ParameterType<BukkitCommandActor, *>>()

    private val lamp: Lamp<BukkitCommandActor>
        get() {
            return _lamp!!
        }

    init {
        parameters[OnlinePlayer::class.java] = OnlinePlayerParameter()
    }


    /**
     * Registers command parameters for the Lamp.
     * This method allows you to define custom parameter types that can be used in commands.
     * @param types A vararg of pairs where each pair consists of a Class type and its corresponding ParameterType.
     * Example:
     * ```kotlin
     * class OnlinePlayer(val player: String) {}
     * class OnlinePlayerParameter : ParameterType<BukkitCommandActor, OnlinePlayer> {
     *
     *     override fun parse(input: MutableStringStream, context: ExecutionContext<BukkitCommandActor>): OnlinePlayer {
     *         val name = input.readString()
     *         val player = Bukkit.getPlayer(name)
     *             ?: throw CommandErrorException("Player with name '$name' is not online or does not exist.")
     *         return OnlinePlayer(player.name)
     *     }
     *
     *     override fun defaultSuggestions(): SuggestionProvider<BukkitCommandActor> {
     *         return SuggestionProvider { _ ->
     *             Bukkit.getOnlinePlayers().map { it.name }.toList()
     *         }
     *     }
     * }
     * ```
     *
     * Register it like this:
     * ```kotlin
     * CommandManager().apply {
     *     parameter(
     *     OnlinePlayer::class.java to OnlinePlayerParameter()
     *     )
     *     build()
     *     command(YourCommand())
     * }
     * ```
     */
    fun parameter(vararg types: Pair<Class<*>, ParameterType<BukkitCommandActor, *>>) {
        for (type in types) {
            parameters[type.first] = type.second
        }
    }

    fun build(){
        builder.parameterTypes {
            parameters.forEach { (type, parameterType) ->
                @Suppress("UNCHECKED_CAST")
                it.addParameterType(type as Class<Any>, parameterType as ParameterType<BukkitCommandActor, Any>)
            }
        }
        _lamp = builder.build()
    }

    fun command(vararg commands: Any) {
        for (command in commands) {
            //no longer using paper lamp
            lamp.register(command)
        }
    }
}