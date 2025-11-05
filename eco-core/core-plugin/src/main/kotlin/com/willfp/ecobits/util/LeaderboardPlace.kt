package com.willfp.ecobits.util

import org.bukkit.OfflinePlayer
import java.math.BigDecimal

data class LeaderboardPlace(
    val player: OfflinePlayer,
    val amount: BigDecimal
)
