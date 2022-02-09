package io.github.superjoy0502.lifesteals.listener

import io.github.superjoy0502.lifesteals.plugin.LifeStealPlugin
import io.github.superjoy0502.lifesteals.plugin.removeHeart
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

class PlayerListener(private val plugin: LifeStealPlugin) : Listener {

    @EventHandler
    fun playerKilledEvent(event: PlayerDeathEvent) {

        if (!plugin.started) return // 게임 시작했는지 확인

//        println("Event")

        val victim = event.player
        val killer = victim.killer
        val deathReason = victim.lastDamageCause
        if (killer == null) {

            if (deathReason is EntityDamageByEntityEvent) {

                if (deathReason.damager is Mob) { // 몬스터에 의해 사망한 경우

//                    println("MONSTER Removing ${plugin.lifeStealValue.toDouble()} health from ${victim.name}")
                    victim.removeHeart(plugin.lifeStealValue.toDouble(), plugin)
                    return

                }

            }

        } else { // 플레이어에 의해 사망한 경우

            if (victim != killer) {

//                println("PLAYER Removing ${plugin.lifeStealValue.toDouble()} health from ${victim.name}")
                victim.removeHeart(plugin.lifeStealValue.toDouble(), plugin)
                val attribute = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                if (attribute != null) {

                    attribute.baseValue = attribute.baseValue + plugin.lifeStealValue

                }

                return

            }

        }
        // 기타
        val attribute = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)
        if (attribute != null) {

            var value = attribute.baseValue / 2
            if (value.toInt() % 2 == 1) value += 1
//            println("ELSE ${event.deathMessage()}")
            victim.removeHeart(value, plugin)

            return

        }

    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {

        if (plugin.phaseManager.isTrackingClosestPlayer) {

            val target = event.player

            for (player in getPlayersClosestToTarget(target)) player.compassTarget = target.location

        }

    }

    @EventHandler
    fun onCompassMoveInventory(event: InventoryMoveItemEvent) {

        var isDestinationPlayer = false

        if (event.item == ItemStack(Material.COMPASS)) {

            for (player in plugin.survivorList) {

                if (event.destination == player.inventory) isDestinationPlayer = true

            }

            if (isDestinationPlayer && !plugin.phaseManager.isTrackingClosestPlayer) event.isCancelled = true

        }

    }

    @EventHandler
    fun onCompassPickUp(event: EntityPickupItemEvent) {

        if (event.entity !is Player) return

        if (event.item == ItemStack(Material.COMPASS)) {

            if (!plugin.phaseManager.isTrackingClosestPlayer) event.isCancelled = true

        }

    }

    fun getPlayersClosestToTarget(target: Player): List<Player> {

        val list = arrayListOf<Player>()

        if (!plugin.phaseManager.playerTrackingMap.containsValue(target)) return emptyList()

        for (pair in plugin.phaseManager.playerTrackingMap) if (pair.value == target) list.add(pair.key)

        return list

    }

}
