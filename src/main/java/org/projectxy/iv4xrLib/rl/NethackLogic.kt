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
import java.lang.IllegalStateException
import kotlin.math.max
import kotlin.math.min

fun observe(model: RLWorldModel): Distribution<RLWorldModel> {
    return always(model)
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
    if (mob >= 0) {
        return attackMob(mob, model)
    }
    return when (configuration.tiles[x][y]) {
        NethackModelTile.WALKABLE -> always(NethackModelState.position.modify(model) { Vec3(x.toFloat(), y.toFloat(), 0.toFloat()) })
        NethackModelTile.WALL -> always(model)
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

fun dropLoot(location: Vec3): Distribution<NethackItem> {
    val gold: Distribution<NethackItem> = uniform(50..500).map { ModelGold(location, it) }
    val sword: Distribution<NethackItem> = uniform(0..10).map { Weapon(location, "Sword", 1, "", it) }
    val bow: Distribution<NethackItem> = uniform(4..7).map { Weapon(location, "Bow", 1, "", it) }
    val x: Distribution<out NethackItem> = flip(0.5).chain { if (it) gold else bow }
    return flip(0.6).chain {
        if (it) {
            uniform(5, 8, 3).map {
                Restorable(location, 1, it)
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

fun moveInInventoryScreen(move: Move, model: RLWorldModel): Distribution<RLWorldModel> {
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