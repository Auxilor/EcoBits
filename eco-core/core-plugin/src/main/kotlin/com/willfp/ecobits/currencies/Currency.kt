@file:JvmName("CurrencyUtils")

package com.willfp.ecobits.currencies

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.price.Prices
import com.willfp.eco.util.formatWithCommas
import com.willfp.ecobits.EcoBitsPlugin
import com.willfp.ecobits.commands.DynamicCurrencyCommand
import com.willfp.ecobits.currencies.CurrenciesLeaderboard.getPosition
import com.willfp.ecobits.integrations.IntegrationVault
import com.willfp.ecobits.plugin
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.ServicePriority
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

open class Currency(
    val id: String,
    val plugin: EcoBitsPlugin,
    val config: Config
) {

    private val descCache = Caffeine.newBuilder()
        .expireAfterWrite(plugin.configYml.getInt("gui.cache-ttl").toLong(), TimeUnit.MILLISECONDS)
        .build<Int, String>()

    val default = BigDecimal(config.getDouble("default"))

    val name = config.getFormattedString("name")

    val symbol = config.getFormattedString("symbol")

    val max: BigDecimal? = if (config.has("max") && config.getDouble("max") > 0)
        BigDecimal(config.getDouble("max"))
    else null

    val isPayable = config.getBool("payable")

    val isDecimal = config.getBool("decimal")

    val isRegisteredWithVault = config.getBool("vault")

    val isLocal = config.getBool("local")

    val commands = config.getStrings("commands").map { DynamicCurrencyCommand(it, this) }

    val key = PersistentDataKey(
        plugin.createNamespacedKey(if (isLocal) "${plugin.serverID}_${id}" else id),
        PersistentDataKeyType.BIG_DECIMAL,
        default
    )

    val format = config.getFormattedString("format")

    val formatShort = config.getFormattedString("format-short")

    val decimalFormat = DecimalFormat(config.getString("decimal-format"))

    val decimalFormatShort = DecimalFormat(config.getString("decimal-format-short"))

    private fun registerCommands() {
        this.commands.forEach { it.register() }
    }

    private fun unregisterCommands() {
        this.commands.forEach { it.unregister() }
    }

    init {
        PlaceholderManager.registerPlaceholder(
            PlayerPlaceholder(
                plugin,
                id
            ) {
                it.getBalance(this).decimalFormat(this)
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerPlaceholder(
                plugin,
                "${id}_short"
            ) {
                it.getBalance(this).decimalFormatShort(this)
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerPlaceholder(
                plugin,
                "${id}_formatted"
            ) {
                it.getBalance(this).format(this)
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerPlaceholder(
                plugin,
                "${id}_formatted_short"
            ) {
                it.getBalance(this).formatShort(this)
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerPlaceholder(
                plugin,
                "${id}_commas"
            ) {
                it.getBalance(this).formatWithCommas()
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerPlaceholder(
                plugin,
                "${id}_integer"
            ) {
                it.getBalance(this).toInt().toString()
            }
        )

        if (plugin.configYml.getBool("leaderboard.enabled"))
            PlaceholderManager.registerPlaceholder(
                PlayerPlaceholder(
                    plugin,
                    "${id}_leaderboard_rank"
                )
                { player ->
                    val emptyPosition = plugin.langYml.getString("top.empty-position")
                    val position = getPosition(player.uniqueId)
                    position?.toString() ?: emptyPosition
                }.register()
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

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "${id}_symbol"
            ) {
                this.symbol
            }
        )

        Prices.registerPriceFactory(PriceFactoryCurrency(this))

        if (isRegisteredWithVault && IntegrationVault.isVaultPresent) {
            Bukkit.getServer().servicesManager.register(
                Economy::class.java,
                IntegrationVault(this),
                plugin,
                ServicePriority.Highest
            )
        }

        this.unregisterCommands()
        this.registerCommands()
    }

    internal open fun getBalance(player: OfflinePlayer) = getSavedBalance(player)
    internal fun getSavedBalance(player: OfflinePlayer) = player.profile.read(key)

    override fun equals(other: Any?): Boolean {
        return other is Currency && other.id == this.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}

fun BigDecimal.hasDecimals(): Boolean {
    return this.setScale(0, RoundingMode.CEILING) != this.setScale(0, RoundingMode.FLOOR)
}

@Deprecated("Deprecated")
fun BigDecimal.formatWithExtension(): String {
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

fun BigDecimal.format(currency: Currency): String {
    return currency.format
        .replace("%amount%", this.decimalFormat(currency))
        .replace("%name%", currency.name)
        .replace("%symbol%", currency.symbol)
}

fun BigDecimal.formatShort(currency: Currency): String {
    return currency.formatShort
        .replace("%amount%", this.decimalFormatShort(currency))
        .replace("%name%", currency.name)
        .replace("%symbol%", currency.symbol)
}

fun BigDecimal.decimalFormat(currency: Currency): String {
    val stripped = this.stripTrailingZeros()
    return currency.decimalFormat.format(stripped)
}

fun BigDecimal.decimalFormatShort(currency: Currency): String {
    val numValue = this.toLong()
    val value = floor(log10(numValue.toDouble())).toInt()

    val base = value / 3

    return if (value >= 3 && base < plugin.shortcuts.size) {
        currency.decimalFormatShort.format(numValue / 10.0.pow((base * 3).toDouble())) + plugin.shortcuts[base]
    } else {
        currency.decimalFormatShort.format(numValue)
    }
}

fun OfflinePlayer.getBalance(currency: Currency): BigDecimal {
    return this.profile.read(currency.key)
}

fun OfflinePlayer.setBalance(currency: Currency, value: BigDecimal) {
    val coerced = if (currency.max == null) value.coerceAtLeast(BigDecimal.ZERO)
    else value.coerceIn(BigDecimal.ZERO..currency.max)

    this.profile.write(
        currency.key,
        coerced
    )
}

fun OfflinePlayer.adjustBalance(currency: Currency, by: BigDecimal) {
    this.setBalance(currency, this.getBalance(currency) + by)
}
