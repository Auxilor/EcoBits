package com.willfp.ecobits.commands

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecobits.plugin
import org.bukkit.command.CommandSender

object CommandEcoBits : PluginCommand(
    plugin, "ecobits",
    "ecobits.command.ecobits",
    false
) {
    init {
        this.addSubcommand(CommandReload)
            .addSubcommand(CommandGive())
            .addSubcommand(CommandGivesilent())
            .addSubcommand(CommandGet())
            .addSubcommand(CommandSet())
            .addSubcommand(CommandReset())
            .addSubcommand(CommandPay())
            .addSubcommand(CommandBalance())
            .addSubcommand(CommandTake())
            .addSubcommand(CommandTakesilent())
        if (plugin.configYml.getBool("leaderboard.enabled"))
            this.addSubcommand(CommandTop())
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("invalid-command")
        )
    }
}
