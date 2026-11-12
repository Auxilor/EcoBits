package com.willfp.ecobits.currencies

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableList
import com.google.common.collect.Maps
import com.willfp.ecobits.commands.payShortcutCommand
import com.willfp.ecobits.plugin
import org.bukkit.Bukkit
import wiki.ecohub.core.Eco
import wiki.ecohub.core.command.EcoCommand
import wiki.ecohub.core.price.Prices

object Currencies {
    private val BY_ID: BiMap<String, Currency> = Maps.synchronizedBiMap(HashBiMap.create())

    private var payShortcut: EcoCommand? = null

    /**
     * Get all registered [Currency]s.
     *
     * @return A list of all [Currency]s.
     */
    @JvmStatic
    fun values(): List<Currency> {
        return synchronized(BY_ID) { ImmutableList.copyOf(BY_ID.values) }
    }

    /**
     * Get [Currency] matching ID.
     *
     * @param name The name to search for.
     * @return The matching [Currency], or null if not found.
     */
    @JvmStatic
    fun getByID(name: String): Currency? {
        return BY_ID[name]
    }

    /**
     * Update all [Currency]s.
     */
    @JvmStatic
    fun update() {
        Eco.get().beginCommandBatch()
        try {
            Bukkit.getServicesManager().unregisterAll(plugin)

            for (currency in values()) {
                removeCurrency(currency)
            }

            for (config in plugin.configYml.getSubsections("currencies")) {
                addNewCurrency(Currency(config.getString("id"), plugin, config))
            }

            payShortcut?.unregister()
            payShortcut = values().firstOrNull { it.isRegisteredWithVault }
                ?.takeIf { plugin.configYml.getBool("pay-command") }
                ?.let { payShortcutCommand(it).apply { register() } }
        } finally {
            Eco.get().endCommandBatch()
        }
    }

    /**
     * Add new [Currency] to Currencys.
     *
     * @param currency The [Currency] to add.
     */
    @JvmStatic
    fun addNewCurrency(currency: Currency) {
        BY_ID.remove(currency.id)
        BY_ID[currency.id] = currency
    }

    /**
     * Remove [Currency] from Currencys.
     *
     * @param currency The [Currency] to remove.
     */
    @JvmStatic
    fun removeCurrency(currency: Currency) {
        currency.shutdown()
        Prices.unregisterPriceFactory(currency.priceFactory)
        BY_ID.remove(currency.id)
    }
}
