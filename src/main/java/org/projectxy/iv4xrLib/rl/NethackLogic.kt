package org.projectxy.iv4xrLib.rl

import A.B.*
import arrow.core.Option
import arrow.core.toOption
import arrow.optics.*
import arrow.optics.dsl.at
import arrow.optics.typeclasses.At
import arrow.optics.typeclasses.Index
import eu.iv4xr.framework.model.distribution.*
import eu.iv4xr.framework.model.distribution.Distributions.uniform
import eu.iv4xr.framework.spatial.Vec3
import org.projectxy.iv4xrLib.NethackWrapper
import org.projectxy.iv4xrLib.NethackWrapper.Movement.*
import org.projectxy.iv4xrLib.Utils
import org.projectxy.iv4xrLib.rl.NethackModelTileType.WALKABLE
import java.lang.IllegalStateException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun observe(model: NethackModelState): Distribution<NethackModelState> {
    return always(model)
}

fun move(move: Move, model: NethackModelState, configuration: NethackModelConfiguration): Distribution<NethackModelState> {
    return moveInWorld(move, model, configuration)
}

fun useItem(item: UseItem, model: NethackModelState): Distribution<NethackModelState> {
    val inventoryItem = model.Inventory[item.inventoryIndex]
    return always(
            when (inventoryItem) {
                is Weapon -> equipWeapon(inventoryItem, model)
                is Restorable -> restoreHealth(inventoryItem, item.inventoryIndex, model)
                else -> model
            }
    )
}

fun action(interact: NethackWrapper.Interact, state: NethackModelState): Distribution<NethackModelState> {
    return when (interact) {
        NethackWrapper.Interact.PickupItem -> pickupItem(state)
        else -> always(state)
    }
}

fun pickupItem(state: NethackModelState): Distribution<NethackModelState> {
    val item = state.items.firstOrNull { Utils.sameTile(it.position, state.position) }
    val newInv = item?.let { it.item cons state.Inventory } ?: state.Inventory
    val newFloor = state.items.filter { !Utils.sameTile(it.position, state.position) }
    return always(state.copy(Inventory = newInv, items = newFloor))
}


fun moveInWorld(move: Move, model: NethackModelState, configuration: NethackModelConfiguration): Distribution<NethackModelState> {
    val x = model.position.x.toInt()
    val y = model.position.y.toInt()
    return when (move.movement) {
        UP -> moveToSquare(x, y - 1, model, configuration)
        DOWN -> moveToSquare(x, y + 1, model, configuration)
        LEFT -> moveToSquare(x - 1, y, model, configuration)
        RIGHT -> moveToSquare(x + 1, y, model, configuration)
        DONOTHING -> always(model)
    }
}

fun moveToSquare(x: Int, y: Int, model: NethackModelState, configuration: NethackModelConfiguration): Distribution<NethackModelState> {
    val mob = model.mobs.indexOfFirst { it.position.x.toInt() == x && it.position.y.toInt() == y }
    val updatedTimeStamp = model.copy(timestamp = model.timestamp + 1)
    if (mob >= 0) {
        return attackMob(mob, updatedTimeStamp)
    }
    return when (configuration.tiles[x][y].type) {
        WALKABLE -> always(NethackModelState.position.modify(updatedTimeStamp) { Vec3(x.toFloat(), y.toFloat(), 0.toFloat()) })
        NethackModelTileType.WALL -> always(updatedTimeStamp)
    }
}

fun <T, A> Lens<T, List<A>>.atIndex(index: Int) = this compose Index.list<A>().index(index)

fun attackMob(mobIndex: Int, model: NethackModelState): Distribution<NethackModelState> {
    val mob = model.mobs[mobIndex]
    val wasAlive = mob.health > 0
    val newHealth = max(0, mob.health - model.player.equippedWeaponDmg)
    val isAlive = newHealth > 0
    val newMob = mob.copy(health = newHealth, alive = newHealth > 0)
    val newModel = NethackModelState.mobs.atIndex(mobIndex).modify(model) { newMob }

    if (wasAlive && !isAlive) {
        return ifd(
                flip(0.35),
                dropLoot(mob.position).map { newModel.copy(items = it cons newModel.items) },
                always(newModel)
        )
    }
    return always(newModel)
}

fun generalLogic(nethackModelState: NethackModelState): NethackModelState {
    if (nethackModelState.timestamp % 8 == 0) return reduceHealth(nethackModelState, 1)
    return nethackModelState
}

fun reduceHealth(nethackModelState: NethackModelState, reduction: Int): NethackModelState {
    return NethackModelState.player.health.modify(nethackModelState) { max(0, it - reduction) }
            .let { new ->
                NethackModelState.player.isAlive.modify(new) { new.player.health > 0 }
            }
}

fun moveMobs(nethackModelState: NethackModelState) {

}

fun sameRoom(mobPos: Vec3, playerPos: Vec3, rooms: List<Room>): Boolean {
    val mobRoom = roomIndex(mobPos.xy(), rooms)
    val playerRoom = roomIndex(playerPos.xy(), rooms)
    return mobRoom != null && playerRoom != null && mobRoom == playerRoom
}

fun roomIndex(xy: Pair<Int, Int>, rooms: List<Room>): Int? {
    val (x, y) = xy
    return rooms.indexOfFirst {
        x > it.x &&
                x < it.x + it.sizeX &&
                y > it.y &&
                y < it.y + it.sizeY

    }.takeIf { it >= 0 }
}

fun mobAction(nethackModelState: NethackModelState, mob: NethackMob, conf: NethackModelConfiguration): Distribution<NethackModelState> {
    val (x, y) = mob.position.xy()
    val (px, py) = nethackModelState.position.xy()
    val canAttackPlayer = (abs(x - px) + abs(y - py)) <= 1
    return when {
        !mob.alive -> always(nethackModelState.copy(mobs = nethackModelState.mobs.filter { it != mob }))
        canAttackPlayer -> always(attackPlayer(nethackModelState, mob))
        sameRoom(mob.position, nethackModelState.position, conf.rooms) -> moveInRoom(mob, nethackModelState, conf)
        mob.seenPlayer -> moveTowardsPlayer(mob, nethackModelState, conf)
        else -> always(searchPlayer(mob, nethackModelState, conf))
    }
}

fun isCloser(x: Int, px: Int, dx: Int): Boolean = abs(x - px) > abs((x + dx) - px)

fun shouldMove(mob: NethackMob, nethackModelState: NethackModelState, configuration: NethackModelConfiguration, dx: Int, dy: Int): Boolean {
    val (x, y) = mob.position.xy()
    val (px, py) = nethackModelState.position.xy()
    val nx = x + dx
    val ny = y + dy
    if (nethackModelState.mobs.any { it.position.x.toInt() == nx || it.position.y.toInt() == ny }) return false
    if (configuration.tiles[nx][ny].type != WALKABLE) return false
    return isCloser(x, px, dx) || isCloser(y, py, dy)
}

fun move(priorities: List<Pair<Int, Int>>, mob: NethackMob, nethackModelState: NethackModelState, configuration: NethackModelConfiguration): NethackModelState {
    val move = priorities.firstOrNull { shouldMove(mob, nethackModelState, configuration, it.first, it.second) }
            ?: return nethackModelState
    val newMob = NethackMob.position.modify(mob) { Vec3((it.x.toInt() + move.first).toFloat(), (it.y.toInt() + move.second).toFloat(), 0f) }
    return NethackModelState.mobs.modify(nethackModelState) {
        newMob cons it.filter { it != mob }
    }
}

fun moveTowardsPlayer(mob: NethackMob, nethackModelState: NethackModelState, configuration: NethackModelConfiguration): Distribution<NethackModelState> {
    val moveX = listOf(-1 to 0, 1 to 0)
    val moveY = listOf(0 to -1, 0 to 1)
    return if_(flip(0.5)) {
        move(moveX + moveY, mob, nethackModelState, configuration)
    }.else_ {
        move(moveY + moveX, mob, nethackModelState, configuration)
    }
}

fun searchPlayer(mob: NethackMob, nethackModelState: NethackModelState, configuration: NethackModelConfiguration): NethackModelState {
    val (px, py) = nethackModelState.position.xy()
    val (mx, my) = mob.position.xy()
    val length = (0..6)
    val ranges = listOf(
            length.map { 0 to it },
            length.map { 0 to -it },
            length.map { it to 0 },
            length.map { -it to 0 },
    )
    val isPlayer = { (dx, dy): Pair<Int, Int> -> (px == (mx + dx)) || py == (my + dy) }
    val seenPlayer = ranges.any { range ->
        val hasLine = range
                .takeWhile { !isPlayer(it) }
                .all { (dx, dy) -> configuration.tiles[mx + dx][my + dy].type == WALKABLE }
        range.any { isPlayer(it) } && hasLine
    }
    return updateMob(mob, nethackModelState) { NethackMob.seenPlayer.modify(it) { it || seenPlayer } }

}

fun moveInRoom(mob: NethackMob, nethackModelState: NethackModelState, configuration: NethackModelConfiguration): Distribution<NethackModelState> {
    val newMob = NethackMob.seenPlayer.modify(mob) { true }
    return moveTowardsPlayer(newMob, updateMob(mob, nethackModelState) { newMob }, configuration)
}


fun attackPlayer(nethackModelState: NethackModelState, mob: NethackMob): NethackModelState {
    return reduceHealth(nethackModelState, mob.attackDmg)
}

fun dropLoot(location: Vec3): Distribution<NethackWorldItem> {
    val gold: Distribution<NethackWorldItem> = uniform(50..500).map { NethackWorldItem(location, ModelGold(it)) }
    val sword: Distribution<NethackWorldItem> = uniform(0..10).map { NethackWorldItem(location, Weapon(1, "Sword", it)) }
    val bow: Distribution<NethackWorldItem> = uniform(4..7).map { NethackWorldItem(location, Weapon(1, "", it)) }
    return flip(0.6).chain {
        if (it) {
            uniform(5, 8, 3).map {
                NethackWorldItem(location, Restorable(1, it))
            }
        } else {
            flip(0.5).chain {
                if (it) gold
                else
                    flip(0.5).chain {
                        if (it) sword else bow
                    }
            }
        }

    }
}

fun updateMob(mob: NethackMob, nethackModelState: NethackModelState, update: (NethackMob) -> NethackMob): NethackModelState {
    return NethackModelState.mobs.modify(nethackModelState) {
        update(mob) cons it.filter { it != mob }
    }
}


fun equipWeapon(weapon: Weapon, model: NethackModelState): NethackModelState {
    return NethackModelState.player.modify(model) {
        it.copy(equippedWeaponName = weapon.weaponName, equippedWeaponDmg = weapon.attackDmg)
    }
}

fun <T, V> Lens<T, Map<String, V>>.at(string: String): Lens<T, Option<V>> {
    return at(At.map(), string)
}

fun restoreHealth(restorable: Restorable, index: Int, model: NethackModelState): NethackModelState {
    return NethackModelState.player.health.modify(model) {
        min(model.player.maxhealth, (it) + restorable.restoreAmount)
    }.let {
        NethackModelState.Inventory.atIndex(index).modify(model) {
            NethackItem.restorable.modify(it) { it.copy(amount = it.amount - 1) }
        }
    }
}