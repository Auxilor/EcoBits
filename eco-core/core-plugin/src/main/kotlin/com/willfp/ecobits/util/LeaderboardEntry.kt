package com.willfp.ecobits.util

import org.bukkit.OfflinePlayer
import java.math.BigDecimal

data class LeaderboardEntry(
    val player: OfflinePlayer,
    val amount: BigDecimal
)
