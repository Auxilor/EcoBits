package com.willfp.ecobits.commands

import com.willfp.ecobits.EcoBitsPlugin
import com.willfp.ecobits.currencies.Currency
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.command

internal fun EcoBitsPlugin.ecoBitsCommand() = command(name = "ecobits", permission = "ecobits.command.ecobits") {
    reloadCommand()
    currencySubcommands(null)
}

/**
 * The currency subcommands, bound to [currency] or taking it as an argument when null.
 */
internal fun CommandBuilder<CommandSender>.currencySubcommands(currency: Currency?) {
    adjustCommand("give", currency, silent = false, negative = false)
    adjustCommand("givesilent", currency, silent = true, negative = false)
    getCommand(currency)
    setCommand(currency)
    resetCommand(currency)
    payCommand(currency)
    balanceCommand(currency)
    adjustCommand("take", currency, silent = false, negative = true)
    adjustCommand("takesilent", currency, silent = true, negative = true)
    topCommand(currency)
}
