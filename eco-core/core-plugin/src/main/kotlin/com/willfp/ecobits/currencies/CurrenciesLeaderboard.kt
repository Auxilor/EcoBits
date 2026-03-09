package com.willfp.ecobits.currencies

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.ecobits.plugin
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.time.Duration
import java.util.*

object CurrenciesLeaderboard {
    private var leaderboardCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(plugin.configYml.getInt("leaderboard.cache-lifetime").toLong()))
        .build<Boolean, Map<Currency, List<UUID>>> {
            if (!plugin.configYml.getBool("leaderboard.enabled"))
                return@build emptyMap()
            val offlinePlayers = Bukkit.getOfflinePlayers()
            val top = mutableMapOf<Currency, List<UUID>>()
            for (currency in Currencies.values())
                top[currency] = offlinePlayers.sortedByDescending { it.getBalance(currency) }.map { it.uniqueId }
            return@build top
        }

    fun Currency.getTop(position: Int): LeaderboardEntry? {
        require(position > 0) { "Position must be greater than 0" }

        val uuid = leaderboardCache.get(true)[this]?.getOrNull(position - 1) ?: return null

        val player = Bukkit.getOfflinePlayer(uuid).takeIf { it.hasPlayedBefore() } ?: return null

        return LeaderboardEntry(
            player,
            player.getBalance(this)
        )
    }

    fun Currency.getPosition(uuid: UUID): Int? {
        val leaderboard = leaderboardCache.get(true)[this]
        val index = leaderboard?.indexOf(uuid)
        return if (index == -1) null else index?.plus(1)
    }

    data class LeaderboardEntry(
        val player: OfflinePlayer,
        val amount: BigDecimal
    )
}