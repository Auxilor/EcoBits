package com.willfp.ecobits.currencies

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.placeholder.RegistrablePlaceholder
import com.willfp.eco.core.placeholder.context.PlaceholderContext
import com.willfp.eco.util.formatWithCommas
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import java.util.regex.Pattern

class EcoBitsTopPlaceholder(
    private val plugin: EcoPlugin
) : RegistrablePlaceholder {
    private val pattern = Pattern.compile("top_([a-z0-9_]+)_(\\d+)_(name|amount)(?:_(commas|formatted|integer))?")

    override fun getPattern(): Pattern = pattern
    override fun getPlugin(): EcoPlugin = plugin

    override fun getValue(params: String, ctx: PlaceholderContext): String? {
        val emptyPosition: String = plugin.langYml.getString("top.empty-position")
        val matcher = pattern.matcher(params)

        if (!matcher.matches()) return null

        val currencyId = matcher.group(1)
        val place = matcher.group(2).toIntOrNull() ?: return null
        val type = matcher.group(3)
        val formatType = matcher.group(4)

        val currency = Currencies.getByID(currencyId) ?: return null
        val topEntry = currency.getTop(place) ?: return emptyPosition

        return when (type) {
            "name" -> topEntry.player?.savedDisplayName ?: emptyPosition
            "amount" -> {
                val amount = topEntry.amount
                when (formatType) {
                    "commas" -> amount.formatWithCommas()
                    "formatted" -> amount.formatWithExtension()
                    "integer" -> amount.toInt().toString()
                    else -> amount.toNiceString()
                }
            }

            else -> null
        }
    }
}