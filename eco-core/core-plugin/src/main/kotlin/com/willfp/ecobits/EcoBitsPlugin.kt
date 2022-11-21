package com.willfp.ecobits

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecobits.commands.CommandEcoBits
import org.bukkit.event.Listener

class EcoBitsPlugin : EcoPlugin() {
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

    companion object {
        @JvmStatic
        lateinit var instance: EcoBitsPlugin
    }
}

