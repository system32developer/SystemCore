package com.system32.systemCore.managers

import com.system32.systemCore.SystemCore
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.command.CommandParameter
import revxrsal.commands.parameter.ParameterType

class CommandManager {
    val plugin = SystemCore.plugin

    val builder = BukkitLamp.builder(plugin)
    private var _lamp: Lamp<BukkitCommandActor>? = null
    val parameters = mutableMapOf<Class<*>, ParameterType<BukkitCommandActor, *>>()

    val lamp: Lamp<BukkitCommandActor>
        get() {
            return _lamp!!
        }


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

    fun command(command: Any) {
        if (command is Iterable<*>) {
            for (cmd in command) {
                lamp.register(cmd)
            }
        } else {
            lamp.register(command)
        }
    }

}