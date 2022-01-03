package io.github.superjoy0502.lifesteal.listener

import io.github.superjoy0502.lifesteal.plugin.LifeStealPlugin
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(val plugin: LifeStealPlugin) : Listener {

    @EventHandler
    fun playerKilledEvent(event: PlayerDeathEvent) {

        if (!plugin.started) return // 게임 시작했는지 확인

        val victim = event.player
        val killer = victim.killer ?: return // 플레이어가 죽인건지 확인
        if (victim == killer) return // 자살인지 확인

        if (victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue == 2.0) { // 더 이상 깎을 체력이 없을 때

            victim.gameMode = GameMode.SPECTATOR
            plugin.survivorList.remove(victim)

            if (plugin.survivorList.size == 1) {

                plugin.endGame()

            }

            return

        }

        victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue =
            victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue?.minus(2)!!
        killer.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue =
            killer.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue?.plus(2)!!

    }

    /*@EventHandler
    fun playerDisconnectEvent(event: PlayerQuitEvent) {

        if (!plugin.participantList.contains(event.player)) return

        event.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0

    }

    @EventHandler
    fun playerJoinEvent(event: PlayerJoinEvent) {



    }*/

}