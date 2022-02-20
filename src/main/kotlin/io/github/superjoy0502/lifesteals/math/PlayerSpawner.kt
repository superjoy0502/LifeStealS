package io.github.superjoy0502.lifesteals.math

import org.bukkit.Location
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class PlayerSpawner(
    private val totalPlayers: Int,
    private val centreLocation: Location,
    private val thetaZero: Int = Random.nextInt(0, 360)
) {

    var radius = 1100
    private val ySpawn = 313.0

    fun getPlayerSpawnLocation(index: Int): Location {

        return (Location(
            centreLocation.world,
            (radius * cos(((thetaZero / 180) * PI) + ((2 * PI * index) / totalPlayers))) + centreLocation.x,
            ySpawn,
            (radius * sin(((thetaZero / 180) * PI) + ((2 * PI * index) / totalPlayers))) + centreLocation.z
        ))

    }

}
