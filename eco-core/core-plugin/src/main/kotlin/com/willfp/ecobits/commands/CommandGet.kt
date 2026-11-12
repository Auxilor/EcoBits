package com.willfp.ecobits.commands

import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.getBalance
import com.willfp.ecobits.currencies.withCurrencyPlaceholders
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.game.entity.player.PlayerUtils

internal fun CommandBuilder<CommandSender>.getCommand(fixed: Currency?) = subcommand(
    name = "get",
    permission = "ecobits.command.get"
) {
    executes {
        val player = knownPlayer()
        val currency = currency(fixed, 1)

        sender.sendMessage(
            currencyMessage("other-balance", currency).withCurrencyPlaceholders(
                player.getBalance(currency),
                currency,
                PlayerUtils.getSavedDisplayName(player)
            )
        )
    }

    completes {
        when {
            args.size == 1 -> playerNames()
            fixed == null && args.size == 2 -> Currencies.values().map { it.id }
            else -> emptyList()
        }
    }
}
