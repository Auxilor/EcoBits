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
): PluginCommand(
    plugin,
    label,
    "ecobits.command.${currency.id}",
    false
) {
    init {
        DynamicCurrencySubcommand.allCommands.forEach {
            this.addSubcommand(
                DynamicCurrencySubcommand(plugin, it, currency)
            )
        }
    }

    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        sender.sendMessage(this.plugin.langYml.getMessage("invalid-command"))
    }


    class DynamicCurrencySubcommand(
        plugin: EcoPlugin,
        val command: String,
        val currency: Currency
    ): Subcommand(
        plugin,
        command,
        "ecobits.command.${currency.id}.$command",
        false
    ) {
        override fun onExecute(sender: CommandSender, args: MutableList<String>) {
            super.onExecute(sender, args)
        }

        override fun onExecute(sender: Player, args: MutableList<String>) {
            val format = when {
                currencyCommands.containsIgnoreCase(command) -> currencyFormat
                playerCurrencyCommands.containsIgnoreCase(command) -> playerCurrencyFormat
                playerCurrencyAmountCommands.containsIgnoreCase(command) -> playerCurrencyAmountFormat
                else -> ""
            }

            Bukkit.dispatchCommand(sender,
                format.replace(
                    "%command%", command
                ).replace(
                    "%player%", args.getOrElse(0) { "" }
                ).replace(
                    "%amount%", args.getOrElse(1) { "" }
                ).replace(
                    "%currency%", currency.id
                )
            )
        }

        override fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
            return when {
                args.size == 1 -> {
                    if (currencyCommands.containsIgnoreCase(command)) {
                        StringUtil.copyPartialMatches(args.first(),
                            Currencies.values().map { it.id }, mutableListOf())
                    } else {
                        StringUtil.copyPartialMatches(args.first(),
                            Bukkit.getOnlinePlayers().map { it.name }, mutableListOf())
                    }
                }

                args.size == 2 && !currencyCommands.containsIgnoreCase(command) -> {
                    StringUtil.copyPartialMatches(args.first(),
                        Currencies.values().map { it.id }, mutableListOf())
                }

                else -> mutableListOf()
            }
        }

        companion object {
            val currencyCommands = listOf("balance")
            val playerCurrencyCommands = listOf("get", "reset")
            val playerCurrencyAmountCommands = listOf("give", "givesilent", "pay", "set", "take", "takesilent")
            val allCommands = currencyCommands + playerCurrencyCommands + playerCurrencyAmountCommands
            val currencyFormat = "ecobits %command% %currency%"
            val playerCurrencyFormat = "ecobits %command% %player% %currency%"
            val playerCurrencyAmountFormat = "ecobits %command% %player% %currency% %amount%"
        }
    }
}