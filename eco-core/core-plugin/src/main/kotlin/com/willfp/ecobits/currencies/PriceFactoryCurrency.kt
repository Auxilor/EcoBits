package com.willfp.ecobits.currencies

import org.bukkit.entity.Player
import wiki.ecohub.core.price.Price
import wiki.ecohub.core.price.PriceFactory
import wiki.ecohub.ui.placeholder.PlaceholderContext
import wiki.ecohub.ui.placeholder.PlaceholderContextSupplier
import java.util.Collections
import java.util.WeakHashMap

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
        private val multipliers: MutableMap<Player, Double> = Collections.synchronizedMap(WeakHashMap())

        override fun canAfford(player: Player, multiplier: Double) =
            player.getBalance(currency).toDouble() >= getValue(player, multiplier)

        override fun pay(player: Player, multiplier: Double) =
            player.adjustBalance(currency, -getValue(player, multiplier).toBigDecimal())

        override fun giveTo(player: Player, multiplier: Double) =
            player.adjustBalance(currency, getValue(player, multiplier).toBigDecimal())

        override fun getValue(player: Player, multiplier: Double) =
            xp(baseContext.copyWithPlayer(player)) * getMultiplier(player) * multiplier

        override fun getMultiplier(player: Player) = multipliers[player] ?: 1.0

        override fun setMultiplier(player: Player, multiplier: Double) {
            multipliers[player] = multiplier
        }
    }
}
