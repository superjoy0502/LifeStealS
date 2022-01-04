package io.github.superjoy0502.lifesteal.plugin

import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.heartbeat.coroutines.Suspension
import io.github.monun.kommand.kommand
import io.github.superjoy0502.lifesteal.listener.PlayerListener
import io.github.superjoy0502.lifesteal.math.PlayerSpawner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Dongwoo Kim
 */

class LifeStealPlugin : JavaPlugin() {

    private val pluginVersion = "0.2.0b"
    private val lifesteal = "${ChatColor.RED}LifeSteal${ChatColor.RESET}"
    private val playerListener = PlayerListener(this)

    var started = false
    var initialized = false
    var participantList: ArrayList<Player> = ArrayList()
    var survivorList: ArrayList<Player> = ArrayList()
    var centreLocation: Location? = null
    var phase = 0
    lateinit var phaseScope: CoroutineScope
    lateinit var worldBorderScope: CoroutineScope
    var phaseLength = 300
    var currentPhaseLength = phaseLength
    var phaseColor = BarColor.RED
    lateinit var bossBar: BossBar
    var penaltyString = ""
    var lifeStealValue = 2
    var penaltyId = -1

    override fun onEnable() {

        logger.info("$lifesteal 플러그인 v$pluginVersion 이 가동했습니다!")
        Bukkit.getPluginManager().registerEvents(playerListener, this)

        bossBar = server.createBossBar(
            null,
            phaseColor,
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

        phaseScope = HeartbeatScope()
        phaseCoroutine()

    }

    fun phaseCoroutine() {

        phase++
        phaseLength = if (phase < 7) 300 else 180
        val barColorList = listOf(BarColor.RED, BarColor.GREEN, BarColor.BLUE, BarColor.PURPLE, BarColor.PINK, BarColor.WHITE, BarColor.YELLOW)
        phaseColor = barColorList[Random().nextInt(0, 8)]
        bossBar.color = phaseColor
        applyPenaltyToPlayers()
        phaseScope.launch {

            val suspension = Suspension()
            currentPhaseLength = phaseLength
            repeat(phaseLength) {

                currentPhaseLength--
                updateBossBar()
                suspension.delay(1000L)

            }
            lifeStealValue = 2
            if (started) phaseCoroutine()

        }

    }

    fun reset() {



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

    fun updateBossBar() {

        val remainingTime = convertSecondsToMinutesAndSeconds(currentPhaseLength)
        bossBar.setTitle("PHASE $phase: ${remainingTime[0]}m ${remainingTime[1]}s")

    }

    fun applyPenaltyToPlayers() {

        penaltyId = Random().nextInt(0, 5)

        when (penaltyId) {

            0 -> { // 하트 1개 영구적 감소

                penaltyString = "${ChatColor.RED}하트${ChatColor.RESET} 1개가 영구적으로 감소합니다"

                for (player in survivorList) {

                    if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue!! <= 2) {

                        player.gameMode = GameMode.SPECTATOR
                        survivorList.remove(player)
                        player.showTitle(Title.title(Component.text("${ChatColor.RED}탈락하셨습니다"), Component.empty()))

                    }
                    else {

                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue!!.minus(2)

                    }

                }

            }

            1 -> {

                penaltyString = "${ChatColor.RED}하트${ChatColor.RESET}가 2개 빼앗겨집니다"

                lifeStealValue = 4

            }

            2 -> {

                penaltyString = "${ChatColor.AQUA}월드보더${ChatColor.RESET}가 줄어듭니다"

                worldBorderScope = HeartbeatScope()
                val suspension = Suspension()
                worldBorderScope.launch {

                    repeat(phaseLength * 1000) {

                        centreLocation!!.world.worldBorder.size.minus(1000 / (phaseLength * 1000))
                        suspension.delay(1L)

                    }

                }

            }

            3 -> {

                penaltyString = "${ChatColor.RED}저주에 걸립니다"

                for (player in survivorList) {

                    player.addPotionEffect(PotionEffect(PotionEffectType.POISON, 300, 1))

                }

            }

        }

    }

    fun convertSecondsToMinutesAndSeconds(seconds: Int): List<Int> {

        val minutes = seconds / 60
        val seconds = seconds % 60

        return listOf(minutes, seconds)

    }

}