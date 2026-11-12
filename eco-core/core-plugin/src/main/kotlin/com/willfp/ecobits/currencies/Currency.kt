@file:JvmName("CurrencyUtils")

package com.willfp.ecobits.currencies

import com.willfp.ecobits.EcoBitsPlugin
import com.willfp.ecobits.commands.DynamicCurrencyCommand
import com.willfp.ecobits.events.CurrencyGainEvent
import com.willfp.ecobits.integrations.IntegrationVault
import com.willfp.ecobits.plugin
import com.willfp.ecobits.runOwnedIfOnline
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.ServicePriority
import wiki.ecohub.config.Config
import wiki.ecohub.config.lang.LangYml
import wiki.ecohub.core.price.Prices
import wiki.ecohub.data.PersistentDataKey
import wiki.ecohub.data.PersistentDataKeyType
import wiki.ecohub.data.cache.EcoCache
import wiki.ecohub.data.playerbase.leaderboard.Leaderboard
import wiki.ecohub.data.playerbase.leaderboard.Leaderboards
import wiki.ecohub.data.playerbase.leaderboard.registerStandardPlaceholders
import wiki.ecohub.game.entity.player.profile
import wiki.ecohub.math.formatWithCommas
import wiki.ecohub.ui.placeholder.PlaceholderManager
import wiki.ecohub.ui.placeholder.type.PlayerPlaceholder
import wiki.ecohub.ui.placeholder.type.PlayerlessPlaceholder
import wiki.ecohub.ui.text.StringUtils
import wiki.ecohub.ui.text.formatEco
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.Duration
import java.util.Objects
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

open class Currency(
    val id: String,
    val plugin: EcoBitsPlugin,
    val config: Config
) {

    private val descCache = EcoCache.builder<Int, String>()
        .expireAfterWrite(Duration.ofMillis(plugin.configYml.getInt("gui.cache-ttl").toLong()))
        .build()

    val default = config.getDouble("default").toBigDecimal()

    val name = config.getFormattedString("name")

    val symbol = config.getFormattedString("symbol")

    val prefix: String? = if (config.has("prefix")) config.getFormattedString("prefix").ifBlank { null } else null

    val max: BigDecimal? = if (config.has("max") && config.getDouble("max") > 0)
        config.getDouble("max").toBigDecimal()
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
    @Volatile
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

    /**
     * Unregister this currency's commands before it is replaced.
     */
    fun shutdown() {
        unregisterCommands()
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
    internal fun getSavedBalance(player: OfflinePlayer) = player.profile.read(key).withoutDoubleTail()

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

private const val BALANCE_LOCK_STRIPES = 64

private const val MAX_EXACT_SCALE = 20

private const val LEGACY_TAIL_SCALE = 12

private val balanceLocks = Array(BALANCE_LOCK_STRIPES) { Any() }

fun OfflinePlayer.getBalance(currency: Currency): BigDecimal =
    this.profile.read(currency.key).withoutDoubleTail()

fun OfflinePlayer.setBalance(currency: Currency, value: BigDecimal) {
    this.changeBalance(currency) { value }
}

fun OfflinePlayer.adjustBalance(currency: Currency, by: BigDecimal) {
    this.changeBalance(currency) { it + by }
}

/**
 * Replace the balance with [change] of the current balance in one atomic step, so concurrent
 * changes cannot overwrite each other. When [change] returns null the balance is left as it is.
 *
 * @return If the balance was changed.
 */
fun OfflinePlayer.changeBalance(currency: Currency, change: (BigDecimal) -> BigDecimal?): Boolean {
    val (previousBalance, newBalance) = synchronized(balanceLocks[balanceLockIndex(currency)]) {
        val current = this.getBalance(currency)
        val value = change(current) ?: return false

        val coerced = if (currency.max == null) value.coerceAtLeast(BigDecimal.ZERO)
        else value.coerceIn(BigDecimal.ZERO..currency.max)

        this.profile.write(currency.key, coerced)

        current to coerced
    }

    if (newBalance > previousBalance) {
        // Listeners act on the player, whose region the caller may not own on Folia.
        this.runOwnedIfOnline {
            Bukkit.getPluginManager().callEvent(
                CurrencyGainEvent(this, currency, newBalance - previousBalance, newBalance)
            )
        }
    }

    return true
}

enum class PaymentResult {
    PAID,
    CANNOT_AFFORD,
    OVER_MAX
}

/**
 * Move [amount] from this player to [recipient] in one atomic step, holding both balances, so no
 * other change can land between the debit and the credit.
 */
fun OfflinePlayer.payBalance(recipient: OfflinePlayer, currency: Currency, amount: BigDecimal): PaymentResult {
    val lockIndices = listOf(this.balanceLockIndex(currency), recipient.balanceLockIndex(currency)).sorted()
    var recipientBalance = BigDecimal.ZERO

    val result = synchronized(balanceLocks[lockIndices.first()]) {
        synchronized(balanceLocks[lockIndices.last()]) {
            val senderBalance = this.getBalance(currency)
            recipientBalance = recipient.getBalance(currency) + amount

            if (senderBalance < amount) {
                PaymentResult.CANNOT_AFFORD
            } else if (currency.max != null && recipientBalance > currency.max) {
                PaymentResult.OVER_MAX
            } else {
                this.profile.write(currency.key, senderBalance - amount)
                recipient.profile.write(currency.key, recipientBalance)
                PaymentResult.PAID
            }
        }
    }

    if (result == PaymentResult.PAID) {
        recipient.runOwnedIfOnline {
            Bukkit.getPluginManager().callEvent(CurrencyGainEvent(recipient, currency, amount, recipientBalance))
        }
    }

    return result
}

private fun OfflinePlayer.balanceLockIndex(currency: Currency) =
    Math.floorMod(Objects.hash(this.uniqueId, currency.id), balanceLocks.size)

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
    val prefix = currency.prefix ?: currency.plugin.langYml.getPrefix()
    return prefix + this.getFormattedString("${LangYml.KEY_MESSAGES}.$key", option)
}

private fun BigDecimal.withoutDoubleTail(): BigDecimal =
    if (this.stripTrailingZeros().scale() > MAX_EXACT_SCALE) {
        this.setScale(LEGACY_TAIL_SCALE, RoundingMode.HALF_EVEN)
    } else {
        this
    }
