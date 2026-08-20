package com.willfp.ecobits.events

import org.bukkit.OfflinePlayer
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.math.BigDecimal

class VaultBalanceGainEvent(
    val player: OfflinePlayer,
    val amountGained: BigDecimal,
    val newBalance: BigDecimal
) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
