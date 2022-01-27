package io.github.superjoy0502.lifesteals.plugin

import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.heartbeat.coroutines.Suspension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class PhaseManager(private val plugin: LifeStealPlugin) {

    var phase = 0
    var phaseLength = 300
    var currentPhaseLength = phaseLength
    var phaseColor = BarColor.RED
    var penaltyString = ""
    var penaltyId = -1
    var hardCorePenaltyId = -1
    lateinit var phaseScope: CoroutineScope
    lateinit var fixTimeScope: CoroutineScope
    lateinit var fixDifficultyScope: CoroutineScope
    lateinit var fixWeatherScope: CoroutineScope

    fun phaseCoroutine() {

        phaseScope = HeartbeatScope()

        phase++
        phaseLength = if (phase < 7) 300 else 180
        val barColorList = listOf(BarColor.RED, BarColor.GREEN, BarColor.BLUE, BarColor.PURPLE, BarColor.PINK, BarColor.WHITE, BarColor.YELLOW)
        phaseColor = barColorList[(0..7).random()]
        plugin.bossBar.color = phaseColor
        phaseScope.launch {

            applyPenaltyToPlayers()
            val suspension = Suspension()
            currentPhaseLength = phaseLength
            repeat(phaseLength) {

                currentPhaseLength--
                updateBossBar()
                suspension.delay(1000L)

            }
            plugin.lifeStealValue = 2
            if (plugin.started) phaseCoroutine()

        }

    }

    fun updateBossBar() {

        val remainingTime = convertSecondsToMinutesAndSeconds(currentPhaseLength)
        plugin.bossBar.setTitle("PHASE $phase: ${remainingTime[0]}m ${remainingTime[1]}s")

    }

    fun applyPenaltyToPlayers() {

        if (phase == 1 || phase == 2) return // phase 1, 2
        else if (phase in 3..6) { // phase 3 ~ 6

            penaltyId = (0..4).random()

            when (penaltyId) {

                0 -> { // 빼앗기는 하트 수 1개 증가

                    penaltyString = "${ChatColor.RED}하트${ChatColor.RESET}가 한개 더 빼앗깁니다"

                    plugin.lifeStealValue += 2

                }

                1 -> { // 5분동안 모든 플레이어 발광 효과 부여

                    for (player in plugin.survivorList) {

                        player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, phaseLength, 1))

                    }

                }

                2 -> { // 모든 플레이어의 최대 체력이 영구적으로 하트 1개만큼 감소

                    penaltyString = "${ChatColor.RED}하트${ChatColor.RESET} 1개가 영구적으로 감소합니다"

                    for (player in plugin.survivorList) {

                        player.removeHeart(1, plugin)

                    }

                }

                3 -> { // 5분동안 밤 12시, 난이도 보통 유지

                    fixTime()
                    fixDifficulty(Difficulty.NORMAL)

                }

                4 -> { // 5분동안 날씨 번개 유지

                    fixThunderStorm()

                }

            }

        }
        else if (phase in 7..16) { // phase 7 ~ 16

            val decider = (1..100).random()

            when {

                decider <= 20 -> {

                    if (plugin.lifeStealValue <= 10) { // 빼앗기는 하트 수 1개 영구적 증가 (최대 5개까지) - 20%

                        plugin.lifeStealValue += 2

                    } else { // 빼앗기는 하트 수 5개 초과 시

                        if ((0..1).random() == 0) { // 5분동안 밤 12시, 난이도 어려움 고정 - +10%

                            fixTime()
                            fixDifficulty(Difficulty.HARD)

                        } else { // 5분동안 날씨 번개 유지, 난이도 어려움 고정 - +10%

                            fixThunderStorm()
                            fixDifficulty(Difficulty.HARD)

                        }

                    }

                }
                decider in 21..40 -> { // 가장 가까운 플레이어에게 위치를 가르키는 나침반 지급 - 20%

                    // TODO

                }
                decider in 41..50 -> { // 모든 플레이어의 최대 체력이 영구적으로 하트 1개만큼 감소 - 10%

                    for (player in plugin.survivorList) {

                        player.removeHeart(1, plugin)

                    }

                }
                decider in 51..70 -> { // 모든 플레이어에게 발광 효과 부여 - 20%

                    applyPotionEffectToPlayers(PotionEffect(PotionEffectType.GLOWING, phaseLength, 1))

                }
                decider in 71..80 -> { // 5분동안 밤 12시, 난이도 어려움 고정 - 10% (조건부 +10%)

                    fixTime()
                    fixDifficulty(Difficulty.HARD)

                }
                decider in 81..90 -> { // 5분동안 날씨 번개 유지, 난이도 어려움 - 10% (조건부 +10%)

                    fixThunderStorm()
                    fixDifficulty(Difficulty.HARD)

                }
                else -> { // 디버프 - 10%

                    val debuffDecider = (1..10000).random()

                    when {

                        debuffDecider <= 1500 -> // Mining Fatigue Level 1 - 15%
                            applyPotionEffectToPlayers(PotionEffect(PotionEffectType.SLOW_DIGGING, phaseLength, 1))
                        debuffDecider in 1501..2500 -> // Mining Fatigue Level 2 - 10%
                            applyPotionEffectToPlayers(PotionEffect(PotionEffectType.SLOW_DIGGING, phaseLength, 2))
                        debuffDecider in 2501..6500 -> // Hunger - 40%
                            applyPotionEffectToPlayers(PotionEffect(PotionEffectType.HUNGER, phaseLength, 1))
                        debuffDecider in 6501..8100 -> // Poison Level 1 - 16%
                            applyPotionEffectToPlayers(PotionEffect(PotionEffectType.POISON, phaseLength, 1))
                        debuffDecider in 8101..8900 -> // Poison Level 2 - 8%
                            applyPotionEffectToPlayers(PotionEffect(PotionEffectType.POISON, phaseLength, 2))
                        debuffDecider in 8901..8975 -> // Wither Level 1 - 0.75%
                            applyPotionEffectToPlayers(PotionEffect(PotionEffectType.WITHER, phaseLength, 1))
                        debuffDecider in 8976..9000 -> // Wither Level 2 - 0.25%
                            applyPotionEffectToPlayers(PotionEffect(PotionEffectType.WITHER, phaseLength, 2))
                        else -> // Bad Omen - 10%
                            applyPotionEffectToPlayers(PotionEffect(PotionEffectType.BAD_OMEN, phaseLength, 1))

                    }

                }

            }

        }

    }

    fun fixThunderStorm() {
        fixWeatherScope = HeartbeatScope()
        fixWeatherScope.launch {

            val suspension = Suspension()
            repeat(phaseLength / 10) {

                for (world in plugin.server.worlds) {

                    world.setStorm(true)
                    world.isThundering = true

                }

                suspension.delay(10000L)

            }
            for (world in plugin.server.worlds) {

                world.setStorm(false)
                world.isThundering = false

            }

        }
    }

    fun fixTime() {

        fixTimeScope = HeartbeatScope()
        fixTimeScope.launch {

            val suspension = Suspension()
            for (world in plugin.server.worlds) {

                world.time = 18000L
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)

            }
            suspension.delay(phaseLength * 1000L)
            for (world in plugin.server.worlds) {

                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)

            }

        }

    }

    fun fixDifficulty(difficulty: Difficulty) {

        fixDifficultyScope = HeartbeatScope()
        fixDifficultyScope.launch {

            val suspension = Suspension()
            for (world in plugin.server.worlds) {

                world.difficulty = difficulty

            }
            suspension.delay(300000L)
            for (world in plugin.server.worlds) {

                world.difficulty = Difficulty.EASY

            }

        }

    }

    fun applyPotionEffectToPlayers(effect: PotionEffect) {

        for (player in plugin.survivorList) {

            player.addPotionEffect(effect)

        }

    }

    fun convertSecondsToMinutesAndSeconds(seconds: Int): List<Int> {

        val minutes = seconds / 60
        val seconds = seconds % 60

        return listOf(minutes, seconds)

    }

}