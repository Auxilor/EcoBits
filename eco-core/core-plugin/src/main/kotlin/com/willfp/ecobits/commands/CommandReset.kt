package com.willfp.ecobits.commands

import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.setBalance
import com.willfp.ecobits.currencies.withCurrencyPlaceholders
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.data.playerbase.forEachSavedProfile
import wiki.ecohub.game.entity.player.PlayerUtils
import java.util.logging.Level

internal fun CommandBuilder<CommandSender>.resetCommand(fixed: Currency?) = subcommand(
    name = "reset",
    permission = "ecobits.command.reset"
) {
    executes {
        if (argument(0, "must-specify-player").equals("all", true)) {
            resetAllPlayers(resetCurrency(fixed))
        } else {
            resetPlayer(fixed)
        }
    }

    completes {
        when {
            args.size == 1 -> Bukkit.getOfflinePlayers().mapNotNull { it.name } + "all"
            fixed == null && args.size == 2 -> Currencies.values().map { it.id }
            else -> emptyList()
        }
    }
}

private fun CommandContext<CommandSender>.resetPlayer(fixed: Currency?) {
    val player = knownPlayer()
    val currency = resetCurrency(fixed)

    player.setBalance(currency, currency.default)

    if (player.name != null) {
        sender.sendMessage(
            currencyMessage("reset-currency", currency)
                .withCurrencyPlaceholders(currency.default, currency, PlayerUtils.getSavedDisplayName(player))
        )
    }
}

private fun CommandContext<CommandSender>.resetCurrency(fixed: Currency?): Currency =
    fixed ?: args.getOrNull(1)?.let { Currencies.getByID(it.lowercase()) } ?: fail("invalid-currency")

private fun CommandContext<CommandSender>.resetAllPlayers(currency: Currency) {
    forEachSavedProfile(plugin) { uuid ->
        Bukkit.getOfflinePlayer(uuid).setBalance(currency, currency.default)
    }.thenRun {
        plugin.scheduler.global().run {
            sender.sendMessage(
                currencyMessage("reset-all-currency", currency).withCurrencyPlaceholders(currency.default, currency)
            )
        }
    }.exceptionally { error ->
        plugin.logger.log(Level.SEVERE, "Failed to reset ${currency.id} for every player", error)
        null
    }
}
