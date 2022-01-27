package io.github.superjoy0502.lifesteals.plugin

import io.github.monun.kommand.kommand
import io.github.superjoy0502.lifesteals.listener.PlayerListener
import io.github.superjoy0502.lifesteals.math.PlayerSpawner
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * @author Dongwoo Kim
 */

class LifeStealPlugin : JavaPlugin() {

    private val pluginVersion = "0.2.0b"
    private val lifesteal = "${ChatColor.RED}LifeSteal${ChatColor.RESET}"
    private val playerListener = PlayerListener(this)
    private val phaseManager = PhaseManager(this)

    var started = false
    var initialized = false
    var participantList: ArrayList<Player> = ArrayList()
    var survivorList: ArrayList<Player> = ArrayList()
    var centreLocation: Location? = null
    var lifeStealValue = 2
//    lateinit var worldBorderScope: CoroutineScope
    lateinit var bossBar: BossBar

    override fun onEnable() {

        logger.info("$lifesteal 플러그인 v$pluginVersion 이 가동했습니다!")
        Bukkit.getPluginManager().registerEvents(playerListener, this)

        bossBar = server.createBossBar(
            null,
            phaseManager.phaseColor,
            BarStyle.SOLID
        )
        bossBar.isVisible = false

        kommand {

            register("lifesteal") {

                permission("lifesteal.commands")

                executes {

                    player.sendMessage(
                        """
                            $lifesteal 플러그인 v$pluginVersion
                            ====================도움말====================
                            /lifesteal - 이 도움말을 출력합니다
                            /lifesteal init - $lifesteal 게임의 중심 위치을 정합니다
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
                            "$lifesteal 게임의 중심이 (${centreLocation!!.x}, ${centreLocation!!.y}, ${centreLocation!!.z})으로 정해졌습니다"
                        )
                        initialized = true

                    }

                }

                then("start") {

                    executes {

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

                            initialized = true

                        }

                        start()

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

    private fun start() {

        for (world in server.worlds) {

            world.difficulty = Difficulty.EASY

        }

        centreLocation!!.world.worldBorder.size = 10000.0
        centreLocation!!.world.worldBorder.center = centreLocation!!

        participantList = ArrayList(server.onlinePlayers)
        survivorList = participantList
        val playerSpawner = PlayerSpawner(participantList.size, centreLocation!!)
        for (i in 0 until participantList.size) {

            val player = participantList[i]
            player.teleport(playerSpawner.getPlayerSpawnLocation(i))
            player.gameMode = GameMode.SURVIVAL
            player.addPotionEffect(
                PotionEffect(PotionEffectType.SLOW_FALLING, 300, 1)
            )
            player.inventory.clear()
            player.inventory.addItem(ItemStack(Material.STONE_SWORD))
            player.inventory.addItem(ItemStack(Material.STONE_AXE))
            player.inventory.addItem(ItemStack(Material.STONE_PICKAXE))
            player.inventory.addItem(ItemStack(Material.BREAD, 10))

        }
        started = true

        phaseManager.phaseCoroutine()

    }

    fun reset() {

        // TODO

    }

    fun endGame(winner: Player) {

        for (player in server.onlinePlayers) {

            player.showTitle(Title.title(
                Component.text("${ChatColor.GREEN}${survivorList[0].name}님 우승!"),
                Component.text(
                    "${ChatColor.RED}Max HP: ${survivorList[0].getAttribute(Attribute.GENERIC_MAX_HEALTH)}")))

        }

        reset()

    }

}