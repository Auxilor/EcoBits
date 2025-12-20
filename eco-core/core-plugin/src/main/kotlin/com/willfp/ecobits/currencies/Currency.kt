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
import com.willfp.eco.util.toNiceString
import com.willfp.ecobits.EcoBitsPlugin
import com.willfp.ecobits.commands.DynamicCurrencyCommand
import com.willfp.ecobits.integrations.IntegrationVault
import com.willfp.ecobits.util.LeaderboardPlace
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.ServicePriority
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

class Currency(
    val id: String,
    val plugin: EcoBitsPlugin,
    val config: Config
) {
    private val leaderboardCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build<Boolean, List<UUID>> {
            Bukkit.getOfflinePlayers().sortedByDescending {
                getBalance(it)
            }.map { it.uniqueId }
        }

    fun getTop(position: Int): LeaderboardPlace? {
        require(position > 0) { "Position must be greater than 0" }

        val uuid = leaderboardCache.get(true).getOrNull(position - 1) ?: return null

        val player = Bukkit.getOfflinePlayer(uuid).takeIf { it.hasPlayedBefore() } ?: return null

        return LeaderboardPlace(
            player,
            getBalance(player)
        )
    }

    fun getPosition(uuid: UUID): Int? {
        val leaderboard = leaderboardCache.get(true)
        val index = leaderboard.indexOf(uuid)
        return if (index == -1) null else index + 1
    }

    private val descCache = Caffeine.newBuilder()
        .expireAfterWrite(plugin.configYml.getInt("gui.cache-ttl").toLong(), TimeUnit.MILLISECONDS)
        .build<Int, String>()

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
            PlayerPlaceholder(
                plugin,
                "${id}_integer"
            ) {
                it.getBalance(this).toInt().toString()
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerPlaceholder(
                plugin,
                "${id}_leaderboard_rank")
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
