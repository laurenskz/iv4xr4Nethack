package org.projectxy.iv4xrLib.rl

import A.B.Weapon
import arrow.optics.optics
import eu.iv4xr.framework.model.ProbabilisticModel
import eu.iv4xr.framework.model.distribution.Distribution
import eu.iv4xr.framework.model.rl.Identifiable
import eu.iv4xr.framework.model.rl.burlapadaptors.DataClassHashableState
import eu.iv4xr.framework.spatial.Vec3
import nl.uu.cs.aplib.mainConcepts.SimpleState
import org.projectxy.iv4xrLib.NethackWrapper
import java.io.Serializable
import java.lang.IllegalStateException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties


// Actions
sealed class NethackModelAction : Identifiable

object Observe : NethackModelAction()
object StartNewGame : NethackModelAction()
object RestartGame : NethackModelAction()

data class UseItem(val inventoryID: String) : NethackModelAction()

data class Move(val movement: NethackWrapper.Movement) : NethackModelAction()

data class Action(val interact: NethackWrapper.Interact) : NethackModelAction()

data class Position(val position: Vec3, val extent: Vec3, val velocity: Vec3)


@optics
data class NethackPlayer(
        val equippedWeaponName: String,
        val equippedWeaponDmg: Int,
        val currentLevel: Int,
        val health: Int,
        val maxhealth: Int,
        val isAlive: Boolean,
        val aimingBow: Boolean
) {
    companion object
}

@optics
data class NethackModelState(val player: NethackPlayer, val Inventory: Map<String, NethackItem>) {
    companion object
}

@optics
sealed class NethackItem(open val amount: Int, open val id: String) {
    companion object
}


data class Weapon(override val amount: Int, override val id: String, val weaponName: String, val attackDmg: Int) : NethackItem(amount, id)
data class Restorable(override val amount: Int, override val id: String, val restoreAmount: Int) : NethackItem(amount, id)


data class RLWorldEntity(val id: String, val type: String, val position: Position, val elements: Map<String, RLWorldEntity>, val properties: Map<String, Serializable>) {


    fun updateProperty(name: String, value: Serializable?): RLWorldEntity {
        if (value == null) return this
        return copy(properties = properties + mapOf(name to value))
    }

    fun modifyProperty(name: String, update: (Serializable) -> Serializable): RLWorldEntity {
        return properties[name]?.let {
            updateProperty(name, update(it))
        } ?: this
    }

    fun modifyElement(name: String, update: (RLWorldEntity) -> RLWorldEntity): RLWorldEntity {
        val element = elements[name] ?: return this
        return copy(elements = elements + mapOf(name to update(element)))
    }
}

data class RLWorldModel(val agentId: String, val position: Position, val elements: Map<String, RLWorldEntity>) : DataClassHashableState() {
    fun modifyElement(name: String, update: (RLWorldEntity) -> RLWorldEntity): RLWorldModel {
        val element = elements[name] ?: return this
        return copy(elements = elements + mapOf(name to update(element)))
    }
}


interface Holder<T> {
    fun accumulate(t: T): T
}

@Suppress("UNCHECKED_CAST")
class RLEntityPropertyProp<T : Serializable>(model: RLWorldEntity) : Holder<RLWorldEntity>, DictProp<T>({
    model.properties[it] as T
}) {

    override fun accumulate(t: RLWorldEntity): RLWorldEntity {
        return commit(t) { n, value, v -> v.updateProperty(n, value) }
    }
}

class RLEntityProp<T : Holder<RLWorldEntity>>(model: RLWorldEntity, read: (RLWorldEntity) -> T) : Holder<RLWorldEntity>, DictProp<T>({
    read(model.elements[it] ?: error("Malformed model"))

}) {
    override fun accumulate(t: RLWorldEntity): RLWorldEntity {
        return commit(t) { name, childValue, v ->
            v.modifyElement(name) {
                childValue.accumulate(t.elements[name] ?: error("Malformed model"))
            }
        }
    }
}

class RLWorldProp<T : Holder<RLWorldEntity>>(model: RLWorldModel, read: (RLWorldEntity) -> T) : Holder<RLWorldModel>, DictProp<T>({
    read(model.elements[it] ?: error("Malformed model"))

}) {
    override fun accumulate(t: RLWorldModel): RLWorldModel {
        return commit(t) { name, childValue, v ->
            v.modifyElement(name) {
                childValue.accumulate(t.elements[name] ?: error("Malformed model"))
            }
        }
    }
}

open class DictProp<T>(val read: (String) -> T) : ReadWriteProperty<Any, T> {

    var cache: T? = null
    var name: String? = null
    var dirty = false

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        cache = value
        dirty = true
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return cache ?: read(property.name)
    }

    fun <V> commit(v: V, commitment: (String, T, V) -> V): V {
        return cache?.let { c ->
            name?.let { n ->
                commitment(n, c, v)
            }
        } ?: v
    }
}

@Suppress("UNCHECKED_CAST")
open class CompositeHolder<T> : Holder<T> {
    override fun accumulate(t: T): T {
        return this::class.memberProperties.fold(t) { curr, prop ->
            if (prop is Holder<*>) {
                (prop as Holder<T>).accumulate(curr)
            } else curr
        }
    }
}

open class FinalHolder<T>(val t: T) : CompositeHolder<T>() {
    fun getValue(): T {
        return accumulate(t)
    }
}

class PlayerStatusModel(entity: RLWorldEntity) : CompositeHolder<RLWorldEntity>() {
    var equippedWeaponName: String by RLEntityPropertyProp(entity)
    var equippedWeaponDmg: Int by RLEntityPropertyProp(entity)
}

interface Usable<T> {
    fun use(t: T): T
}


//data class Bab(val equippedWeaponName)

class NetHackWorldModel(val model: RLWorldModel) : FinalHolder<RLWorldModel>(model) {
    val playerStatus by RLWorldProp(model, ::PlayerStatusModel)


}

enum class NethackModelTile {
    WALL, WALKABLE
}

data class NethackModelConfiguration(val rows: Int, val columns: Int, val tiles: Array<Array<NethackModelTile>>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NethackModelConfiguration

        if (rows != other.rows) return false
        if (columns != other.columns) return false
        if (!tiles.contentDeepEquals(other.tiles)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + columns
        result = 31 * result + tiles.contentDeepHashCode()
        return result
    }
}

/**
 * The nethack state includes all mobs, items and player properties
 * The configuration includes the layout of the game. This means the location of walkable tiles and walls
 */
class NethackModel(private val configuration: NethackModelConfiguration) : ProbabilisticModel<RLWorldModel, NethackModelAction> {
    override fun possibleStates(): Sequence<RLWorldModel> {
        TODO("Not yet implemented")
    }

    override fun possibleActions(state: RLWorldModel): Sequence<NethackModelAction> {
        val usages = state.elements["Inventory"]?.elements?.values?.map { UseItem(it.id) } ?: emptyList()
        return usages.asSequence()
    }

    override fun executeAction(action: NethackModelAction, state: SimpleState): Any {
        TODO("Not yet implemented")
    }

    override fun convertState(state: SimpleState): RLWorldModel {
        TODO("Not yet implemented")
    }

    override fun isTerminal(state: RLWorldModel): Boolean {
        TODO("Not yet implemented")
    }

    override fun transition(current: RLWorldModel, action: NethackModelAction): Distribution<RLWorldModel> {
        return when (action) {
            Observe -> observe(current)
            StartNewGame -> TODO()
            RestartGame -> TODO()
            is UseItem -> TODO()
            is Move -> TODO()
            is Action -> TODO()
        }
    }

    override fun proposal(current: RLWorldModel, action: NethackModelAction, result: RLWorldModel): Distribution<out Any> {
        TODO("Not yet implemented")
    }

    override fun possibleActions(): Sequence<NethackModelAction> {
        TODO("Not yet implemented")
    }

    override fun initialState(): Distribution<RLWorldModel> {
        TODO("Not yet implemented")
    }
}