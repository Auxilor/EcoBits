@file:JvmName("CurrencyUtils")

package com.willfp.ecobits.currencies

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.economy.EconomyManager
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.DynamicPlaceholder
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.price.Prices
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import com.willfp.ecobits.EcoBitsPlugin
import com.willfp.ecobits.commands.DynamicCurrencyCommand
import com.willfp.ecobits.integrations.IntegrationVault
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.ServicePriority
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.Duration
import java.util.Optional
import java.util.regex.Pattern
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

class Currency(
    val id: String,
    val plugin: EcoBitsPlugin,
    val config: Config
) {
    val leaderboardCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(plugin.configYml.getInt("cache-expire-after").toLong()))
        .build<Int, Optional<LeaderboardPlace>>()

    val default = BigDecimal(config.getDouble("default"))

    val name = config.getFormattedString("name")

    val max: BigDecimal? = if (config.has("max") && config.getDouble("max") > 0)
        BigDecimal(config.getDouble("max"))
    else null

    val isPayable = config.getBool("payable")

    val isDecimal = config.getBool("decimal")

    val isRegisteredWithVault = config.getBool("vault")

    val isLocal = config.getBool("local")

    val commands = config.getStrings("commands").map { DynamicCurrencyCommand(plugin, it, this) }

    val key = PersistentDataKey(
        plugin.createNamespacedKey(if (isLocal) "${plugin.serverID}_${id}" else id),
        PersistentDataKeyType.BIG_DECIMAL,
        default
    )

    fun getLeaderboardPlace(place: Int): LeaderboardPlace? {
        return leaderboardCache.get(place) {
            val top = Bukkit.getOfflinePlayers()
                .sortedByDescending { it.getBalance(this) }.getOrNull(place - 1)

            if (top == null) {
                Optional.empty()
            } else Optional.of(LeaderboardPlace(top, top.getBalance(this)))
        }.orElse(null)
    }

    private fun registerCommands() {
        this.commands.forEach { it.register() }
    }

    private fun unregisterCommands() {
        this.commands.forEach { it.unregister() }
    }

    init {
        PlaceholderManager.registerPlaceholder(
            DynamicPlaceholder(
                plugin,
                Pattern.compile("top_${id}_[0-9]+_[a-z]+_?[a-z]*"),
            ) { value ->
                val place = value.split("_").getOrNull(2)
                    ?.toIntOrNull() ?: return@DynamicPlaceholder ""

                val type = value.split("_").getOrNull(3)
                    ?: return@DynamicPlaceholder ""

                val raw = value.split("_").getOrNull(4)
                    ?.equals("raw", true) ?: true

                val placeObj = getLeaderboardPlace(place)

                return@DynamicPlaceholder when (type) {
                    "name" -> placeObj?.player?.savedDisplayName
                        ?: plugin.langYml.getFormattedString("top.name-empty")

                    "amount" -> (if (raw) placeObj?.amount.toNiceString() else placeObj?.amount?.formatWithExtension())
                        ?: plugin.langYml.getFormattedString("top.amount-empty")

                    else -> ""
                }
            }
        )

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
                "${id}_commas"
            ) {
                it.getBalance(this).formatWithCommas()
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
                ServicePriority.Highest
            )
        }

        this.unregisterCommands()
        this.registerCommands()
    }

    override fun equals(other: Any?): Boolean {
        return other is Currency && other.id == this.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}

data class LeaderboardPlace(
    val player: OfflinePlayer,
    val amount: BigDecimal
)

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

fun BigDecimal.formatWithCommas(): String {
    val stripped = this.stripTrailingZeros()
    val decimalFormat = if (stripped.scale() > 0) DecimalFormat("#,##0.00") else DecimalFormat("#,##0")
    return decimalFormat.format(stripped)
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
