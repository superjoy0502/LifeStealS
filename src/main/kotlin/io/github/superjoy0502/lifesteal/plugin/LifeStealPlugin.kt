package io.github.superjoy0502.lifesteal.plugin

import io.github.superjoy0502.lifesteal.listener.PlayerListener
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Dongwoo Kim
 */

class LifeStealPlugin : JavaPlugin() {

    val playerListener = PlayerListener()

    override fun onEnable() {

        logger.info("${ChatColor.RED}LifeSteal${ChatColor.RESET} Plugin v0.0.1 is On!")
        Bukkit.getPluginManager().registerEvents(playerListener, this)

    }

}