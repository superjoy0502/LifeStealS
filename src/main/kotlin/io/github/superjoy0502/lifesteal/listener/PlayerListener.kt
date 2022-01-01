package io.github.superjoy0502.lifesteal.listener

import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerListener : Listener {

    @EventHandler
    fun playerKilledEvent(event: PlayerDeathEvent) {

        val victim = event.player
        val killer = victim.killer ?: return

        if (victim == killer) return

        victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue =
            victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue?.minus(2)!!
        killer.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue =
            killer.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue?.plus(2)!!

//        Bukkit.getServer().logger.info("${killer.name} killed ${victim.name}.")

    }

}