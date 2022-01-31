package io.github.superjoy0502.lifesteals.listener

import io.github.superjoy0502.lifesteals.plugin.LifeStealPlugin
import io.github.superjoy0502.lifesteals.plugin.removeHeart
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import kotlin.math.ceil
import kotlin.math.floor

class PlayerListener(private val plugin: LifeStealPlugin) : Listener {

    @EventHandler
    fun playerKilledEvent(event: PlayerDeathEvent) {

        if (!plugin.started) return // 게임 시작했는지 확인

        val victim = event.player
        val killer = victim.killer
        val deathReason = victim.lastDamageCause
        if (killer == null) {

            if (deathReason is EntityDamageByEntityEvent) {

                if (deathReason.damager is Mob) { // 몬스터에 의해 사망한 경우

                    victim.removeHeart(plugin.lifeStealValue.toDouble(), plugin)
                    return

                }

            }

        }
        else { // 플레이어에 의해 사망한 경우

            if (victim != killer) {

                victim.removeHeart(plugin.lifeStealValue.toDouble(), plugin)
                killer.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue += plugin.lifeStealValue

                return

            }

        }
        // 기타
        val value = ceil(victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue / 2)
        victim.removeHeart(value, plugin)

    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {

        if (!plugin.phaseManager.isTrackingClosestPlayer) return

        val target = event.player

        for (player in getPlayersClosestToTarget(target)) player.compassTarget = target.location

    }

    fun getPlayersClosestToTarget(target: Player): List<Player> {

        val list = arrayListOf<Player>()

        if (!plugin.phaseManager.playerTrackingMap.containsValue(target)) return emptyList()

        for (pair in plugin.phaseManager.playerTrackingMap) if (pair.value == target) list.add(pair.key)

        return list

    }

}
