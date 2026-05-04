package com.willfp.ecobits.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.savedDisplayName
import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.adjustBalance
import com.willfp.ecobits.currencies.decimalFormat
import com.willfp.ecobits.currencies.decimalFormatShort
import com.willfp.ecobits.currencies.format
import com.willfp.ecobits.currencies.formatShort
import com.willfp.ecobits.currencies.hasDecimals
import com.willfp.ecobits.currencies.numOfDecimals
import com.willfp.ecobits.plugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class CommandGive(
    private val currency: Currency? = null
) : Subcommand(
    plugin,
    "give",
    "ecobits.command.give",
    false
) {
    private val argOffset = if (currency == null) 0 else -1

    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("must-specify-player"))
            return
        }

        val player = Bukkit.getPlayer(args[0])
            ?: Bukkit.getOfflinePlayers().firstOrNull {
                it.name.equals(args[0], ignoreCase = true)
            }
            ?: run {
                sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
                return
            }

        if (this.currency == null) {
            if (args.size < 2) {
                sender.sendMessage(plugin.langYml.getMessage("must-specify-currency"))
                return
            }
        }

        val currency = this.currency ?: Currencies.getByID(args[1].lowercase())

        if (currency == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-currency"))
            return
        }

        if (args.size < 3 + argOffset) {
            sender.sendMessage(plugin.langYml.getMessage("must-specify-amount"))
            return
        }

        val amount = args[2 + argOffset].toBigDecimalOrNull()

        if (amount == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-amount"))
            return
        }

        if (amount.hasDecimals() && !currency.isDecimal) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-amount"))
            return
        }

        if (currency.maxDecimals != null && amount.numOfDecimals() > currency.maxDecimals && currency.isDecimal) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-amount"))
            return
        }

        player.adjustBalance(currency, amount)

        sender.sendMessage(
            plugin.langYml.getMessage("gave-currency", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%player%", player.savedDisplayName)
                .replace("%amount%", amount.decimalFormat(currency))
                .replace("%amount_short%", amount.decimalFormatShort(currency))
                .replace("%amount_formatted%", amount.format(currency))
                .replace("%amount_formatted_short%", amount.formatShort(currency))
                .replace("%amount_raw%", amount.toPlainString())
                .replace("%amount_integer%", amount.toInt().toString())
                .replace("%currency%", currency.name)
                .replace("%symbol%", currency.symbol)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.isEmpty()) {
            return Bukkit.getOnlinePlayers().map { it.name }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Bukkit.getOnlinePlayers().map { it.name },
                completions
            )
        }

        if (this.currency == null) {
            if (args.size == 2) {
                StringUtil.copyPartialMatches(
                    args[1],
                    Currencies.values().map { it.id },
                    completions
                )
            }
        }

        if (args.size == 3 + argOffset) {
            StringUtil.copyPartialMatches(
                args[2 + argOffset],
                arrayOf(1, 2, 3, 4, 5).map { it.toString() },
                completions
            )
        }

        return completions
    }
}
