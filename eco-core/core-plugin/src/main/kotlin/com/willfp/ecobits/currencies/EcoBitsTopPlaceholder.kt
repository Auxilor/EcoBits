package com.willfp.ecobits.currencies

import com.willfp.ecobits.plugin
import wiki.ecohub.core.plugin.EcoPlugin
import wiki.ecohub.game.entity.player.PlayerUtils
import wiki.ecohub.math.formatWithCommas
import wiki.ecohub.ui.placeholder.PlaceholderContext
import wiki.ecohub.ui.placeholder.RegistrablePlaceholder
import wiki.ecohub.ui.text.formatEco
import java.util.regex.Pattern

object EcoBitsTopPlaceholder : RegistrablePlaceholder {
    private val pattern =
        Pattern.compile("top_([a-z0-9_]+)_(\\d+)_(name|amount)(?:_(commas|formatted|raw|integer|short|formatted_short))?")

    override fun getPattern(): Pattern = pattern
    override fun getPlugin(): EcoPlugin = plugin

    override fun getValue(params: String, ctx: PlaceholderContext): String? {
        val emptyPosition: String = plugin.langYml.getString("top.empty-position").formatEco()
        val matcher = pattern.matcher(params)

        if (!matcher.matches()) return null

        if (!plugin.configYml.getBool("leaderboard.enabled")) return null

        val currencyId = matcher.group(1)
        val place = matcher.group(2).toIntOrNull() ?: return null
        val type = matcher.group(3)
        val formatType = matcher.group(4)

        val currency = Currencies.getByID(currencyId) ?: return null
        val topEntry = currency.leaderboard?.getTop(place) ?: return emptyPosition
        val player = topEntry.player

        return when (type) {
            "name" -> PlayerUtils.getSavedDisplayName(player)
            "amount" -> {
                // Read the exact BigDecimal rather than the entry's double: the leaderboard ranks
                // on a double, which does not round-trip a balance above 2^53.
                val amount = player.getBalance(currency)
                when (formatType) {
                    "short" -> amount.decimalFormatShort(currency)
                    "formatted" -> amount.format(currency)
                    "formatted_short" -> amount.formatShort(currency)
                    "commas" -> amount.formatWithCommas()
                    "integer" -> amount.toInt().toString()
                    "raw" -> amount.toPlainString()
                    else -> amount.decimalFormat(currency)
                }
            }

            else -> null
        }
    }
}