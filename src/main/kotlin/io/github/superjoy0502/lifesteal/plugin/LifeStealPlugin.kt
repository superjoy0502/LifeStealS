package io.github.superjoy0502.lifesteal.plugin

import io.github.monun.kommand.kommand
import io.github.superjoy0502.lifesteal.listener.PlayerListener
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Dongwoo Kim
 */

class LifeStealPlugin : JavaPlugin() {

    val pluginVersion = "0.1.0"
    val lifesteal = "${ChatColor.RED}LifeSteal${ChatColor.RESET}"

    val playerListener = PlayerListener(this)
    var started = false

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
                            ==========도움말==========
                            /lifesteal - 이 도움말을 출력합니다
                            /lifesteal start - $lifesteal 게임을 시작합니다
                        """.trimIndent()
                    )

                }

                then("start") {

                    executes {

                        if (started) player.sendMessage(
                            "${ChatColor.RED}이미 게임은 시작되었습니다${ChatColor.RESET}" +
                                    "게임은 최후의 1인이 남을때까지 계속됩니다"
                        )
                        started = true

                    }

                }

            }

        }

    }

}