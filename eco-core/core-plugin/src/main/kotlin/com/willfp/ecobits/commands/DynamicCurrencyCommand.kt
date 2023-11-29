package com.willfp.ecobits.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecobits.currencies.Currency
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DynamicCurrencyCommand(
    plugin: EcoPlugin,
    label: String,
    val currency: Currency
) : PluginCommand(
    plugin,
    label,
    "ecobits.command.${currency.id}",
    false
) {
    private val balanceCommand = if (this.currency.hasShortBalanceCommand) CommandBalance(this.plugin, this.currency)
        else null

    init {
        this.addSubcommand(CommandGive(plugin, currency))
            .addSubcommand(CommandGivesilent(plugin, currency))
            .addSubcommand(CommandGet(plugin, currency))
            .addSubcommand(CommandSet(plugin, currency))
            .addSubcommand(CommandReset(plugin, currency))
            .addSubcommand(CommandPay(plugin, currency))
            .addSubcommand(balanceCommand ?: CommandBalance(plugin, currency))
            .addSubcommand(CommandTake(plugin, currency))
            .addSubcommand(CommandTakesilent(plugin, currency))
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
