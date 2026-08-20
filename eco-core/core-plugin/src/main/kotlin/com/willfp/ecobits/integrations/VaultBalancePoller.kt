package com.willfp.ecobits.integrations

import com.willfp.ecobits.events.VaultBalanceGainEvent
import com.willfp.ecobits.plugin
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object VaultBalancePoller : Listener {
    private val lastSeenBalances = ConcurrentHashMap<UUID, BigDecimal>()

    fun start() {
        val intervalSeconds = plugin.configYml.getInt("vault.poll-interval-seconds")

        if (intervalSeconds <= 0) {
            return
        }

        val intervalTicks = intervalSeconds * 20L

        plugin.scheduler.runTimer(intervalTicks, intervalTicks) {
            poll()
        }
    }

    private fun poll() {
        val economy = Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider ?: return

        if (economy is IntegrationVault) {
            return
        }

        for (player in Bukkit.getOnlinePlayers()) {
            val newBalance = economy.getBalance(player).toBigDecimal()
            val previousBalance = lastSeenBalances[player.uniqueId]

            lastSeenBalances[player.uniqueId] = newBalance

            if (previousBalance != null && newBalance > previousBalance) {
                Bukkit.getPluginManager().callEvent(
                    VaultBalanceGainEvent(player, newBalance - previousBalance, newBalance)
                )
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        lastSeenBalances.remove(event.player.uniqueId)
    }
}
