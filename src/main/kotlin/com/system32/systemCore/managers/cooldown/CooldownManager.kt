package com.system32.systemCore.managers.cooldown

/**
 * Manages cooldowns for different actions performed by players.
 * This class allows tracking, starting, checking, retrieving, and removing cooldowns per action and player.
 */
class CooldownManager {
    private val playerCooldowns = mutableMapOf<String, MutableMap<String, Cooldown>>()

    /**
     * Starts a cooldown for a specific action and player.
     *
     * @param action The action associated with the cooldown.
     * @param player The player's identifier.
     * @param duration The duration of the cooldown in seconds.
     */
    fun startCooldown(action: String, player: String, duration: Int) {
        playerCooldowns.computeIfAbsent(action) { mutableMapOf() }[player] = Cooldown(System.currentTimeMillis() + (duration * 1000))
    }

    /**
     * Checks if a cooldown is active for a specific action and player.
     * If the cooldown has expired, it is removed from the system.
     *
     * @param action The action to check.
     * @param player The player's identifier.
     * @return True if the cooldown is active, false otherwise.
     */
    fun isCooldownActive(action: String, player: String): Boolean {
        val cooldown = playerCooldowns[action]?.get(player) ?: return false
        if (System.currentTimeMillis() > cooldown.expirationTime) {
            playerCooldowns[action]?.remove(player)
            if (playerCooldowns[action]?.isEmpty() == true) playerCooldowns.remove(action)
            return false
        }
        return true
    }

    /**
     * Retrieves the cooldown object for a specific action and player.
     *
     * @param action The action to retrieve the cooldown for.
     * @param player The player's identifier.
     * @return The cooldown object if present, null otherwise.
     */
    fun getCooldown(action: String, player: String): Cooldown? {
        return playerCooldowns[action]?.get(player)
    }

    /**
     * Removes an active cooldown for a specific action and player.
     *
     * @param action The action to remove the cooldown for.
     * @param player The player's identifier.
     */
    fun removeCooldown(action: String, player: String) {
        playerCooldowns[action]?.remove(player)
        if (playerCooldowns[action]?.isEmpty() == true) playerCooldowns.remove(action)
    }

    /**
     * Clears all cooldowns for all actions and players.
     */
    fun clearAllCooldowns() {
        playerCooldowns.clear()
    }
}
