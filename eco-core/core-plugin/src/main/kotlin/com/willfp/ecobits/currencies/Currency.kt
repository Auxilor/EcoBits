@file:JvmName("CurrencyUtils")

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
import com.willfp.ecobits.integrations.IntegrationVault
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.ServicePriority
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

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

    val isPayable = config.getBool("payable")

    val isDecimal = config.getBool("decimal")

    val isRegisteredWithVault = config.getBool("vault")

    init {
        PlaceholderManager.registerPlaceholder(
            PlayerPlaceholder(
                plugin,
                id
            ) {
                it.getBalance(this).toNiceString()
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerPlaceholder(
                plugin,
                "${id}_formatted"
            ) {
                it.getBalance(this).formatWithExtension()
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

        if (isRegisteredWithVault && IntegrationVault.isVaultPresent) {
            Bukkit.getServer().servicesManager.register(
                Economy::class.java,
                IntegrationVault(this),
                plugin,
                ServicePriority.Normal
            )
        }
    }
}

fun Double.formatWithExtension(): String {
    val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
    val numValue = this.toLong()
    val value = floor(log10(numValue.toDouble())).toInt()

    val base = value / 3

    return if (value >= 3 && base < suffix.size) {
        DecimalFormat("#0.0").format(numValue / 10.0.pow((base * 3).toDouble())) + suffix[base]
    } else {
        DecimalFormat("#,##0").format(numValue)
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
