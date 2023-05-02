package com.willfp.ecobits.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.containsIgnoreCase
import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class DynamicCurrencyCommand(
    plugin: EcoPlugin,
    label: String,
    val currency: Currency
) : PluginCommand(
    plugin,
    label,
    "ecobits.command.${currency.id}",
    false
) {
    init {
        this.addSubcommand(CommandGive(plugin, currency))
            .addSubcommand(CommandGivesilent(plugin, currency))
            .addSubcommand(CommandGet(plugin, currency))
            .addSubcommand(CommandSet(plugin, currency))
            .addSubcommand(CommandReset(plugin, currency))
            .addSubcommand(CommandPay(plugin, currency))
            .addSubcommand(CommandBalance(plugin, currency))
            .addSubcommand(CommandTake(plugin, currency))
            .addSubcommand(CommandTakesilent(plugin, currency))
    }

    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("invalid-command")
        )
    }
}
