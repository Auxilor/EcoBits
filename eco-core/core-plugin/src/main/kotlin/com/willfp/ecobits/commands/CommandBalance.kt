package com.willfp.ecobits.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.savedDisplayName
import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.decimalFormat
import com.willfp.ecobits.currencies.decimalFormatShort
import com.willfp.ecobits.currencies.format
import com.willfp.ecobits.currencies.formatShort
import com.willfp.ecobits.currencies.getBalance
import com.willfp.ecobits.plugin
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class CommandBalance(
    private val currency: Currency? = null
) : Subcommand(
    plugin,
    "balance",
    "ecobits.command.balance",
    true
) {
    override fun onExecute(player: Player, args: List<String>) {
        if (this.currency == null) {
            if (args.isEmpty()) {
                player.sendMessage(plugin.langYml.getMessage("must-specify-currency"))
                return
            }
        }

        val currency = this.currency ?: Currencies.getByID(args[0].lowercase())

        if (currency == null) {
            player.sendMessage(plugin.langYml.getMessage("invalid-currency"))
            return
        }

        player.sendMessage(
            plugin.langYml.getMessage("balance", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%player%", player.savedDisplayName)
                .replace("%amount%", player.getBalance(currency).decimalFormat(currency))
                .replace("%amount_short%", player.getBalance(currency).decimalFormatShort(currency))
                .replace("%amount_formatted%", player.getBalance(currency).format(currency))
                .replace("%amount_formatted_short%", player.getBalance(currency).formatShort(currency))
                .replace("%currency%", currency.name)
                .replace("%currency_symbol%", currency.symbol)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (this.currency == null) {
            if (args.isEmpty()) {
                return Currencies.values().map { it.id }
            }

            if (args.size == 1) {
                StringUtil.copyPartialMatches(
                    args[0],
                    Currencies.values().map { it.id },
                    completions
                )
            }
        }

        return completions
    }
}
