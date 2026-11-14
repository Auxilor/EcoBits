package com.willfp.ecobits.commands

import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.PaymentResult
import com.willfp.ecobits.currencies.getCurrencyMessage
import com.willfp.ecobits.currencies.payBalance
import com.willfp.ecobits.currencies.withCurrencyPlaceholders
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.game.entity.player.PlayerUtils

internal fun CommandBuilder<CommandSender>.payCommand(fixed: Currency?) = playerSubcommand(
    name = "pay",
    permission = "ecobits.command.pay"
) {
    executes {
        pay(fixed)
    }

    completes {
        payCompletions(fixed)
    }
}

internal fun CommandContext<Player>.pay(fixed: Currency?) {
    @Suppress("DEPRECATION")
    val recipient = Bukkit.getOfflinePlayer(argument(0, "must-specify-player"))

    if (!recipient.hasPlayedBefore() && !recipient.isOnline || recipient.uniqueId == sender.uniqueId) {
        fail("invalid-player")
    }

    val currency = currency(fixed, 1) { it.isPayable }
    val amount = amount(currency, amountIndex(fixed), positive = true)

    val result = sender.payBalance(recipient, currency, amount)

    if (result != PaymentResult.PAID) {
        val key = if (result == PaymentResult.CANNOT_AFFORD) "cannot-afford" else "too-much"
        sender.sendMessage(plugin.langYml.getCurrencyMessage(key, currency))
        return
    }

    recipient.player?.sendMessage(
        currencyMessage("received-money", currency)
            .withCurrencyPlaceholders(amount, currency, PlayerUtils.getSavedDisplayName(sender))
    )

    sender.sendMessage(
        currencyMessage("paid-player", currency)
            .withCurrencyPlaceholders(amount, currency, PlayerUtils.getSavedDisplayName(recipient))
    )
}

internal fun CommandContext<*>.payCompletions(fixed: Currency?): List<String> =
    amountCommandCompletions(fixed, Currencies.values().filter { it.isPayable })
