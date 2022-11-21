package com.willfp.ecobits.currencies

import com.willfp.eco.core.math.MathContext
import com.willfp.eco.core.price.Price
import com.willfp.eco.core.price.PriceFactory
import org.bukkit.entity.Player
import java.util.UUID
import java.util.function.Function
import kotlin.math.roundToInt

class PriceFactoryCurrency(
    private val currency: Currency
) : PriceFactory {
    override fun getNames() = listOf(
        currency.id
    )

    override fun create(baseContext: MathContext, function: Function<MathContext, Double>): Price {
        return PriceCurrency(currency, baseContext) { function.apply(it) }
    }

    private class PriceCurrency(
        private val currency: Currency,
        private val baseContext: MathContext,
        private val xp: (MathContext) -> Double
    ) : Price {
        private val multipliers = mutableMapOf<UUID, Double>()

        override fun canAfford(player: Player) = player.getBalance(currency) >= getValue(player)

        override fun pay(player: Player) {
            player.adjustBalance(currency, -getValue(player))
        }

        override fun giveTo(player: Player) {
            player.adjustBalance(currency, getValue(player))
        }

        override fun getValue(player: Player): Double {
            return xp(MathContext.copyWithPlayer(baseContext, player)) * getMultiplier(player)
        }

        override fun getMultiplier(player: Player): Double {
            return multipliers[player.uniqueId] ?: 1.0
        }

        override fun setMultiplier(player: Player, multiplier: Double) {
            multipliers[player.uniqueId] = multiplier
        }
    }
}