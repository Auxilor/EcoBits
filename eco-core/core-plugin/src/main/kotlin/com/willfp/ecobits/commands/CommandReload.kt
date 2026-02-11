package com.willfp.ecobits.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.ecobits.plugin
import org.bukkit.command.CommandSender

object CommandReload : Subcommand(
    plugin,
    "reload",
    "ecobits.command.reload",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        plugin.reload()
        sender.sendMessage(plugin.langYml.getMessage("reloaded"))
    }
}
