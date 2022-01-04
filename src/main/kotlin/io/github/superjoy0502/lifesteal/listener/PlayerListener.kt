package io.github.superjoy0502.lifesteal.listener

import io.github.superjoy0502.lifesteal.plugin.LifeStealPlugin
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Mob
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import kotlin.math.floor

class PlayerListener(val plugin: LifeStealPlugin) : Listener {

    @EventHandler
    fun playerKilledEvent(event: PlayerDeathEvent) {

        if (!plugin.started) return // 게임 시작했는지 확인

        val victim = event.player
        val killer = victim.killer
        val deathReason = victim.lastDamageCause
        if (killer == null) {

            if (deathReason is EntityDamageByEntityEvent) {

                if (deathReason.damager is Mob) { // 몬스터에 의해 사망한 경우

                    victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue?.minus(plugin.lifeStealValue)
                    return

                }

            }

        }
        else { // 플레이어에 의해 사망한 경우

            if (victim != killer) {

                val victimMaxHealth = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue ?: return
                val killerMaxHealth = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue ?: return
                if (victimMaxHealth <= plugin.lifeStealValue) { // 더 이상 깎을 체력이 없을 때

                    victim.gameMode = GameMode.SPECTATOR
                    plugin.survivorList.remove(victim)

                    if (plugin.survivorList.size == 1) {

                        plugin.endGame(killer)

                    }

                    return

                }

                victimMaxHealth.minus(plugin.lifeStealValue)
                killerMaxHealth.plus(plugin.lifeStealValue)

                return

            }

        }
        // 기타
        val value = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue
        victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue =
            floor(value / 2)

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