package io.github.superjoy0502.lifesteals.listener

import io.github.superjoy0502.lifesteals.plugin.LifeStealPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import kotlin.math.floor

class PlayerListener(val plugin: LifeStealPlugin) : Listener {

    @EventHandler
    fun playerKilledEvent(event: PlayerDeathEvent) {

        if (!plugin.started) return // 게임 시작했는지 확인

        val victim = event.player
        val killer = victim.killer
        val deathReason = victim.lastDamageCause
        if (killer == null) {

            if (deathReason is EntityDamageByEntityEvent) {

                if (deathReason.damager is Mob) { // 몬스터에 의해 사망한 경우

                    victim.removeHeart(plugin.lifeStealValue / 2)
                    return

                }

            }

        }
        else { // 플레이어에 의해 사망한 경우

            if (victim != killer) {

                victim.removeHeart(plugin.lifeStealValue / 2)
                killer.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue += plugin.lifeStealValue

                return

            }

        }
        // 기타
        val value = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue
        if (value == 1.0) {

            plugin.survivorList.remove(victim)

        }
        else {

            victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue =
                floor(victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue / 2)

        }

    }

    private fun Player.removeHeart(hearts: Int) {

        if (this.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue!! <= (hearts * 2.0)) {

            this.gameMode = GameMode.SPECTATOR
            plugin.survivorList.remove(player)
            this.showTitle(Title.title(Component.text("${ChatColor.RED}탈락하셨습니다"), Component.empty()))

        }
        else {

            this.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue =- (hearts * 2.0)

        }

    }

}
