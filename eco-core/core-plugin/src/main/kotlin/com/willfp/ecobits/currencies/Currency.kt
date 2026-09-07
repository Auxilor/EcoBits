@file:JvmName("CurrencyUtils")

package com.willfp.ecobits.currencies

import com.willfp.eco.core.cache.EcoCache
import com.willfp.eco.core.config.base.LangYml
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.leaderboard.Leaderboard
import com.willfp.eco.core.leaderboard.Leaderboards
import com.willfp.eco.core.leaderboard.registerStandardPlaceholders
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.price.Prices
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.formatWithCommas
import com.willfp.ecobits.EcoBitsPlugin
import com.willfp.ecobits.commands.DynamicCurrencyCommand
import com.willfp.ecobits.events.CurrencyGainEvent
import com.willfp.ecobits.integrations.IntegrationVault
import com.willfp.ecobits.plugin
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.ServicePriority
import java.time.Duration
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import com.willfp.eco.util.formatEco

open class Currency(
    val id: String,
    val plugin: EcoBitsPlugin,
    val config: Config
) {

    private val descCache = EcoCache.builder<Int, String>()
        .expireAfterWrite(Duration.ofMillis(plugin.configYml.getInt("gui.cache-ttl").toLong()))
        .build()

    val default = BigDecimal(config.getDouble("default"))

    val name = config.getFormattedString("name")

    val symbol = config.getFormattedString("symbol")

    val prefix: String? = if (config.has("prefix")) config.getFormattedString("prefix").ifBlank { null } else null

    val max: BigDecimal? = if (config.has("max") && config.getDouble("max") > 0)
        BigDecimal(config.getDouble("max"))
    else null

    val isPayable = config.getBool("payable")

    val isDecimal = config.getBool("decimal")

    val maxDecimals = if (config.has("max-decimals") && config.getInt("max-decimals") > 0)
        config.getInt("max-decimals")
    else null

    val isRegisteredWithVault = config.getBool("vault")

    val isLocal = config.getBool("local")

    val hasShortBalanceCommand = config.getBool("balance-shorthand")

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

    val priceFactory = PriceFactoryCurrency(this)

    /**
     * The leaderboard ranking players by their balance of this currency, or null before the
     * first reload has registered it.
     */
    var leaderboard: Leaderboard? = null
        private set

    /**
     * Register (or re-register) this currency's leaderboard and its placeholders.
     *
     * Called from [com.willfp.ecobits.EcoBitsPlugin.handleReload] rather than from the
     * constructor: currencies are rebuilt by Currencies.update() during the reload, and a
     * leaderboard registered in the constructor would be thrown away by the
     * [Leaderboards.unregisterAll] call at the top of the reload handler.
     */
    internal fun registerLeaderboard() {
        // Nothing at all is registered when disabled -- no leaderboard, and no placeholders. The
        // stub rank placeholder is deliberately gone: every plugin now leaves its placeholders
        // unregistered when its leaderboard is off, rather than three of them disagreeing.
        if (!plugin.configYml.getBool("leaderboard.enabled")) {
            leaderboard = null
            return
        }

        // Ranked by the currency key directly: eco reads every ranked key on the server in one
        // batched query and updates the values in memory as they are written, neither of which it
        // can do through an opaque provider. Players at or below the configured starting balance
        // have not earned anything and are left unranked.
        //
        // Ranked on double for ordering only - the exact BigDecimal is re-read for display, since
        // a balance above 2^53 will not round-trip.
        val leaderboard = Leaderboards.ofKey(plugin, id, key)

        this.leaderboard = leaderboard

        // Prefixed with the leaderboard suffix so the rank placeholder keeps the exact name
        // servers already use: %ecobits_<id>_leaderboard_rank%.
        leaderboard.registerStandardPlaceholders(
            plugin,
            "${id}_leaderboard",
            plugin.langYml.getString("top.empty-position").formatEco()
        ) { BigDecimal.valueOf(it).decimalFormat(this) }
    }

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
                "${id}_raw"
            ) {
                it.getBalance(this).toPlainString()
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

        Prices.registerPriceFactory(priceFactory)

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

fun BigDecimal.numOfDecimals(): Int {
    val plain = this.stripTrailingZeros().toPlainString()
    val index = plain.indexOf('.')
    return if (index < 0) 0 else plain.length - index - 1
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
        .replace("%currency%", currency.name)
        .replace("%symbol%", currency.symbol)
}

fun BigDecimal.formatShort(currency: Currency): String {
    return currency.formatShort
        .replace("%amount%", this.decimalFormatShort(currency))
        .replace("%currency%", currency.name)
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

    val previousBalance = this.getBalance(currency)

    this.profile.write(
        currency.key,
        coerced
    )

    if (coerced > previousBalance) {
        Bukkit.getPluginManager().callEvent(
            CurrencyGainEvent(this, currency, coerced - previousBalance, coerced)
        )
    }
}

fun OfflinePlayer.adjustBalance(currency: Currency, by: BigDecimal) {
    this.setBalance(currency, this.getBalance(currency) + by)
}

fun String.withCurrencyPlaceholders(amount: BigDecimal, currency: Currency, player: String? = null): String {
    var message = this
        .replace("%amount%", amount.decimalFormat(currency))
        .replace("%amount_short%", amount.decimalFormatShort(currency))
        .replace("%amount_formatted%", amount.format(currency))
        .replace("%amount_formatted_short%", amount.formatShort(currency))
        .replace("%amount_raw%", amount.toPlainString())
        .replace("%amount_integer%", amount.toInt().toString())
        .replace("%currency%", currency.name)
        .replace("%symbol%", currency.symbol)

    if (player != null) {
        message = message.replace("%player%", player)
    }

    return message
}

fun LangYml.getCurrencyMessage(
    key: String,
    currency: Currency,
    option: StringUtils.FormatOption = StringUtils.FormatOption.WITH_PLACEHOLDERS
): String {
    val prefix = currency.prefix ?: currency.plugin.langYml.prefix
    return prefix + this.getFormattedString("${LangYml.KEY_MESSAGES}.$key", option)
}
