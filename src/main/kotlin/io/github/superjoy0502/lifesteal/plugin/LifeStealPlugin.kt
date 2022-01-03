package io.github.superjoy0502.lifesteal.plugin

import io.github.monun.kommand.kommand
import io.github.superjoy0502.lifesteal.listener.PlayerListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Dongwoo Kim
 */

class LifeStealPlugin : JavaPlugin() {

    private val pluginVersion = "0.1.0b"
    private val lifesteal = "${ChatColor.RED}LifeSteal${ChatColor.RESET}"
    private val playerListener = PlayerListener(this)

    var started = false
    var initialized = false
    var participantList: ArrayList<Player> = ArrayList()
    var survivorList: ArrayList<Player> = ArrayList()
    var centreLocation: Location? = null

    override fun onEnable() {

        logger.info("$lifesteal 플러그인 v$pluginVersion 이 가동했습니다!")
        Bukkit.getPluginManager().registerEvents(playerListener, this)

        kommand {

            register("lifesteal") {

                permission("lifesteal.commands")

                executes {

                    player.sendMessage(
                        """
                            $lifesteal 플러그인 v$pluginVersion
                            ====================도움말====================
                            /lifesteal - 이 도움말을 출력합니다
                            /lifesteal init - $lifesteal 게임의 중심 위치을 정합니다 (시작 전 필수)
                            /lifesteal start - $lifesteal 게임을 시작합니다
                        """.trimIndent()
                    )

                }

                then("init") {

                    executes {

                        if (started) {

                            player.sendMessage(
                                "${ChatColor.RED}이미 게임은 시작되었습니다${ChatColor.RESET}" +
                                        "게임은 최후의 1인이 남을때까지 계속됩니다"
                            )
                            return@executes

                        }

                        centreLocation = player.location
                        player.sendMessage(
                            "$lifesteal 게임의 중심이 $centreLocation 으로 정해졌습니다"
                        )
                        initialized = true

                    }

                }

                then("start") {

                    executes {

                        if (!initialized) {

                            player.sendMessage("${ChatColor.RED}/lifesteal init 으로 게임 초기 설정을 먼저 해주세요!")
                            return@executes

                        }

                        if (started) {

                            player.sendMessage(
                                "${ChatColor.RED}이미 게임은 시작되었습니다${ChatColor.RESET}" +
                                        "게임은 최후의 1인이 남을때까지 계속됩니다"
                            )
                            return@executes

                        }

                        if (centreLocation == null) {

                            centreLocation = Location(
                                player.world,
                                0.0,
                                player.world.getHighestBlockAt(0, 0).y.toDouble(),
                                0.0
                            )

                        }

                        started = true
                        participantList = ArrayList(server.onlinePlayers)
                        survivorList = participantList
                        for (player in participantList) {

                            player.gameMode = GameMode.SURVIVAL

                        }

                    }

                }

                then("debug") {

                    executes {

                        if (!initialized) {

                            player.sendMessage("초기 설정 안함")
                            return@executes

                        }

                        player.sendMessage("초기 설정 함")

                    }

                }

            }

        }

    }

    fun reset() {



    }

    fun endGame() {

        for (player in server.onlinePlayers) {

            player.showTitle(Title.title(
                Component.text("${ChatColor.GREEN}${survivorList[0].name}님 우승!"),
                Component.text(
                    "${ChatColor.RED}Max HP: ${survivorList[0].getAttribute(Attribute.GENERIC_MAX_HEALTH)}")))

        }

        reset()

    }

}