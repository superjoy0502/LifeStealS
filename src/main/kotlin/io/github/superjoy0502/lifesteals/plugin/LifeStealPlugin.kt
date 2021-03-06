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
import org.bukkit.inventory.Recipe
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


/**
 * @author Dongwoo Kim
 */

class LifeStealPlugin : JavaPlugin() {

    val pluginVersion = "1.5.1b"
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

    var world: World? = null
    var nether: World? = null
    var end: World? = null

    override fun onEnable() {

        logger.info("$lifesteal 플러그인 v$pluginVersion 이 가동했습니다!")
        logger.info("Made by ${ChatColor.GOLD}Dongwoo Kim${ChatColor.RESET} (https://github.com/superjoy0502/LifeStealS)")

        reset()

        Bukkit.getPluginManager().registerEvents(playerListener, this)
        Bukkit.getPluginManager().registerEvents(disconnectListener, this)
        getCommand("lifesteal")?.setExecutor(commandDispatcher)
        getCommand("compass")?.setExecutor(commandDispatcher)

        val it = server.recipeIterator()
        var recipe: Recipe?
        while (it.hasNext()) {

            recipe = it.next()
            if (recipe != null && recipe.result.type == Material.COMPASS) it.remove()

        }

    }

    fun start() {

        world = centreLocation?.world!!
        nether = server.getWorld(world!!.name + "_nether")
        end = server.getWorld(world!!.name + "_the_end")

        world!!.difficulty = Difficulty.EASY
        nether?.difficulty = Difficulty.EASY
        end?.difficulty = Difficulty.EASY

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
        val playerSpawner = PlayerSpawner(survivorList.size, centreLocation!!)
        for (i in 0 until survivorList.size) {

            val player = survivorList[i]
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
        world!!.setGameRule(GameRule.KEEP_INVENTORY, true)
        nether?.setGameRule(GameRule.KEEP_INVENTORY, true)
        end?.setGameRule(GameRule.KEEP_INVENTORY, true)
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
        phaseManager.increaseLifeStealValueCount = 0
        started = false
        initialized = false
        participantList = ArrayList()
        survivorList = ArrayList()
        lifeStealValue = 2
        if (bossBar != null) bossBar!!.isVisible = false
        val world = centreLocation?.world
        val nether = server.getWorld(world?.name + "_nether")
        world?.worldBorder?.size = 2500.0
        world?.worldBorder?.center = centreLocation!!
        nether?.worldBorder?.size = 2500.0
        nether?.worldBorder?.center = centreLocation!!

    }

    fun endGame(winner: Player) {

        for (player in server.onlinePlayers) {

            player.showTitle(
                Title.title(
                    Component.text("${ChatColor.GREEN}${winner.name}님 우승!"),
                    Component.text(
                        "${ChatColor.RED}Max HP: ${winner.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue.toString()}"
                    )
                )
            )

        }

        reset()

    }

}
