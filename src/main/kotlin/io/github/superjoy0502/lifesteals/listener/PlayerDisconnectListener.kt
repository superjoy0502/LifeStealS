package io.github.superjoy0502.lifesteals.listener

import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.heartbeat.coroutines.Suspension
import io.github.superjoy0502.lifesteals.plugin.LifeStealPlugin
import kotlinx.coroutines.launch
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerDisconnectListener(private val plugin: LifeStealPlugin) : Listener {

    var playerDisconnectTimeMap = mutableMapOf<Player, Int>()

    @EventHandler
    fun onPlayerDisconnect(event: PlayerQuitEvent) {

        if (!plugin.started) return

        val disconnectScope = HeartbeatScope()
        val player = event.player

        disconnectScope.launch {

            if (plugin.phaseManager.phase < 17) {

                playerDisconnectTimeMap[player] = 0
                var counter = 0
                val suspension = Suspension()
                repeat(180) {

                    suspension.delay(1000L)
                    if (plugin.server.onlinePlayers.contains(player)) {

                        playerDisconnectTimeMap.remove(player)
                        return@repeat

                    }
                    counter++
                    playerDisconnectTimeMap[player] = counter

                }

            }

            plugin.survivorList.remove(player)
            plugin.participantList.remove(player)

            if (plugin.survivorList.size == 1) {

                plugin.endGame(plugin.survivorList[0])

            }

        }

    }

    @EventHandler
    fun onPlayerConnect(event: PlayerJoinEvent) {

        val player = event.player

        if (!plugin.participantList.contains(player)) {

            player.gameMode = GameMode.SPECTATOR

        }

    }

}