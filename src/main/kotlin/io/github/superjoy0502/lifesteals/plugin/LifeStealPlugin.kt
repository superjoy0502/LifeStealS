package io.github.superjoy0502.lifesteals.plugin

import io.github.monun.kommand.kommand
import io.github.superjoy0502.lifesteals.listener.PlayerDisconnectListener
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

    private val pluginVersion = "1.2.1b"
    private val lifesteal = "${ChatColor.RED}LifeSteal${ChatColor.GOLD}S${ChatColor.RESET}"
    private var playerListener = PlayerListener(this)
    private var disconnectListener = PlayerDisconnectListener(this)
    var phaseManager = PhaseManager(this)

    var started = false
    var initialized = false
    var participantList: ArrayList<Player> = ArrayList()
    var survivorList: ArrayList<Player> = ArrayList()
    var centreLocation: Location? = null
    var lifeStealValue = 2
    lateinit var bossBar: BossBar

    override fun onEnable() {

        logger.info("$lifesteal 플러그인 v$pluginVersion 이 가동했습니다!")

        reset()

        kommand {

            register("lifesteal") {

                permission("lifesteal.commands")

                executes {

                    player.sendMessage(
                        """
                            $lifesteal 플러그인 v$pluginVersion
                            ====================도움말====================
                            /lifesteal - 이 도움말을 출력합니다
                            /lifesteal init - $lifesteal 게임의 중심 좌표(기본값 0, 0)를 정합니다
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

                then("compass") {

                    permission("lifesteal.commands.compass")

                    executes {

                        if (phaseManager.playersWithNoCompassList.contains(player)) {

                            if (player.inventory.addItem(ItemStack(Material.COMPASS)).isNotEmpty()) {

                                player.sendMessage("인벤토리가 꽉 차있어 나침반이 지급되지 않았습니다. /lifesteal compass 명령어를 사용해서 받아주세요.")

                            }
                            else {

                                phaseManager.playersWithNoCompassList.remove(player)

                            }

                        }
                        else player.sendMessage("나침반을 지급 받을 수 없습니다!")

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

        reset()

        participantList = ArrayList(server.onlinePlayers)
        survivorList = participantList
        val playerSpawner = PlayerSpawner(participantList.size, centreLocation!!)
        for (i in 0 until participantList.size) {

            val player = participantList[i]
            player.teleport(playerSpawner.getPlayerSpawnLocation(i))
            player.gameMode = GameMode.SURVIVAL
            player.addPotionEffect(
                PotionEffect(PotionEffectType.SLOW_FALLING, 300*20, 1)
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

    private fun reset() {

        for (player in server.onlinePlayers) {

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
            if (centreLocation != null) player.teleport(centreLocation!!)
            player.gameMode = GameMode.SURVIVAL

        }
        for (player in participantList) player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
        survivorList = arrayListOf()
        participantList = arrayListOf()
        playerListener = PlayerListener(this)
        disconnectListener = PlayerDisconnectListener(this)
        phaseManager = PhaseManager(this)
        started = false
        initialized = false
        participantList = ArrayList()
        survivorList = ArrayList()
        lifeStealValue = 2
        Bukkit.getPluginManager().registerEvents(playerListener, this)
        Bukkit.getPluginManager().registerEvents(disconnectListener, this)
        bossBar = server.createBossBar(
            null,
            phaseManager.phaseColor,
            BarStyle.SOLID
        )
        bossBar.isVisible = false
        centreLocation?.world?.worldBorder?.size = 10000.0
        centreLocation?.world?.worldBorder?.center = centreLocation!!

    }

    fun endGame(winner: Player) {

        for (player in server.onlinePlayers) {

            player.showTitle(Title.title(
                Component.text("${ChatColor.GREEN}${winner.name}님 우승!"),
                Component.text(
                    "${ChatColor.RED}Max HP: ${winner.getAttribute(Attribute.GENERIC_MAX_HEALTH)}")))

        }

        reset()

    }

}
