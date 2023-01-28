package com.willfp.ecobits

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.integrations.IntegrationLoader
import com.willfp.ecobits.commands.CommandEcoBits
import com.willfp.ecobits.integrations.IntegrationVault
import org.bukkit.event.Listener

class EcoBitsPlugin : EcoPlugin() {
    val serverID = configYml.getString("server-id")

    init {
        instance = this
    }

    override fun loadListeners(): List<Listener> {
        return emptyList()
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandEcoBits(this)
        )
    }

    override fun loadIntegrationLoaders(): List<IntegrationLoader> {
        return listOf(
            IntegrationLoader("Vault") { IntegrationVault.isVaultPresent = true }
        )
    }

    companion object {
        @JvmStatic
        lateinit var instance: EcoBitsPlugin
    }
}

