package com.system32.systemCore.utils

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

class ServerUtil {
    companion object{
        fun player(uuid: UUID): Player? {
            return Bukkit.getPlayer(uuid)
        }

        fun player(name: String) : Player?{
            return Bukkit.getPlayer(name)
        }

        fun offlinePlayer(name: String) : Player?{
            return Bukkit.getOfflinePlayer(name).player
        }

        fun offlinePlayer(uuid: UUID) : Player?{
            return Bukkit.getOfflinePlayer(uuid).player
        }

        fun players() : List<Player>{
            return Bukkit.getOnlinePlayers().toList()
        }
    }
}