package com.willfp.ecobits.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender

class CommandEcoBits(plugin: EcoPlugin) : PluginCommand(plugin, "ecobits", "ecobits.command.ecobits", false) {
    init {
        this.addSubcommand(CommandReload(plugin))
            .addSubcommand(CommandGive(plugin))
            .addSubcommand(CommandGivesilent(plugin))
            .addSubcommand(CommandGet(plugin))
            .addSubcommand(CommandSet(plugin))
            .addSubcommand(CommandReset(plugin))
            .addSubcommand(CommandPay(plugin))
            .addSubcommand(CommandBalance(plugin))
            .addSubcommand(CommandTake(plugin))
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("invalid-command")
        )
    }
}
