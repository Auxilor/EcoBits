package com.willfp.ecobits

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.integrations.IntegrationLoader
import com.willfp.eco.util.ClassUtils
import com.willfp.ecobits.commands.CommandEcoBits
import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.integrations.IntegrationVault
import org.bukkit.event.Listener

class EcoBitsPlugin : EcoPlugin() {
    val serverID = configYml.getString("server-id")

    //
    val allowOfflinePlayersString = configYml.getString("allow-offline-players-in-commands")
    val allowOfflinePlayers = allowOfflinePlayersString.toBoolean()
    //

    init {
        instance = this
    }

    override fun handleLoad() {
        // Initial preload for Vault
        if (ClassUtils.exists("net.milkbowl.vault.economy.Economy")) {
            IntegrationVault.isVaultPresent = true
            Currencies.update(this)
        }
    }

    override fun handleReload() {
        Currencies.update(this)
    }

    override fun loadListeners(): List<Listener> {
        return emptyList()
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandEcoBits(this)
        )
    }

    override fun getMinimumEcoVersion(): String {
        return "6.60.0"
    }

    companion object {
        @JvmStatic
        lateinit var instance: EcoBitsPlugin
    }
}

