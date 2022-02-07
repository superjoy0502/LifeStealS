package io.github.superjoy0502.lifesteals.plugin

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

    val pluginVersion = "1.2.21b"
    private val commandDispatcher = CommandDispatcher(this)
    val lifesteal = "${ChatColor.RED}LifeSteal${ChatColor.GOLD}S${ChatColor.RESET}"
    private val playerListener = PlayerListener(this)
    private val disconnectListener = PlayerDisconnectListener(this)
    var phaseManager = PhaseManager(this)

    var started = false
    var initialized = false
    var participantList: ArrayList<Player> = ArrayList()
    var survivorList: ArrayList<Player> = ArrayList()
    var centreLocation: Location? = null
    var lifeStealValue = 2
    var bossBar: BossBar? = null

    override fun onEnable() {

        logger.info("$lifesteal 플러그인 v$pluginVersion 이 가동했습니다!")

        reset()

        Bukkit.getPluginManager().registerEvents(playerListener, this)
        Bukkit.getPluginManager().registerEvents(disconnectListener, this)
        getCommand("lifesteal")?.setExecutor(commandDispatcher)
        getCommand("compass")?.setExecutor(commandDispatcher)

    }

    fun start() {


        for (world in server.worlds) {

            world.difficulty = Difficulty.EASY

        }

        reset()

        participantList = ArrayList(server.onlinePlayers)
        survivorList = participantList
        bossBar = server.createBossBar(
            null,
            phaseManager.phaseColor,
            BarStyle.SOLID
        )
        bossBar?.isVisible = true
        println(bossBar!!.isVisible)
        val playerSpawner = PlayerSpawner(participantList.size, centreLocation!!)
        for (i in 0 until participantList.size) {

            val player = participantList[i]
            bossBar?.addPlayer(player)
            player.teleport(playerSpawner.getPlayerSpawnLocation(i))
            player.gameMode = GameMode.SURVIVAL
            player.addPotionEffect(
                PotionEffect(PotionEffectType.SLOW_FALLING, 60 * 20, 1)
            )
            player.inventory.clear()
            player.inventory.addItem(ItemStack(Material.STONE_SWORD))
            player.inventory.addItem(ItemStack(Material.STONE_AXE))
            player.inventory.addItem(ItemStack(Material.STONE_PICKAXE))
            player.inventory.addItem(ItemStack(Material.BREAD, 10))
            player.world.time = 0
            if (centreLocation != null) player.world.spawnLocation = centreLocation!!

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
        survivorList = arrayListOf()
        participantList = arrayListOf()
        disconnectListener.playerDisconnectTimeMap = mutableMapOf<Player, Int>()
        phaseManager = PhaseManager(this)
        started = false
        initialized = false
        participantList = ArrayList()
        survivorList = ArrayList()
        lifeStealValue = 2
        if (bossBar != null) bossBar!!.isVisible = false
        centreLocation?.world?.worldBorder?.size = 10000.0
        centreLocation?.world?.worldBorder?.center = centreLocation!!

    }

    fun endGame(winner: Player) {

        for (player in server.onlinePlayers) {

            player.showTitle(
                Title.title(
                    Component.text("${ChatColor.GREEN}${winner.name}님 우승!"),
                    Component.text(
                        "${ChatColor.RED}Max HP: ${winner.getAttribute(Attribute.GENERIC_MAX_HEALTH)}"
                    )
                )
            )

        }

        reset()

    }

}
