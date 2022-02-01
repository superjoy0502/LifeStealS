package io.github.superjoy0502.lifesteals.plugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

fun Player.removeHeart(health: Double, plugin: LifeStealPlugin) {

    if (this.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue!! <= health) {

        this.gameMode = GameMode.SPECTATOR
        plugin.survivorList.remove(player)
        this.showTitle(Title.title(Component.text("${ChatColor.RED}탈락하셨습니다"), Component.empty()))

        if (plugin.survivorList.size == 1) {

            plugin.endGame(plugin.survivorList[0])

        }

    }
    else {

        this.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue =- health

    }

}