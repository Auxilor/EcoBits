package com.willfp.ecobits.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.formatWithCommas
import com.willfp.eco.util.savedDisplayName
import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.CurrenciesLeaderboard.getTop
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.decimalFormat
import com.willfp.ecobits.currencies.decimalFormatShort
import com.willfp.ecobits.currencies.format
import com.willfp.ecobits.currencies.formatShort
import com.willfp.ecobits.plugin
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class CommandTop(
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
                    .replace("%balance%", balance.decimalFormat(currency))
                    .replace("%balance_short%", balance.decimalFormatShort(currency))
                    .replace("%balance_formatted%", balance.format(currency))
                    .replace("%balance_formatted_short%", balance.formatShort(currency))
                    .replace("%balance_commas%", balance.formatWithCommas())
                    .replace("%balance_raw%", balance.toPlainString())
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
                        .replace("%symbol%", currency.symbol)
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