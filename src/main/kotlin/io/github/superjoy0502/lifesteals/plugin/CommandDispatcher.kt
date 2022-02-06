package io.github.superjoy0502.lifesteals.plugin

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CommandDispatcher(val plugin: LifeStealPlugin) : CommandExecutor {

    /**
     * Executes the given command, returning its success.
     * <br></br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender Source of the command
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return true if a valid command, otherwise false
     */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        val commandName = command.name

        if (commandName.equals("lifesteal", true)) {

            if (args.isEmpty())  {

                sender.sendMessage(
                    """
                                    ${plugin.lifesteal} 플러그인 v${plugin.pluginVersion}
                                    ====================도움말====================
                                    /lifesteal - 이 도움말을 출력합니다
                                    /lifesteal init - ${plugin.lifesteal} 게임의 중심 좌표(기본값 0, 0)를 정합니다
                                    /lifesteal start - ${plugin.lifesteal} 게임을 시작합니다
                                    /compass - 게임 시작 후 나침반을 받아야하지만 못 받았을 경우 사용할 수 있습니다
                                """.trimIndent()
                )

                return true

            }

            val commandArg: String = args[0]

            when {

                commandArg.equals("init", true) -> {

                    if (plugin.started) {

                        sender.sendMessage(
                            "${ChatColor.RED}이미 게임은 시작되었습니다${ChatColor.RESET}" +
                                    "게임은 최후의 1인이 남을때까지 계속됩니다"
                        )
                        return true

                    }

                    if (sender !is Player) {

                        sender.sendMessage("플레이어만 이 명령어를 사용할 수 있습니다")
                        return false

                    }

                    plugin.centreLocation = sender.location
                    sender.sendMessage(
                        "${plugin.lifesteal} 게임의 중심이 (${plugin.centreLocation!!.x}, ${plugin.centreLocation!!.y}, ${plugin.centreLocation!!.z})으로 정해졌습니다"
                    )
                    plugin.initialized = true

                    return true

                }

                commandArg.equals("start", true) -> {

                    if (plugin.started) {

                        sender.sendMessage(
                            "${ChatColor.RED}이미 게임은 시작되었습니다${ChatColor.RESET}" +
                                    "게임은 최후의 1인이 남을때까지 계속됩니다"
                        )
                        return true

                    }

                    if (sender !is Player) {

                        sender.sendMessage("플레이어만 이 명령어를 사용할 수 있습니다")
                        return false

                    }

                    if (plugin.centreLocation == null) {

                        plugin.centreLocation = Location(
                            sender.world,
                            0.0,
                            sender.world.getHighestBlockAt(0, 0).y.toDouble(),
                            0.0
                        )

                        plugin.initialized = true

                    }

                    plugin.start()

                    return true

                }

            }

        } else if (commandName.equals("compass", true)) {

            if (sender !is Player) {

                sender.sendMessage("플레이어만 이 명령어를 사용할 수 있습니다")
                return false

            }

            if (plugin.phaseManager.playersWithNoCompassList.contains(sender)) {

                if (sender.inventory.addItem(ItemStack(Material.COMPASS)).isNotEmpty()) {

                    sender.sendMessage("인벤토리가 꽉 차있어 나침반이 지급되지 않았습니다. /lifesteal compass 명령어를 사용해서 받아주세요.")

                } else {

                    plugin.phaseManager.playersWithNoCompassList.remove(sender)

                }

            } else sender.sendMessage("나침반을 지급 받을 수 없습니다!")

            return true

        }

        return false

    }

}