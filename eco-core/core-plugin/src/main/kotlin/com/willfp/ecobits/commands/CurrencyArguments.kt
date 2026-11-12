package com.willfp.ecobits.commands

import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.getCurrencyMessage
import com.willfp.ecobits.currencies.hasDecimals
import com.willfp.ecobits.currencies.numOfDecimals
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.ui.text.StringUtils
import java.math.BigDecimal

internal val amountCompletions = listOf("1", "2", "3", "4", "5")

/**
 * The index of the amount argument: after the player, and after the currency unless the command
 * is bound to [fixed].
 */
internal fun amountIndex(fixed: Currency?): Int = if (fixed == null) 2 else 1

/**
 * The player named by argument 0, if they are online or have joined before.
 */
internal fun CommandContext<*>.knownPlayer(): OfflinePlayer {
    @Suppress("DEPRECATION")
    val player = Bukkit.getOfflinePlayer(argument(0, "must-specify-player"))

    if (!player.hasPlayedBefore() && !player.isOnline) {
        fail("invalid-player")
    }

    return player
}

/**
 * [fixed], or the currency named by argument [index], passing [filter].
 */
internal fun CommandContext<*>.currency(
    fixed: Currency?,
    index: Int,
    filter: (Currency) -> Boolean = { true }
): Currency {
    val currency = fixed ?: Currencies.getByID(argument(index, "must-specify-currency").lowercase())
    return currency?.takeIf(filter) ?: fail("invalid-currency")
}

/**
 * The amount in argument [index], valid for [currency]; above zero when [positive].
 */
internal fun CommandContext<*>.amount(currency: Currency, index: Int, positive: Boolean): BigDecimal {
    val amount = argument(index, "must-specify-amount").toBigDecimalOrNull()
        ?.takeIf { !positive || it.signum() > 0 }
        ?: fail("invalid-amount")

    if (amount.hasDecimals() && !currency.isDecimal) {
        fail("invalid-amount")
    }

    val maxDecimals = currency.maxDecimals

    if (currency.isDecimal && maxDecimals != null && amount.numOfDecimals() > maxDecimals) {
        fail("invalid-amount")
    }

    return amount
}

/**
 * The lang message [key] for [currency], placeholders left untranslated.
 */
internal fun CommandContext<*>.currencyMessage(key: String, currency: Currency): String =
    plugin.langYml.getCurrencyMessage(key, currency, StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)

/**
 * Completions for `<player> [currency] <amount>`.
 */
internal fun CommandContext<*>.amountCommandCompletions(
    fixed: Currency?,
    currencies: List<Currency> = Currencies.values()
): List<String> = when {
    args.size == 1 -> playerNames()
    fixed == null && args.size == 2 -> currencies.map { it.id }
    args.size == amountIndex(fixed) + 1 -> amountCompletions
    else -> emptyList()
}
