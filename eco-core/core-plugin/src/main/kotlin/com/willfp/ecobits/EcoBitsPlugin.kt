package com.willfp.ecobits

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.bstats.EcoMetricsChart
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.leaderboard.Leaderboards
import com.willfp.eco.util.ClassUtils
import com.willfp.ecobits.commands.CommandEcoBits
import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.EcoBitsTopPlaceholder
import com.willfp.ecobits.integrations.IntegrationVault

internal lateinit var plugin: EcoBitsPlugin
    private set

class EcoBitsPlugin : EcoPlugin() {
    val serverID = configYml.getString("server-id")

    val shortcuts = configYml.getStrings("shortcuts")

    init {
        plugin = this
    }

    override fun handleLoad() {
        // Initial preload for Vault
        if (ClassUtils.exists("net.milkbowl.vault.economy.Economy")) {
            IntegrationVault.isVaultPresent = true
            Currencies.update()
        }
    }

    override fun handleEnable() {
        if (this.configYml.getBool("leaderboard.enabled"))
            EcoBitsTopPlaceholder.register()
    }

    override fun handleReload() {
        // Registration is keyed plugin:id, so a currency removed from the config would otherwise
        // leave a dead leaderboard behind, still being refreshed, on every reload.
        Leaderboards.unregisterAll(this)

        Currencies.update()

        // Registered here, after the currencies have been rebuilt, rather than from the Currency
        // constructor: a leaderboard registered there would be deleted by the unregisterAll above.
        for (currency in Currencies.values()) {
            currency.registerLeaderboard()
        }
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandEcoBits
        )
    }

    override fun getCustomCharts() = listOf(
        EcoMetricsChart.SingleLine("total_currencies") { Currencies.values().size },
        EcoMetricsChart.SingleLine("vault_currencies") { Currencies.values().count { it.isRegisteredWithVault } },
        EcoMetricsChart.SingleLine("payable_currencies") { Currencies.values().count { it.isPayable } },
        EcoMetricsChart.SimplePie("leaderboard_enabled") {
            if (configYml.getBool("leaderboard.enabled")) "enabled" else "disabled"
        },
        EcoMetricsChart.SimplePie("vault_present") {
            if (IntegrationVault.isVaultPresent) "present" else "absent"
        }
    )
}

