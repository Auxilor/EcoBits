package com.willfp.ecobits.currencies

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableList
import com.willfp.eco.core.config.updating.ConfigUpdater
import com.willfp.ecobits.EcoBitsPlugin

object Currencies {
    private val BY_ID: BiMap<String, Currency> = HashBiMap.create()

    /**
     * Get all registered [Currency]s.
     *
     * @return A list of all [Currency]s.
     */
    @JvmStatic
    fun values(): List<Currency> {
        return ImmutableList.copyOf(BY_ID.values)
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
     *
     * @param plugin Instance of EcoBits.
     */
    @JvmStatic
    fun update(plugin: EcoBitsPlugin) {
        for (currency in values()) {
            removeCurrency(currency)
        }

        for (config in plugin.configYml.getSubsections("currencies")) {
            addNewCurrency(Currency(config.getString("id"), plugin, config))
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
        BY_ID.remove(currency.id)
    }
}
