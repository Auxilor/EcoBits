package com.willfp.ecobits.currencies

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.price.Prices
import com.willfp.eco.util.toNiceString
import org.bukkit.OfflinePlayer

class Currency(
    val id: String,
    val plugin: EcoPlugin,
    val config: Config
) {
    val default = config.getDouble("default")

    val key = PersistentDataKey(
        plugin.createNamespacedKey(id),
        PersistentDataKeyType.DOUBLE,
        default
    )

    val name = config.getFormattedString("name")
    val max = config.getDouble("max").let { if (it < 0) Double.MAX_VALUE else it }

    init {
        PlaceholderManager.registerPlaceholder(
            PlayerPlaceholder(
                plugin,
                id
            ) {
                it.getBalance(this).toString()
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerPlaceholder(
                plugin,
                "${id}_formatted"
            ) {
                it.getBalance(this).toNiceString()
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "${id}_name"
            ) {
                this.name
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "${id}_max"
            ) {
                this.max.toString()
            }
        )

        Prices.registerPriceFactory(PriceFactoryCurrency(this))
    }
}

fun OfflinePlayer.getBalance(currency: Currency): Double {
    return this.profile.read(currency.key)
}

fun OfflinePlayer.setBalance(currency: Currency, value: Double) {
    this.profile.write(
        currency.key,
        value.coerceIn(0.0..currency.max)
    )
}

fun OfflinePlayer.adjustBalance(currency: Currency, by: Double) {
    this.setBalance(currency, this.getBalance(currency) + by)
}
