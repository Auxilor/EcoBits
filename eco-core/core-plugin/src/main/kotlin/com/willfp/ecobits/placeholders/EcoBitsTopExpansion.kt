package com.willfp.ecobits.placeholders

import com.willfp.eco.util.savedDisplayName
import com.willfp.ecobits.EcoBitsPlugin
import com.willfp.ecobits.currencies.Currencies
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class EcoBitsTopExpansion(val plugin: EcoBitsPlugin): PlaceholderExpansion() {
    /**
     * The placeholder identifier of this expansion. May not contain %,
     * {} or _
     *
     * @return placeholder identifier that is associated with this expansion
     */
    override fun getIdentifier(): String {
        return "ecobitstop"
    }

    /**
     * The author of this expansion
     *
     * @return name of the author for this expansion
     */
    override fun getAuthor(): String {
        return "_OfTeN_"
    }

    /**
     * The version of this expansion
     *
     * @return current version of this expansion
     */
    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val args = params.split("_")
        val currency = Currencies.getByID(args.firstOrNull()?.lowercase() ?: return "Invalid currency")
        val place = args.getOrNull(1)?.toIntOrNull() ?: return "Invalid place (must be an integer)"

        return when (args.lastOrNull() ?: return "Invalid type: ${args.lastOrNull()}. Must be name/amount") {
            "name" -> currency?.getTop(place)?.player?.savedDisplayName ?: plugin.langYml.getFormattedString(
                "top.name-empty"
            )
            "amount" -> currency?.getTop(place)?.amount?.toString() ?: plugin.langYml.getFormattedString(
                "top.amount-empty"
            )
            else -> null
        }
    }
}