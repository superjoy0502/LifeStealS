package io.github.superjoy0502.lifesteals.listener

import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.heartbeat.coroutines.Suspension
import io.github.superjoy0502.lifesteals.math.PlayerSpawner
import io.github.superjoy0502.lifesteals.plugin.LifeStealPlugin
import io.github.superjoy0502.lifesteals.plugin.removeHeart
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.block.data.type.Bed
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


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
                else if (deathReason.damager is Arrow) { // 화살에 의해 사망한 경우

                    val arrow = deathReason.damager as Arrow
                    if (arrow.shooter is Skeleton) {

                        victim.removeHeart(plugin.lifeStealValue.toDouble(), plugin)
                        return

                    }

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

        val target = event.player

        if (plugin.phaseManager.isTrackingClosestPlayer) {

            for (player in getPlayersClosestToTarget(target)) player.compassTarget = target.location

        }

        if (plugin.phaseManager.phase == 17) {

            if (target.location.y <= 0) {

                target.addPotionEffect(PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 1))

            }
            else {

                target.removePotionEffect(PotionEffectType.WITHER)

            }

        }

    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {

        val player = event.player
        if (!plugin.survivorList.contains(player)) return

        val playerSpawner = PlayerSpawner(plugin.survivorList.size, plugin.centreLocation!!)
        playerSpawner.radius = (plugin.centreLocation!!.world.worldBorder.size * 0.4).toInt()

        event.respawnLocation = playerSpawner.getPlayerSpawnLocation((0 until plugin.survivorList.size).random())

        val givePlayerEffectScope = HeartbeatScope()

        givePlayerEffectScope.launch {

            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_FALLING, 60 * 20, 1))
            if (plugin.phaseManager.phase == 17) {

                val suspension = Suspension()

                suspension.delay(50L)

                player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1))
                player.addPotionEffect(PotionEffect(PotionEffectType.HUNGER, Integer.MAX_VALUE, 1))
                player.addPotionEffect(PotionEffect(PotionEffectType.BAD_OMEN, Integer.MAX_VALUE, 1))

            }

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

    @EventHandler
    fun onSleep(event: PlayerBedEnterEvent) {

        event.isCancelled = true
        event.player.sendMessage("${ChatColor.RED}어디서 주무시려고")
        
    }

    @EventHandler
    fun onInteractBed(event: PlayerInteractEvent) {

        if (event.clickedBlock is Bed) {

            event.isCancelled = true
            event.player.sendMessage("${ChatColor.RED}일 하세요 일")

        }

    }

    @EventHandler
    fun onConsumeMilk(event: PlayerItemConsumeEvent) {

        if (plugin.phaseManager.phase != 17) return
        if (event.item != ItemStack(Material.MILK_BUCKET)) return

        event.isCancelled = true

    }

    fun getPlayersClosestToTarget(target: Player): List<Player> {

        val list = arrayListOf<Player>()

        if (!plugin.phaseManager.playerTrackingMap.containsValue(target)) return emptyList()

        for (pair in plugin.phaseManager.playerTrackingMap) if (pair.value == target) list.add(pair.key)

        return list

    }

}
