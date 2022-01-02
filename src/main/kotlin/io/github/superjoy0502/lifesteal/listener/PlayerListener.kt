package io.github.superjoy0502.lifesteal.listener

import io.github.superjoy0502.lifesteal.plugin.LifeStealPlugin
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerListener(val plugin: LifeStealPlugin) : Listener {

    @EventHandler
    fun playerKilledEvent(event: PlayerDeathEvent) {

        if (!plugin.started) return

        val victim = event.player
        val killer = victim.killer ?: return
        if (victim == killer) return

        val victimMaxHealth = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)

        if (victimMaxHealth?.baseValue == 2.0) {

            victim.gameMode = GameMode.SPECTATOR
            return

        }

        val killerMaxHealth = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH)

        victimMaxHealth?.baseValue = victimMaxHealth?.baseValue?.minus(2)!!
        killerMaxHealth?.baseValue = killerMaxHealth?.baseValue?.plus(2)!!


    }

}