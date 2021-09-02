package org.projectxy.iv4xrLib.rl

import arrow.optics.optics
import eu.iv4xr.framework.model.ProbabilisticModel
import eu.iv4xr.framework.model.distribution.Distribution
import eu.iv4xr.framework.model.rl.Identifiable
import eu.iv4xr.framework.model.rl.burlapadaptors.DataClassHashableState
import eu.iv4xr.framework.spatial.Vec3
import nl.uu.cs.aplib.mainConcepts.SimpleState
import org.projectxy.iv4xrLib.NethackWrapper
import java.io.Serializable
import java.time.temporal.TemporalAmount


// Actions
sealed class NethackModelAction : Identifiable

object Observe : NethackModelAction()
object StartNewGame : NethackModelAction()
object RestartGame : NethackModelAction()

data class UseItem(val inventoryIndex: Int) : NethackModelAction()

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
data class NethackModelState(
        val player: NethackPlayer,
        val Inventory: List<NethackItem>,
        val items: List<NethackItem>,
        val position: Vec3,
        val timestamp: Int,
        val stairs: Position,
        val mobs: List<NethackMob>
) {
    companion object
}

data class WorldElementProperties(
        val position: Vec3,
        val id: String,
        val type: String
)

data class NethackMob(
        val position: Vec3,
        val health: Int,
        val attackDmg: Int,
        val alive: Boolean,
        val seenPlayer: Boolean,
        val waitTurn: Boolean
)

@optics
sealed class NethackItem(open val position: Vec3) {
    companion object
}


data class Weapon(override val position: Vec3, val type: String, val amount: Int, val weaponName: String, val attackDmg: Int) : NethackItem(position)
data class Restorable(override val position: Vec3, val amount: Int, val restoreAmount: Int) : NethackItem(position)
data class ModelGold(override val position: Vec3, val amount: Int) : NethackItem(position)

data class RLWorldEntity(val id: String, val type: String, val position: Position, val elements: Map<String, RLWorldEntity>, val properties: Map<String, Serializable>)

data class RLWorldModel(val agentId: String, val position: Position, val elements: Map<String, RLWorldEntity>) : DataClassHashableState()

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
//        val usages = (0..state) { UseItem(it.id) } ?: emptyList()
//        return usages.asSequence()
        return emptySequence()
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