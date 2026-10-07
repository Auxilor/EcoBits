package com.willfp.ecobits.events

import com.willfp.ecobits.currencies.Currency
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.math.BigDecimal

class CurrencyGainEvent(
    val player: OfflinePlayer,
    val currency: Currency,
    val amountGained: BigDecimal,
    val newBalance: BigDecimal
) : Event(!Bukkit.isPrimaryThread()) {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
