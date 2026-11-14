package com.willfp.ecobits.commands

import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.decimalFormat
import com.willfp.ecobits.currencies.decimalFormatShort
import com.willfp.ecobits.currencies.format
import com.willfp.ecobits.currencies.formatShort
import com.willfp.ecobits.currencies.getBalance
import com.willfp.ecobits.currencies.getCurrencyMessage
import com.willfp.ecobits.plugin
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.game.entity.player.PlayerUtils
import wiki.ecohub.ui.text.StringUtils

internal fun CommandBuilder<CommandSender>.balanceCommand(fixed: Currency?) = playerSubcommand(
    name = "balance",
    permission = "ecobits.command.balance"
) {
    executes {
        sendBalance(sender, currency(fixed, 0))
    }

    completes {
        if (fixed == null && args.size == 1) {
            Currencies.values().map { it.id }
        } else {
            emptyList()
        }
    }
}

internal fun sendBalance(player: Player, currency: Currency) {
    val balance = player.getBalance(currency)

    player.sendMessage(
        plugin.langYml.getCurrencyMessage("balance", currency, StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
            .replace(oldValue = "%player%", newValue = PlayerUtils.getSavedDisplayName(player))
            .replace(oldValue = "%amount%", newValue = balance.decimalFormat(currency))
            .replace(oldValue = "%amount_short%", newValue = balance.decimalFormatShort(currency))
            .replace(oldValue = "%amount_formatted%", newValue = balance.format(currency))
            .replace(oldValue = "%amount_formatted_short%", newValue = balance.formatShort(currency))
            .replace(oldValue = "%currency%", newValue = currency.name)
            .replace(oldValue = "%currency_symbol%", newValue = currency.symbol)
    )
}
