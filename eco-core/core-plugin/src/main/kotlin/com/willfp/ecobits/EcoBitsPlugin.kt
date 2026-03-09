package com.willfp.ecobits

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
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
        Currencies.update()
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandEcoBits
        )
    }
}

