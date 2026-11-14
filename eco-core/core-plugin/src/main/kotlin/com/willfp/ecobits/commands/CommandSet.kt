package com.willfp.ecobits.commands

import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.setBalance
import com.willfp.ecobits.currencies.withCurrencyPlaceholders
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.game.entity.player.PlayerUtils

internal fun CommandBuilder<CommandSender>.setCommand(fixed: Currency?) = subcommand(
    name = "set",
    permission = "ecobits.command.set"
) {
    executes {
        val player = knownPlayer()
        val currency = currency(fixed, 1)
        val amount = amount(currency, amountIndex(fixed), positive = false)

        player.setBalance(currency, amount)

        sender.sendMessage(
            currencyMessage("set-currency", currency)
                .withCurrencyPlaceholders(amount, currency, PlayerUtils.getSavedDisplayName(player))
        )
    }

    completes {
        amountCommandCompletions(fixed)
    }
}
