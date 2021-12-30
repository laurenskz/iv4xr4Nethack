package org.projectxy.iv4xrLib.rl

import A.B.Room
import eu.iv4xr.framework.spatial.Vec3
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class NethackLogicKtTest {

    @Test
    fun testRoomIndex() {
        val i = roomIndex(3 to 3, listOf(
                Room(0, 0, 5, 2),
                Room(0, 0, 5, 5)
        ))
        assertEquals(i, 1)
    }

    @Test
    fun testSameRoom() {
        assertTrue(sameRoom(
                Vec3(3f, 3f, 0f),
                Vec3(4f, 2f, 0f),
                listOf(
                        Room(0, 0, 5, 5),
                        Room(12, 12, 5, 5)
                )
        ))
        assertFalse(sameRoom(
                Vec3(8f, 3f, 0f),
                Vec3(4f, 2f, 0f),
                listOf(
                        Room(0, 0, 5, 5),
                        Room(12, 12, 5, 5)
                )
        ))
        assertFalse(sameRoom(
                Vec3(3f, 3f, 0f),
                Vec3(14f, 14f, 0f),
                listOf(
                        Room(0, 0, 5, 5),
                        Room(12, 12, 5, 5)
                )
        ))
    }
}