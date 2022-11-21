package com.willfp.ecobits.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNiceString
import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.getBalance
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class CommandBalance(
    plugin: EcoPlugin
) : Subcommand(
    plugin,
    "balance",
    "ecobits.command.balance",
    true
) {
    override fun onExecute(player: Player, args: List<String>) {
        if (args.isEmpty()) {
            player.sendMessage(plugin.langYml.getMessage("must-specify-currency"))
            return
        }

        val currency = Currencies.getByID(args[0].lowercase())

        if (currency == null) {
            player.sendMessage(plugin.langYml.getMessage("invalid-currency"))
            return
        }

        player.sendMessage(
            plugin.langYml.getMessage("balance", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%amount%", player.getBalance(currency).toNiceString())
                .replace("%currency%", currency.name)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.isEmpty()) {
            Currencies.values().map { it.id }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Currencies.values().map { it.id },
                completions
            )
        }

        return completions
    }
}
