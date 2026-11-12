package com.willfp.ecobits.commands

import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.adjustBalance
import com.willfp.ecobits.currencies.withCurrencyPlaceholders
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.core.command.CommandFailure
import wiki.ecohub.game.entity.player.PlayerUtils
import java.math.BigDecimal

private class Adjustment(val player: OfflinePlayer, val currency: Currency, val amount: BigDecimal)

/**
 * give / take, and their silent versions, which send nothing at all.
 */
internal fun CommandBuilder<CommandSender>.adjustCommand(
    name: String,
    fixed: Currency?,
    silent: Boolean,
    negative: Boolean
) = subcommand(name = name, permission = "ecobits.command.$name") {
    executes {
        val adjustment = try {
            adjust(fixed, negative)
        } catch (failure: CommandFailure) {
            if (silent) null else throw failure
        }

        if (adjustment != null && !silent) {
            notifyAdjusted(adjustment, negative)
        }
    }

    completes {
        amountCommandCompletions(fixed)
    }
}

private fun CommandContext<CommandSender>.adjust(fixed: Currency?, negative: Boolean): Adjustment {
    val player = knownPlayer()
    val currency = currency(fixed, 1)
    val amount = amount(currency, amountIndex(fixed), positive = true)

    player.adjustBalance(currency, if (negative) -amount else amount)

    return Adjustment(player, currency, amount)
}

private fun CommandContext<CommandSender>.notifyAdjusted(adjustment: Adjustment, negative: Boolean) {
    val (senderKey, playerKey) = if (negative) {
        "took-currency" to "lost-currency"
    } else {
        "gave-currency" to "received-currency"
    }

    val player = adjustment.player
    val currency = adjustment.currency

    sender.sendMessage(
        currencyMessage(senderKey, currency)
            .withCurrencyPlaceholders(adjustment.amount, currency, PlayerUtils.getSavedDisplayName(player))
    )

    player.player?.sendMessage(
        currencyMessage(playerKey, currency).withCurrencyPlaceholders(adjustment.amount, currency)
    )
}
