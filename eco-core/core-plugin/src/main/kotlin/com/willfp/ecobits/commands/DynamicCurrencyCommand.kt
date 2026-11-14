package com.willfp.ecobits.commands

import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.plugin
import org.bukkit.entity.Player
import wiki.ecohub.core.command.EcoCommand
import wiki.ecohub.core.command.command
import wiki.ecohub.core.command.playerCommand

/**
 * A command bound to [currency]. With `balance-shorthand`, running it bare shows a player
 * their balance.
 */
internal fun dynamicCurrencyCommand(label: String, currency: Currency): EcoCommand =
    plugin.command(name = label, permission = "ecobits.command.${currency.id}") {
        currencySubcommands(currency)

        executes {
            val player = sender as? Player

            if (player == null || !currency.hasShortBalanceCommand) {
                fail("invalid-command")
            }

            sendBalance(player, currency)
        }
    }

/**
 * `/pay` for the currency registered with Vault.
 */
internal fun payShortcutCommand(currency: Currency): EcoCommand =
    plugin.playerCommand(name = "pay", permission = "ecobits.command.pay") {
        executes {
            pay(currency)
        }

        completes {
            payCompletions(currency)
        }
    }
