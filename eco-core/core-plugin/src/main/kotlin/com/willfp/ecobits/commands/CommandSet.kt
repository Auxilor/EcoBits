package com.willfp.ecobits.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import com.willfp.ecobits.EcoBitsPlugin
import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.setBalance
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class CommandSet(
        plugin: EcoPlugin,
        private val currency: Currency? = null
) : Subcommand(
        plugin,
        "set",
        "ecobits.command.set",
        false
) {
    private val argOffset = if (currency == null) 0 else -1

    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("must-specify-player"))
            return
        }

        val allowOfflinePlayers = EcoBitsPlugin.instance.allowOfflinePlayers
        val targetPlayer: OfflinePlayer? = if (allowOfflinePlayers) {
            Bukkit.getOfflinePlayer(args[0])
        } else {
            Bukkit.getPlayer(args[0])
        }

        // Don't allow the usage on offline players
        if (targetPlayer == null || (!allowOfflinePlayers && !targetPlayer.isOnline)) {
            sender.sendMessage(plugin.langYml.getMessage("player-not-online"))
            return
        }

        // Don't allow the usage on player that don't exist
        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline) {
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

        val amount = args[2 + argOffset].toDoubleOrNull()

        if (amount == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-amount"))
            return
        }

        targetPlayer.setBalance(currency, amount.toBigDecimal())

        sender.sendMessage(
                plugin.langYml.getMessage("set-currency", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                        .replace("%player%", targetPlayer.savedDisplayName)
                        .replace("%amount%", amount.toNiceString())
                        .replace("%currency%", currency.name)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.isEmpty()) {
            return if (EcoBitsPlugin.instance.allowOfflinePlayers) {
                Bukkit.getOfflinePlayers().mapNotNull { it.name }
            } else {
                Bukkit.getOnlinePlayers().map { it.name }
            }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                    args[0],
                    if (EcoBitsPlugin.instance.allowOfflinePlayers) {
                        Bukkit.getOfflinePlayers().map { it.name }
                    } else {
                        Bukkit.getOnlinePlayers().map { it.name }
                    },
                    completions
            )
        }

        if (this.currency == null && args.size == 2) {
            StringUtil.copyPartialMatches(
                    args[1],
                    Currencies.values().map { it.id },
                    completions
            )
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
