package com.willfp.ecobits.currencies

import com.willfp.eco.core.placeholder.context.PlaceholderContext
import com.willfp.eco.core.placeholder.context.PlaceholderContextSupplier
import com.willfp.eco.core.price.Price
import com.willfp.eco.core.price.PriceFactory
import org.bukkit.entity.Player
import java.util.UUID

class PriceFactoryCurrency(
    private val currency: Currency
) : PriceFactory {
    override fun getNames() = listOf(
        currency.id
    )

    override fun create(baseContext: PlaceholderContext, function: PlaceholderContextSupplier<Double>): Price {
        return PriceCurrency(currency, baseContext) { function.get(it) }
    }

    private class PriceCurrency(
        private val currency: Currency,
        private val baseContext: PlaceholderContext,
        private val xp: (PlaceholderContext) -> Double
    ) : Price {
        private val multipliers = mutableMapOf<UUID, Double>()

        override fun canAfford(player: Player, multiplier: Double) =
            player.getBalance(currency).toDouble() >= getValue(player, multiplier)

        override fun pay(player: Player, multiplier: Double) =
            player.adjustBalance(currency, -getValue(player, multiplier).toBigDecimal())

        override fun giveTo(player: Player, multiplier: Double) =
            player.adjustBalance(currency, getValue(player, multiplier).toBigDecimal())

        override fun getValue(player: Player, multiplier: Double) =
            xp(baseContext.copyWithPlayer(player)) * getMultiplier(player) * multiplier

        override fun getMultiplier(player: Player): Double {
            return multipliers[player.uniqueId] ?: 1.0
        }

        override fun setMultiplier(player: Player, multiplier: Double) {
            multipliers[player.uniqueId] = multiplier
        }
    }
}
