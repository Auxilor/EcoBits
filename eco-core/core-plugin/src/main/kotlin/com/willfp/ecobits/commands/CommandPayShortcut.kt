package com.willfp.ecobits.commands

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.plugin
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandPayShortcut(
    currency: Currency
) : PluginCommand(
    plugin,
    "pay",
    "ecobits.command.pay",
    true
) {
    private val payCommand = CommandPay(currency)

    override fun onExecute(player: Player, args: List<String>) {
        payCommand.onExecute(player, args)
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return payCommand.tabComplete(sender, args)
    }
}
