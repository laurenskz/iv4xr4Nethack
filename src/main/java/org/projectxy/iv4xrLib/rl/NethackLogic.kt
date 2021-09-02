package org.projectxy.iv4xrLib.rl

import A.B.*
import arrow.core.Option
import arrow.core.toOption
import arrow.optics.Lens
import arrow.optics.POptional
import arrow.optics.PPrism
import arrow.optics.dsl.at
import arrow.optics.typeclasses.At
import eu.iv4xr.framework.model.distribution.Distribution
import eu.iv4xr.framework.model.distribution.always
import org.projectxy.iv4xrLib.NethackWrapper
import org.projectxy.iv4xrLib.NethackWrapper.Movement.*
import java.lang.IllegalStateException
import kotlin.math.max
import kotlin.math.min

fun observe(model: RLWorldModel): Distribution<RLWorldModel> {
    return always(model)
}

fun useItem(item: UseItem, model: NethackModelState): Distribution<NethackModelState> {
    val inventoryItem = model.Inventory[item.inventoryID] ?: return always(model)
    return always(
            when (inventoryItem) {
                is Weapon -> equipWeapon(inventoryItem, model)
                is Restorable -> restoreHealth(inventoryItem, model)
                else -> model
            }
    )
}


fun moveInWorld(move: Move, model: NethackModelState): Distribution<RLWorldModel> {
    return when (move.movement) {
        UP -> TODO()
        DOWN -> TODO()
        LEFT -> TODO()
        RIGHT -> TODO()
        DONOTHING -> TODO()
    }
}

fun moveToSquare(x: Int, y: Int, model: RLWorldModel, configuration: NethackModelConfiguration): Distribution<RLWorldModel> {
    val px = model.position.position.x.toInt()
    val py = model.position.position.y.toInt()
    val tile = configuration.tiles[x][y]
    val mob = model.elements.values.firstOrNull { it.position.position.x.toInt() == x && it.position.position.y.toInt() == y && it.properties["attackDmg"] != null }
    if (mob != null) {

    }
    TODO()
}

fun attackMob(mob: RLWorldEntity, model: RLWorldModel) {
    val mobHealth = mob.properties["health"] ?: return
    model
    model.elements[model.agentId]
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

fun restoreHealth(restorable: Restorable, model: NethackModelState): NethackModelState {
    return NethackModelState.player.health.modify(model) {
        min(model.player.maxhealth, (it) + restorable.amount)
    }.let {
        NethackModelState.Inventory.at(restorable.id).modify(model) {
            it.map { NethackItem.restorable.modify(it) { it.copy(amount = it.amount - 1) } }
        }
    }
}