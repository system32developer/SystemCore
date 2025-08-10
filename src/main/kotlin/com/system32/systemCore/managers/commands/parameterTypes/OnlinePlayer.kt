package com.system32.systemCore.managers.commands.parameterTypes

import org.bukkit.Bukkit
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.exception.CommandErrorException
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.parameter.ParameterType
import revxrsal.commands.stream.MutableStringStream

class OnlinePlayer(val name: String) {
    val asPlayer get() = Bukkit.getPlayer(name) ?: throw CommandErrorException("Player with name '$player' is not online or does not exist.")

    val uniqueId get() = asPlayer.uniqueId

}
class OnlinePlayerParameter : ParameterType<BukkitCommandActor, OnlinePlayer> {

    override fun parse(input: MutableStringStream, context: ExecutionContext<BukkitCommandActor>): OnlinePlayer {
        val name = input.readString()
        val player = Bukkit.getPlayer(name)
            ?: throw CommandErrorException("Player with name '$name' is not online or does not exist.")
        return OnlinePlayer(player.name)
    }

    override fun defaultSuggestions(): SuggestionProvider<BukkitCommandActor> {
        return SuggestionProvider { _ ->
            Bukkit.getOnlinePlayers().map { it.name }.toList()
        }
    }
}
