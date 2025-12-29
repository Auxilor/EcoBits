package com.willfp.ecobits.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.formatWithCommas
import com.willfp.ecobits.currencies.formatWithExtension
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class CommandTop(
    plugin: EcoPlugin,
    private val currency: Currency? = null
) : Subcommand(
    plugin,
    "top",
    "ecobits.command.top",
    false
) {
    private val argOffset = if (currency == null) 0 else -1

    override fun onExecute(sender: CommandSender, args: List<String>) {
        plugin.scheduler.runAsync {
            val currency = if (this.currency == null) {
                if (args.isEmpty()) {
                    sender.sendMessage(plugin.langYml.getMessage("must-specify-currency"))
                    return@runAsync
                }
                Currencies.getByID(args[0].lowercase())
            } else {
                this.currency
            }

            if (currency == null) {
                sender.sendMessage(plugin.langYml.getMessage("invalid-currency"))
                return@runAsync
            }

            val page = if (args.size > 1 + argOffset) {
                args[1 + argOffset].toIntOrNull() ?: 1
            } else {
                1
            }

            val offset = (page - 1) * 10
            val positions = ((offset + 1)..(offset + 10)).toList()
            val top = positions.mapNotNull { currency.getTop(it) }

            val messages = plugin.langYml.getStrings("top.format")
            val lines = mutableListOf<String>()

            for ((index, entry) in top.withIndex()) {
                val (player, balance) = entry

                val line = plugin.langYml.getString("top-line-format")
                    .replace("%rank%", (offset + index + 1).toString())
                    .replace("%balance%", balance.toNiceString())
                    .replace("%balance_commas%", balance.formatWithCommas())
                    .replace("%balance_formatted%", balance.formatWithExtension())
                    .replace("%balance_integer%", balance.toInt().toString())
                    .replace("%player%", player.savedDisplayName)

                lines.add(line)
            }

            val linesIndex = messages.indexOf("%lines%")
            if (linesIndex != -1) {
                messages.removeAt(linesIndex)
                messages.addAll(linesIndex, lines)
            }

            for (message in messages) {
                sender.sendMessage(
                    message.formatEco(
                        placeholderContext(
                            player = sender as? Player
                        )
                    ).replace("%currency%", currency.name)
                )
            }
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (this.currency == null) {
            if (args.size == 1) {
                StringUtil.copyPartialMatches(
                    args[0],
                    Currencies.values().map { it.id },
                    completions
                )
            }
        }

        if (args.size == 2 + argOffset) {
            StringUtil.copyPartialMatches(
                args[1 + argOffset],
                listOf(1, 2, 3, 4, 5).map { it.toString() },
                completions
            )
        }

        return completions
    }
}