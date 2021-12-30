package org.projectxy.iv4xrLib.rl

import A.B.Room
import eu.iv4xr.framework.spatial.Vec3
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.abs

internal class NethackLogicKtTest {

    lateinit var state: NethackModelState

    @Before
    fun setup() {
        state = NethackModelState(
                NethackPlayer(
                        "Sword", 10, 1, 10, 10, true, false
                ),
                listOf(),
                listOf(),
                Vec3(0f),
                0,
                Vec3(0f),
                listOf()
        )
    }

    fun parseRooms(string: String): List<Room> {
        val symbols = allIndices(string).map {
            symbol(string, it.first, it.second)
        }.toSet()
        val rooms = symbols.filter { "123456789".contains(it) }
        return rooms.map {
            parseRoom(string, it)
        }
    }

    fun parseRoom(string: String, symbol: String): Room {
        val indices = allIndices(string).filter { symbol(string, it.first, it.second) == symbol }
        val minx = indices.minOf { it.first }
        val maxx = indices.maxOf { it.first }
        val miny = indices.minOf { it.second }
        val maxy = indices.maxOf { it.second }
        return Room(minx, miny, abs(minx - maxx) + 1, abs(miny - maxy) + 1)
    }

    fun parseConf(string: String): NethackModelConfiguration {
        val (cols, rows) = confSize(string)
        return NethackModelConfiguration(
                rows, cols, parseTiles(string), 3, state, parseRooms(string)
        )
    }

    fun confSize(string: String): Pair<Int, Int> {
        val rows = string.lines().count()
        val first = string.lines().first().trim()
        val cols = first.length
        return cols to rows
    }

    fun allIndices(string: String): List<Pair<Int, Int>> {
        val (cols, rows) = confSize(string)
        return (0 until rows).flatMap { y ->
            (0 until cols).map { x -> x to y }
        }
    }

    fun stateForConf(string: String): NethackModelState {
        val mob = NethackMob(
                mobPos(string), 1, 4, true, false, false
        )
        return state.copy(mobs = listOf(mob), position = playerPos(string))
    }

    fun playerPos(string: String) = symbolPos(string, "p")

    fun mobPos(string: String) = symbolPos(string, "m")

    private fun symbolPos(string: String, symbol: String): Vec3 {
        return allIndices(string).first { (x, y) ->
            symbol(string, x, y) == symbol
        }.let { (x, y) -> Vec3(x.toFloat(), y.toFloat(), 0f) }
    }

    fun parseTiles(string: String): Array<Array<NethackModelTile>> {
        val (cols, rows) = confSize(string)
        return Array(cols) { x ->
            Array(rows) { y ->
                NethackModelTile(x, y, when (symbol(string, x, y)) {
                    "x" -> NethackModelTileType.WALL
                    else -> NethackModelTileType.WALKABLE
                })
            }
        }
    }

    fun symbol(string: String, x: Int, y: Int): String {
        return string.lines()[y].trim()[x].toString()
    }

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

    @Test
    fun testMoveTowardsPlayer() {
        val conf =
                """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx111111xxxxxxxx22222222xx
                   xx11p111xxxxxxxx22222222xx
                   xx111111oooooooo22222222xx
                   xx1m1111xxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
        val nhConf = parseConf(conf)
        val nethackModelState = stateForConf(conf)
        val result = moveTowardsPlayer(nethackModelState.mobs.first(), nethackModelState, nhConf)
        val updated = """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx111111xxxxxxxx22222222xx
                   xx11p111xxxxxxxx22222222xx
                   xx1m1111oooooooo22222222xx
                   xx11n111xxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
        val newMobs = result.support().toList().map { it.mobs.first() }
        result.support().forEach {
            assertEquals(1, it.mobs.size)
        }
        assertTrue(newMobs.any { it.position == symbolPos(updated, "m") })
        assertTrue(newMobs.any { it.position == symbolPos(updated, "n") })
    }

    fun seenPlayerAfterSearch(conf: String): Boolean {
        val nhConf = parseConf(conf)
        val nethackModelState = stateForConf(conf)
        val updated = searchPlayer(nethackModelState.mobs.first(), nethackModelState, nhConf)
        return updated.mobs.first().seenPlayer
    }

    @Test
    fun testSearchPlayer() {
        assertFalse(
                seenPlayerAfterSearch(
                        """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx11111pxxxxxxxx22222222xx
                   xx111111xxxxxxxx22222222xx
                   xx11m111oooooooo22222222xx
                   xx11111pxxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
                )
        )
        assertTrue(
                seenPlayerAfterSearch(
                        """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx111111xxxxxxxx22222222xx
                   xx111111xxxxxxxx22222222xx
                   xx111111oooooooo22222222xx
                   xx1m111pxxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
                )
        )
        assertTrue(
                seenPlayerAfterSearch(
                        """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx111111xxxxxxxx22222222xx
                   xx111111xxxxxxxx22222222xx
                   xx111111oooooooo22222222xx
                   xx1p111mxxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
                )
        )
        assertTrue(
                seenPlayerAfterSearch(
                        """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx11111pxxxxxxxx22222222xx
                   xx111111xxxxxxxx22222222xx
                   xx111111oooooooo22222222xx
                   xx11111mxxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
                )
        )
        assertFalse(
                seenPlayerAfterSearch(
                        """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx111111xxxxxxxx22222222xx
                   xx111111xxxxxxxx22222222xx
                   xx1m1111ooooooop22222222xx
                   xx111111xxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
                )
        )
    }

    @Test
    fun testMoveInRoom() {
        val conf =
                """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx111111xxxxxxxx22222222xx
                   xx11p111xxxxxxxx22222222xx
                   xx111111oooooooo22222222xx
                   xx1m1111xxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
        val nhConf = parseConf(conf)
        val nethackModelState = stateForConf(conf)
        val result = moveInRoom(nethackModelState.mobs.first(), nethackModelState, nhConf)
        val updated = """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx111111xxxxxxxx22222222xx
                   xx11p111xxxxxxxx22222222xx
                   xx1m1111oooooooo22222222xx
                   xx11n111xxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
        val newMobs = result.support().toList().map { it.mobs.first() }
        result.support().forEach {
            assertEquals(1, it.mobs.size)
        }
        assertTrue(newMobs.any { it.position == symbolPos(updated, "m") })
        assertTrue(newMobs.any { it.position == symbolPos(updated, "n") })
        assertTrue(
                newMobs.all { it.seenPlayer }
        )
    }

    @Test
    fun testAttackPlayer() {
        val updated = attackPlayer(state, NethackMob(Vec3(0f), 1, 4, true, true, false))
        assertEquals(6, updated.player.health)
    }

    @Test
    fun testUpdateMob() {
        val mob1 = NethackMob(Vec3(0f), 10, 6, true, false, false)
        val mob2 = NethackMob(Vec3(1f), 12, 6, true, false, false)
        val nhState = state.copy(mobs = listOf(mob1, mob2))
        val updated = updateMob(mob2, nhState) {
            it.copy(health = 3)
        }
        assertTrue(updated.mobs.contains(mob1))
        assertTrue(updated.mobs.first { it != mob1 }.health == 3)
    }

    @Test
    fun testMove() {
        val conf =
                """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx111111xxxxxxxx22222222xx
                   xx111111xxxxxxxx22222222xx
                   xx111p11oooooooo22222222xx
                   xx1m1111xxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
        val nhConf = parseConf(conf)
        val nethackModelState = stateForConf(conf)
        val updated = move(listOf(0 to 1, 1 to 0), nethackModelState.mobs.first(), nethackModelState, nhConf)
        val updatedConf =
                """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx111111xxxxxxxx22222222xx
                   xx11p111xxxxxxxx22222222xx
                   xx111111oooooooo22222222xx
                   xx11m111xxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
        println(mobPos(conf))
        assertEquals(mobPos(updatedConf), updated.mobs.first().position)
    }

    fun mobMShouldMove(string: String, dx: Int, dy: Int): Boolean {
        val nhConf = parseConf(string)
        val state = stateForConf(string)
        val mobM = NethackMob(symbolPos(string, "m"), 10, 3, true, false, false)
        val all = state.copy(
                mobs = listOf(
                        mobM,
                        NethackMob(symbolPos(string, "n"), 10, 3, true, false, false)
                )
        )
        return shouldMove(mobM, all, nhConf, dx, dy)
    }

    @Test
    fun testShouldMove() {
        assertFalse(
                mobMShouldMove(
                        """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx111111xxxxxxxx22222222xx
                   xx111111xxxxxxxx22222222xx
                   xx111p11oooooooo22222222xx
                   xx1mn111xxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx""", 1, 0)
        )
        assertTrue(
                mobMShouldMove(
                        """xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                   xx111111xxxxxxxx22222222xx
                   xx111111xxxxxxxx22222222xx
                   xx111p11oooooooo22222222xx
                   xx1m1n11xxxxxxxx22222222xx
                   xxxxxxxxxxxxxxxxxxxxxxxxxx""", 1, 0)
        )
    }

    @Test
    fun testMobAction() {
        testAttackAction()
        testMobActionInSameRoom()
        testWhenSeenPlayer()
        testWhenNotSeenPlayer()
    }

    private fun testWhenNotSeenPlayer() {
        val conf =
                """xxxxxxxxxxxxxxxxxxxxxxxxxx
                                   xxxxxxxxxxxxxxxxxxxxxxxxxx
                                   xxm11111xxxxxxxx22222222xx
                                   xx111111xxxxxxxx22222222xx
                                   xx111111pooooooo22222222xx
                                   xx111111xxxxxxxx22222222xx
                                   xxxxxxxxxxxxxxxxxxxxxxxxxx"""
        val nhConf = parseConf(conf)
        val nethackModelState = stateForConf(conf)
        val mob = nethackModelState.mobs.first().copy(seenPlayer = false)
        val seenPlayer = nethackModelState.copy(mobs = listOf(mob))
        val result = mobAction(seenPlayer, mob, nhConf)
        assertEquals(1, result.support().count())
    }

    private fun testWhenSeenPlayer() {
        val conf =
                """xxxxxxxxxxxxxxxxxxxxxxxxxx
                               xxxxxxxxxxxxxxxxxxxxxxxxxx
                               xxm11111xxxxxxxx22222222xx
                               xx111111xxxxxxxx22222222xx
                               xx111111pooooooo22222222xx
                               xx111111xxxxxxxx22222222xx
                               xxxxxxxxxxxxxxxxxxxxxxxxxx"""
        val nhConf = parseConf(conf)
        val nethackModelState = stateForConf(conf)
        val mob = nethackModelState.mobs.first().copy(seenPlayer = true)
        val seenPlayer = nethackModelState.copy(mobs = listOf(mob))
        val result = mobAction(seenPlayer, mob, nhConf)
        assertTrue(
                result.support().any {
                    it.mobs.first().position == mobPos("""xxxxxxxxxxxxxxxxxxxxxxxxxx
                               xxxxxxxxxxxxxxxxxxxxxxxxxx
                               xx1m1111xxxxxxxx22222222xx
                               xx111111xxxxxxxx22222222xx
                               xx111111pooooooo22222222xx
                               xx111111xxxxxxxx22222222xx
                               xxxxxxxxxxxxxxxxxxxxxxxxxx""")
                }
        )
    }

    private fun testMobActionInSameRoom() {
        val conf =
                """xxxxxxxxxxxxxxxxxxxxxxxxxx
                           xxxxxxxxxxxxxxxxxxxxxxxxxx
                           xxm11111xxxxxxxx22222222xx
                           xx111111xxxxxxxx22222222xx
                           xx111p11oooooooo22222222xx
                           xx111111xxxxxxxx22222222xx
                           xxxxxxxxxxxxxxxxxxxxxxxxxx"""
        val nhConf = parseConf(conf)
        val nethackModelState = stateForConf(conf)
        val next = mobAction(nethackModelState, nethackModelState.mobs.first(), nhConf)
        next.support().forEach {
            assertTrue(it.mobs.first().seenPlayer)
        }
        assertEquals(2, next.support().count())
    }

    private fun testAttackAction() {
        val conf =
                """xxxxxxxxxxxxxxxxxxxxxxxxxx
                       xxxxxxxxxxxxxxxxxxxxxxxxxx
                       xx111111xxxxxxxx22222222xx
                       xx111111xxxxxxxx22222222xx
                       xx111pm1oooooooo22222222xx
                       xx111111xxxxxxxx22222222xx
                       xxxxxxxxxxxxxxxxxxxxxxxxxx"""
        val nhConf = parseConf(conf)
        val nethackModelState = stateForConf(conf)
        val next = mobAction(nethackModelState, nethackModelState.mobs.first(), nhConf)
        val support = next.support().toList()
        assertEquals(1, support.size)
        assertEquals(support.first().player.health, 6)
    }

    @Test
    fun testMoveMobs() {

    }
}