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
import java.lang.IllegalStateException
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
//        return attackMob(mob, updatedTimeStamp)
    }
    return when (configuration.tiles[x][y].type) {
        NethackModelTileType.WALKABLE -> always(NethackModelState.position.modify(updatedTimeStamp) { Vec3(x.toFloat(), y.toFloat(), 0.toFloat()) })
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
    val newLives = if (nethackModelState.timestamp % 8 == 0) NethackModelState.player.health.modify(nethackModelState) { max(0, it - 1) } else nethackModelState
    return NethackModelState.player.isAlive.modify(newLives) { newLives.player.health > 0 }
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

fun moveInInventoryScreen(move: Move, model: NethackModelState): Distribution<NethackModelState> {
    TODO()
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