package com.willfp.ecobits.commands

import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.decimalFormat
import com.willfp.ecobits.currencies.decimalFormatShort
import com.willfp.ecobits.currencies.format
import com.willfp.ecobits.currencies.formatShort
import com.willfp.ecobits.currencies.getBalance
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.data.playerbase.leaderboard.LeaderboardEntry
import wiki.ecohub.data.playerbase.leaderboard.Leaderboards
import wiki.ecohub.game.entity.player.PlayerUtils
import wiki.ecohub.math.formatWithCommas
import wiki.ecohub.ui.placeholder.placeholderContext
import wiki.ecohub.ui.text.formatEco

private val pageCompletions = listOf("1", "2", "3", "4", "5")

private const val PAGE_SIZE = 10

/**
 * Lines show each entry's exact balance, as the entry's double does not round-trip above 2^53.
 * Reading it goes through the player's profile, which loads from the database for anyone
 * offline, so the listing runs async.
 */
internal fun CommandBuilder<CommandSender>.topCommand(fixed: Currency?) = subcommand(
    name = "top",
    permission = "ecobits.command.top"
) {
    executes {
        if (!plugin.configYml.getBool("leaderboard.enabled")) {
            fail("invalid-command")
        }

        val currency = currency(fixed, 0)
        val page = args.getOrNull(pageIndex(fixed))?.toIntOrNull() ?: 1

        plugin.scheduler.async().run { sendTop(currency, page) }
    }

    completes {
        when {
            !plugin.configYml.getBool("leaderboard.enabled") -> emptyList()
            fixed == null && args.size == 1 -> Currencies.values().map { it.id }
            args.size == pageIndex(fixed) + 1 -> pageCompletions
            else -> emptyList()
        }
    }
}

private fun CommandContext<CommandSender>.sendTop(currency: Currency, page: Int) {
    val offset = (page - 1) * PAGE_SIZE
    val top = currency.leaderboard
        ?.let { leaderboard -> (offset + 1..offset + PAGE_SIZE).mapNotNull { leaderboard.getTop(it) } }
        .orEmpty()

    val lines = top.map { lineFor(it, currency) }.ifEmpty { listOf(Leaderboards.getNoRecordsMessage(plugin)) }

    val messages = plugin.langYml.getStrings("top.format").toMutableList()
    val linesIndex = messages.indexOf("%lines%")

    if (linesIndex != -1) {
        messages.removeAt(linesIndex)
        messages.addAll(linesIndex, lines)
    }

    val context = placeholderContext(player = sender as? Player)

    for (message in messages) {
        sender.sendMessage(
            message.formatEco(context)
                .replace(oldValue = "%currency%", newValue = currency.name)
                .replace(oldValue = "%symbol%", newValue = currency.symbol)
        )
    }
}

private fun pageIndex(fixed: Currency?): Int = if (fixed == null) 1 else 0

private fun CommandContext<CommandSender>.lineFor(entry: LeaderboardEntry, currency: Currency): String {
    val balance = entry.player.getBalance(currency)

    return plugin.langYml.getString("top-line-format")
        .replace(oldValue = "%rank%", newValue = entry.position.toString())
        .replace(oldValue = "%balance%", newValue = balance.decimalFormat(currency))
        .replace(oldValue = "%balance_short%", newValue = balance.decimalFormatShort(currency))
        .replace(oldValue = "%balance_formatted%", newValue = balance.format(currency))
        .replace(oldValue = "%balance_formatted_short%", newValue = balance.formatShort(currency))
        .replace(oldValue = "%balance_commas%", newValue = balance.formatWithCommas())
        .replace(oldValue = "%balance_raw%", newValue = balance.toPlainString())
        .replace(oldValue = "%balance_integer%", newValue = balance.toInt().toString())
        .replace(oldValue = "%player%", newValue = PlayerUtils.getSavedDisplayName(entry.player))
}
