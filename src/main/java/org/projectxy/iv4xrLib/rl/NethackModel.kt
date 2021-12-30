package org.projectxy.iv4xrLib.rl

import A.B.*
import arrow.optics.optics
import eu.iv4xr.framework.mainConcepts.WorldEntity
import eu.iv4xr.framework.mainConcepts.WorldModel
import eu.iv4xr.framework.model.ProbabilisticModel
import eu.iv4xr.framework.model.distribution.Distribution
import eu.iv4xr.framework.model.distribution.always
import eu.iv4xr.framework.model.rl.*
import eu.iv4xr.framework.model.rl.approximation.*
import eu.iv4xr.framework.model.rl.components.Image
import eu.iv4xr.framework.model.rl.components.Visualizer
import eu.iv4xr.framework.model.rl.valuefunctions.Valuefunction
import eu.iv4xr.framework.spatial.Vec3
import nl.uu.cs.aplib.mainConcepts.SimpleState
import org.projectxy.iv4xrLib.*
import org.projectxy.iv4xrLib.rl.NethackModelTileType.*
import org.tensorflow.ndarray.Shape
import org.tensorflow.ndarray.Shape.UNKNOWN_SIZE
import java.awt.Color
import javax.swing.JFrame
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.system.exitProcess


// Actions
sealed class NethackModelAction : Identifiable

object Observe : NethackModelAction()
object StartNewGame : NethackModelAction()
object RestartGame : NethackModelAction()

data class UseItem(val inventoryIndex: Int) : NethackModelAction()

data class Move(val movement: NethackWrapper.Movement) : NethackModelAction()

data class Action(val interact: NethackWrapper.Interact) : NethackModelAction()

data class Position(val position: Vec3, val extent: Vec3, val velocity: Vec3)

val nethackActionFactory = OneHot<NethackModelAction>(sequence {
    yieldAll(NethackWrapper.Movement.values().map { Move(it) })
//    yieldAll(NethackWrapper.Interact.values().map { Action(it) }
}.toList())


sealed class Mode
class ValuedMode(val episodes: List<Int>) : Mode()
object PerformanceMode : Mode()

data class NethackSolveInput(
        val name: String,
        val seed: Long,
        val nethackConfiguration: NethackConfiguration,
        val enabledActions: List<NethackModelAction>,
        val mode: Mode
)

data class NethackSolveConfiguration(
        val nethackConfiguration: NethackConfiguration,
        val random: Random,
        val mdp: MDP<StateWithGoalProgress<NethackModelState>, NethackModelAction>,
        val state: MyAgentState,
        val agent: RLAgent<NethackModelState, NethackModelAction>
)

data class NethackSolveOutput(
        val name: String,
        val episodes: Int,
        val gamma: Float,
        val epsilon: Double?,
        val n: Int?
)

data class NethackSolverCallback(
        val valueFunction: Valuefunction<NethackModelState>,
        val visitFunction: Valuefunction<NethackModelState>,
        val episodes: Int
)

interface NethackSolver {
    val name: String
    fun train(configuration: NethackSolveConfiguration, episodes: List<Int>, callback: (NethackSolverCallback) -> Unit): NethackSolveOutput
    fun train(configuration: NethackSolveConfiguration): NethackSolveOutput
}

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
    companion object {
        val factory = CompositeFeature<NethackPlayer>(listOf(
                OneHot(listOf("Sword", "Bow")).from { it.equippedWeaponName },
                IntFeature.from { it.equippedWeaponDmg },
                IntFeature.from { it.currentLevel },
                IntFeature.from { it.health },
                IntFeature.from { it.maxhealth },
                BoolFeature.from { it.isAlive },
                BoolFeature.from { it.aimingBow },
        ))
    }
}

class NethackVisualizer(val configuration: NethackModelConfiguration, val state: NethackModelState, val interpolator: (Float) -> Float) : Visualizer<NethackModelState> {
    override fun visualize(valueFunction: Valuefunction<NethackModelState>): Image {
        val tiles = configuration.tiles.map {
            it.map {
                when (it.type) {
                    WALL -> Wall(it.x, it.y)
                    WALKABLE -> FloorTile(it.x, it.y)
                }
            }.toMutableList()
        }
        val values = configuration.tiles.flatMap {
            it.map {
                valueFunction.value(state.copy(position = Vec3(it.x.toFloat(), it.y.toFloat(), 0f)))
            }
        }
        val min = values.minOf { it }
        val max = values.maxOf { it }
        for (item in state.items) {
            val x = item.position.x.toInt()
            val y = item.position.y.toInt()
            tiles[x][y] = ItemTile(
                    when (item.item) {
                        is ModelGold -> Gold(1)
                        is Weapon -> Sword("", 0)
                        is Restorable -> Food()
                    },
                    x,
                    y,
            )
        }
        for (mob in state.mobs) {
            val x = mob.position.x.toInt()
            val y = mob.position.y.toInt()
            tiles[x][y] = Monster(x, y)
        }
        tiles[state.stairs.x.toInt()][state.stairs.y.toInt()] = StairTile(state.stairs.x.toInt(), state.stairs.y.toInt())
        tiles[state.position.x.toInt()][state.position.y.toInt()] = Player(state.position.x.toInt(), state.position.y.toInt())
        return NethackVisualization(tiles, InterpolatingColorer(
                interpolator, min, max,
                Color.RED, Color.GREEN
        ) { x, y ->
            valueFunction.value(state.copy(position = Vec3(x.toFloat(), y.toFloat(), 0f)))
        })
    }
}


@optics
data class NethackModelState(
        val player: NethackPlayer,
        val Inventory: List<NethackItem>,
        val items: List<NethackWorldItem>,
        val position: Vec3,
        val timestamp: Int,
        val stairs: Vec3,
        val mobs: List<NethackMob>
) : FeatureOwner<NethackModelState>(factory) {
    companion object {
        val factory = CompositeFeature<NethackModelState>(listOf(
                NethackPlayer.factory.from { it.player },
                IntFeature.from { it.timestamp },
                Vec3Feature.from { it.position },
                Vec3Feature.from { it.stairs },
                RepeatedFeature(10, NethackMob.factory).from { it.mobs },
                RepeatedFeature(20, NethackWorldItem.factory).from { it.items },
                RepeatedFeature(20, NethackItem.factory).from { it.Inventory },
        ))

        fun visualizer(configuration: NethackModelConfiguration) {

        }

        fun tensorFactory(configuration: NethackModelConfiguration) = GridEncoder<NethackModelState>(Shape.of(UNKNOWN_SIZE, configuration.columns.toLong(), configuration.rows.toLong(), 3)) {
            sequence {
                yield(longArrayOf(it.position.x.toLong(), it.position.y.toLong(), 0))
                for (x in 0 until configuration.columns) {
                    for (y in 0 until configuration.rows) {
                        val tile = configuration.tiles[x][y]
                        when (tile.type) {
                            WALL -> yield(longArrayOf(tile.x.toLong(), tile.y.toLong(), 1))
                            WALKABLE -> yield(longArrayOf(tile.x.toLong(), tile.y.toLong(), 2))
                        }
                    }
                }
            }
        }


        fun factoryFrom(configuration: NethackModelConfiguration) = CompositeFeature<NethackModelState>(listOf(
                OneHot(configuration.walkableTiles()).from { NethackModelTile(it.position.x.toInt(), it.position.y.toInt(), WALKABLE) },
//                DoubleFeature.from { it.timestamp / 100.0 }
//                NethackPlayer.factory.from { it.player },
//                IntFeature.from { it.timestamp },
//                Vec3Feature.from { it.position },
//                Vec3Feature.from { it.stairs },
//                RepeatedFeature(10, NethackMob.factory).from { it.mobs },
//                RepeatedFeature(20, NethackWorldItem.factory).from { it.items },
//                RepeatedFeature(20, NethackItem.factory).from { it.Inventory },
        ))
    }

    override fun toString() = position.toString() + ",steps:$timestamp"
}


@optics
data class NethackMob(
        val position: Vec3,
        val health: Int,
        val attackDmg: Int,
        val alive: Boolean,
        val seenPlayer: Boolean,
        val waitTurn: Boolean
) {
    companion object {
        val factory = CompositeFeature<NethackMob>(listOf(
                Vec3Feature.from { it.position },
                IntFeature.from { it.health },
                IntFeature.from { it.attackDmg },
                BoolFeature.from { it.alive },
                BoolFeature.from { it.seenPlayer },
                BoolFeature.from { it.waitTurn }
        ))

        fun factoryFrom(configuration: NethackModelConfiguration) = CompositeFeature<NethackMob>(listOf(

                Vec3Feature.from { it.position },
                IntFeature.from { it.health },
                IntFeature.from { it.attackDmg },
                BoolFeature.from { it.alive },
                BoolFeature.from { it.seenPlayer },
                BoolFeature.from { it.waitTurn }
        ))
    }
}


@optics
sealed class NethackItem {
    companion object {
        val factory = EncodedSumType(
                listOf(
                        Weapon::class with CompositeFeature(listOf(
                                IntFeature.from { it.amount },
                                IntFeature.from { it.attackDmg }
                        )),
                        Restorable::class with CompositeFeature(listOf(
                                IntFeature.from { it.amount },
                                IntFeature.from { it.restoreAmount }
                        )),
                        ModelGold::class with CompositeFeature(listOf(
                                IntFeature.from { it.amount }
                        )),
                )
        )
    }
}

data class NethackWorldItem(val position: Vec3, val item: NethackItem) {
    companion object {
        val factory = CompositeFeature<NethackWorldItem>(listOf(
                Vec3Feature.from { it.position },
                NethackItem.factory.from { it.item }
        ))
    }
}


data class Weapon(val amount: Int, val weaponName: String, val attackDmg: Int) : NethackItem()

data class Restorable(val amount: Int, val restoreAmount: Int) : NethackItem()

data class ModelGold(val amount: Int) : NethackItem()

data class NethackModelTile(val x: Int, val y: Int, val type: NethackModelTileType)
enum class NethackModelTileType {
    WALL, WALKABLE
}

data class NethackModelConfiguration(val rows: Int, val columns: Int, val tiles: Array<Array<NethackModelTile>>, val inventorySize: Int, val initialState: NethackModelState, val rooms: List<Room>) {
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

    fun walkableTiles() = tiles
            .flatMapIndexed { x, tiles ->
                tiles.mapIndexedNotNull { y, tile ->
                    if (tile.type == WALKABLE) tile
                    else null
                }
            }
}

private fun parseItem(entity: WorldEntity): NethackItem? {
    return when (entity.type) {
//                Floor items
        A.B.Weapon::class.java.simpleName -> entity.become(Weapon::class)
        A.B.HealthPotion::class.java.simpleName -> entity.become(Restorable::class)
        A.B.Water::class.java.simpleName -> entity.become(Restorable::class)
        A.B.Food::class.java.simpleName -> entity.become(Restorable::class)
        A.B.Gold::class.java.simpleName -> entity.become(ModelGold::class)
        else -> null
    }
}


fun <T : Any> WorldEntity.become(clazz: KClass<T>): T {

    val primaryConstructor = clazz.primaryConstructor ?: error("No constructor found")
    val args = primaryConstructor.parameters.map {
        if (it.name == "position") this.position
        else if (it.name == "id") this.id
        else this.getProperty(it.name)
    }
    return primaryConstructor.call(*args.toTypedArray())
}

fun WorldModel.toNethackState(): NethackModelState {
    val worldModel = this
    val player = worldModel.getElement("player").become(NethackPlayer::class)
    val floorItems = mutableListOf<NethackWorldItem>()
    val mobs = mutableListOf<NethackMob>()
    worldModel.elements.forEach { (id, entity) ->
        parseItem(entity)?.also { floorItems.add(NethackWorldItem(entity.position, it)) }
        when (entity.type) {
//                Mobs
            A.B.Monster::class.java.simpleName -> mobs.add(entity.become(NethackMob::class))
        }
    }
    val inventoryItems = worldModel.getElement("Inventory").elements.values.mapNotNull { parseItem(it) }
    return NethackModelState(player, inventoryItems, floorItems, worldModel.position, worldModel.timestamp.toInt(), worldModel.getElement("Stairs").position, mobs)
}

/**
 * The nethack state includes all mobs, items and player properties
 * The configuration includes the layout of the game. This means the location of walkable tiles and walls
 */
class NethackModel(private val configuration: NethackModelConfiguration) : ProbabilisticModel<NethackModelState, NethackModelAction> {
    override fun possibleStates(): Sequence<NethackModelState> {
        TODO("Not yet implemented")
    }

    override fun possibleActions(state: NethackModelState): Sequence<NethackModelAction> {
        return sequence {
//            yield(Observe)
//            yieldAll((state.Inventory.indices).map { UseItem(it) })
            yield(Move(NethackWrapper.Movement.LEFT))
            yield(Move(NethackWrapper.Movement.UP))
            yield(Move(NethackWrapper.Movement.DOWN))
            yield(Move(NethackWrapper.Movement.RIGHT))
//            yieldAll(listOf(PickupItem).map { Action(it) })
        }
    }

    override fun executeAction(action: NethackModelAction, state: SimpleState): Any {
        val wrapper = (state as MyAgentState).env().nethackUnderTest
        return when (action) {
            Observe -> wrapper.observe()
            StartNewGame -> TODO()
            RestartGame -> TODO()
            is UseItem -> wrapper.useItem(action.inventoryIndex)
            is Move -> wrapper.move(action.movement)
            is Action -> wrapper.action(action.interact, "")
        }
    }


    override fun convertState(state: SimpleState): NethackModelState {
        if (state !is MyAgentState) error("Unexpected state")
        return state.wom.toNethackState()
    }


    override fun isTerminal(state: NethackModelState): Boolean {
        return !state.player.isAlive || Vec3.dist(state.position, state.stairs) < 0.01
    }

    override fun transition(current: NethackModelState, action: NethackModelAction): Distribution<NethackModelState> {
        return when (action) {
            Observe -> observe(current)
            StartNewGame -> error("Not supported")
            RestartGame -> error("Not supported")
            is UseItem -> useItem(action, current)
            is Move -> move(action, current, configuration)
            is Action -> action(action.interact, current)
        }.map {
            generalLogic(it)
        }
    }

    override fun proposal(current: NethackModelState, action: NethackModelAction, result: NethackModelState): Distribution<out Any> {
        return always(result)
    }

    override fun possibleActions(): Sequence<NethackModelAction> {
        return sequence {
//            yield(Observe)
//            yieldAll((0..configuration.inventorySize).map { UseItem(it) })
            yield(Move(NethackWrapper.Movement.LEFT))
            yield(Move(NethackWrapper.Movement.UP))
            yield(Move(NethackWrapper.Movement.DOWN))
            yield(Move(NethackWrapper.Movement.RIGHT))
//            yieldAll(NethackWrapper.Interact.values().map { Action(it) })
        }
    }

    override fun initialState(): Distribution<NethackModelState> {
        return always(configuration.initialState)
    }
}

fun MyAgentState.getConf(): NethackModelConfiguration {
    val nh = this.env().nethackUnderTest.nethack
    val tiles = nh.tiles.map { it.map { if (it is Wall) NethackModelTile(it.x, it.y, WALL) else NethackModelTile(it.x, it.y, WALKABLE) }.toTypedArray() }.toTypedArray()
    return NethackModelConfiguration(nh.rows, nh.cols, tiles, 20, this.wom.toNethackState(), nh.roomArr)
}

fun add(vec3: Vec3, x: Int, y: Int) = Vec3(vec3.x.toInt().plus(x).toFloat(), vec3.y.toInt().plus(y).toFloat(), vec3.z)