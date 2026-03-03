package com.willfp.ecobits.commands

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.plugin
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DynamicCurrencyCommand(
    label: String,
    val currency: Currency
) : PluginCommand(
    plugin,
    label,
    "ecobits.command.${currency.id}",
    false
) {
    private val balanceCommand = if (this.currency.hasShortBalanceCommand) CommandBalance(this.currency)
        else null

    init {
        this.addSubcommand(CommandGive(currency))
            .addSubcommand(CommandGivesilent(currency))
            .addSubcommand(CommandGet(currency))
            .addSubcommand(CommandSet(currency))
            .addSubcommand(CommandReset(currency))
            .addSubcommand(CommandPay(currency))
            .addSubcommand(balanceCommand ?: CommandBalance(currency))
            .addSubcommand(CommandTake(currency))
            .addSubcommand(CommandTakesilent(currency))
        if (plugin.configYml.getBool("leaderboard.enabled"))
            this.addSubcommand(CommandTop(currency))
    }

    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        if (balanceCommand != null && sender is Player) {
            balanceCommand.onExecute(sender, args)
        } else {
            sender.sendMessage(
                plugin.langYml.getMessage("invalid-command")
            )
        }
    }
}
