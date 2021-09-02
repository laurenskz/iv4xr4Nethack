package org.projectxy.iv4xrLib.rl

import eu.iv4xr.framework.spatial.Vec3
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class NetHackWorldModelTest {

    @Test
    fun test() {

        val vec = Vec3(1.toFloat())
        val position = Position(vec, vec, vec)
        val model = NetHackWorldModel(
                RLWorldModel("playerStatus", position, mapOf(
                        "playerStatus" to RLWorldEntity(
                                "Bab", "agent", position, mapOf(),
                                mapOf(
                                        "equippedWeaponName" to "Sword",
                                        "equippedWeaponDmg" to 28,
                                )
                        )
                ))
        )
        println(model.model)
        println(model.playerStatus.equippedWeaponName)
        model.playerStatus.equippedWeaponDmg = 7
        println(model.getValue())
    }
}