package com.system32dev.systemCore.managers.commands.parameters

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.exception.CommandErrorException
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.parameter.ParameterType
import revxrsal.commands.stream.MutableStringStream

class OnlinePlayerParameter : ParameterType<BukkitCommandActor, Player> {
    override fun parse(input: MutableStringStream, context: ExecutionContext<BukkitCommandActor>): Player {
        val name = input.readString()
        return Bukkit.getPlayer(name)
            ?: throw CommandErrorException("Player with name '$name' is not online or does not exist.")
    }

    override fun defaultSuggestions(): SuggestionProvider<BukkitCommandActor> {
        return SuggestionProvider { context ->
            Bukkit.getOnlinePlayers()
                .map { it.name }
                .filter { it.startsWith(context.input().peekString(), ignoreCase = true) }
        }
    }
}