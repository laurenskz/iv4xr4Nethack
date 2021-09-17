package org.projectxy.iv4xrLib.rl

import arrow.optics.Lens
import arrow.optics.optics
import eu.iv4xr.framework.model.ProbabilisticModel
import eu.iv4xr.framework.model.distribution.Distribution
import eu.iv4xr.framework.model.rl.Identifiable
import eu.iv4xr.framework.model.rl.burlapadaptors.DataClassHashableState
import eu.iv4xr.framework.spatial.Vec3
import nl.uu.cs.aplib.mainConcepts.SimpleState
import org.projectxy.iv4xrLib.MyAgentState
import org.projectxy.iv4xrLib.NethackWrapper
import java.io.Serializable
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.reflect.safeCast


// Actions
sealed class NethackModelAction : Identifiable

object Observe : NethackModelAction()
object StartNewGame : NethackModelAction()
object RestartGame : NethackModelAction()

data class UseItem(val inventoryIndex: Int) : NethackModelAction()

data class Move(val movement: NethackWrapper.Movement) : NethackModelAction()

data class Action(val interact: NethackWrapper.Interact) : NethackModelAction()

data class Position(val position: Vec3, val extent: Vec3, val velocity: Vec3)

interface FeatureVectorFactory<T> {
    fun features(t: T): FloatArray = FloatArray(count()).also { setFrom(t, it, 0) }
    fun setFrom(t: T, result: FloatArray, start: Int)
    fun count(): Int
}

typealias FeatureActionFactory<S, A> = FeatureVectorFactory<Pair<S, A>>

class MergedFeatureFactory<T, A>(val first: FeatureVectorFactory<T>, val second: FeatureVectorFactory<A>) : FeatureVectorFactory<Pair<T, A>> {
    override fun setFrom(t: Pair<T, A>, result: FloatArray, start: Int) {
        first.setFrom(t.first, result, start)
        second.setFrom(t.second, result, start + first.count())
    }

    override fun count(): Int {
        return first.count() + second.count()
    }
}

open class FeatureOwner<T>(val factory: FeatureVectorFactory<T>) : Identifiable

fun <T : FeatureOwner<T>> T.features() = factory.features(this)

class OneHot<T>(val ts: List<T>) : FeatureVectorFactory<T> {

    override fun setFrom(t: T, result: FloatArray, start: Int) {
        ts.forEachIndexed { i, tp -> result[i + start] = if (t == tp) 1f else 0f }
    }

    override fun count(): Int {
        return ts.count()
    }
}

open class PrimitiveFeature<T>(val toFloat: (T) -> Float) : FeatureVectorFactory<T> {

    override fun setFrom(t: T, result: FloatArray, start: Int) {
        result[start] = toFloat(t)
    }

    override fun count(): Int {
        return 1
    }
}

class ExtractFeature<T, V>(val factory: FeatureVectorFactory<V>, val lens: (T) -> V) : FeatureVectorFactory<T> {
    override fun features(t: T): FloatArray {
        return factory.features(lens(t))
    }

    override fun setFrom(t: T, result: FloatArray, start: Int) {
        return factory.setFrom(lens(t), result, start)
    }

    override fun count(): Int {
        return factory.count()
    }
}


class LensFeature<T, V>(val lens: Lens<T, V>, val factory: FeatureVectorFactory<V>) : FeatureVectorFactory<T> {
    override fun features(t: T): FloatArray {
        return factory.features(lens.get(t))
    }

    override fun setFrom(t: T, result: FloatArray, start: Int) {
        return factory.setFrom(lens.get(t), result, start)
    }

    override fun count(): Int {
        return factory.count()
    }
}

open class CompositeFeature<T>(val featureVectorFactories: List<FeatureVectorFactory<T>>) : FeatureVectorFactory<T> {

    override fun setFrom(t: T, result: FloatArray, start: Int) {
        var count = 0
        for (featureVectorFactory in featureVectorFactories) {
            featureVectorFactory.setFrom(t, result, start + count)
            count += featureVectorFactory.count()
        }
    }

    override fun count(): Int {
        return featureVectorFactories.sumBy { it.count() }
    }
}

fun <T, V> FeatureVectorFactory<V>.from(f: (T) -> V) = ExtractFeature(this, f)

object IntFeature : PrimitiveFeature<Int>({ it.toFloat() })
object BoolFeature : PrimitiveFeature<Boolean>({ if (it) 1f else 0f })

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

class RepeatedFeature<T>(val repetitions: Int, val factory: FeatureVectorFactory<T>) : FeatureVectorFactory<List<T>> {

    override fun setFrom(t: List<T>, result: FloatArray, start: Int) {
        for (i in (0..max(repetitions, t.count()))) {
            factory.setFrom(t[i], result, start + i * factory.count())
        }
    }

    override fun count(): Int {
        return this.repetitions * factory.count()
    }
}

@optics
data class NethackModelState(
        val player: NethackPlayer,
        val Inventory: List<NethackItem>,
        val items: List<NethackItem>,
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
                RepeatedFeature(20, NethackItem.factory).from { it.items },
                RepeatedFeature(20, NethackItem.factory).from { it.Inventory },
        ))
    }
}


object Vec3Feature : FeatureVectorFactory<Vec3> {

    override fun setFrom(t: Vec3, result: FloatArray, start: Int) {
        result[0] = t.x
        result[1] = t.y
        result[2] = t.z
    }

    override fun count() = 3
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
    }
}

infix fun <T : Any, V : T> KClass<V>.with(factory: FeatureVectorFactory<V>) = OptionalFeatureFactory<T, V>(this, factory)

@optics
sealed class NethackItem(open val position: Vec3) {
    companion object {
        val factory = EncodedSumType(
                listOf(
                        Weapon::class with Weapon.factory,
                        Restorable::class with Restorable.factory,
                        ModelGold::class with ModelGold.factory,
                )
        )
    }
}

class OptionalFeatureFactory<T : Any, V : T>(val clazz: KClass<V>, val factory: FeatureVectorFactory<V>) : FeatureVectorFactory<T> {

    override fun setFrom(t: T, result: FloatArray, start: Int) {
        clazz.safeCast(t)?.also {
            factory.setFrom(it, FloatArray(0), 0)
        }
    }

    override fun count(): Int {
        return factory.count()
    }
}

/**
 * Encodes the type of the class as well as the representation given by the factory, note that
 */
class EncodedSumType<T : Any>(factories: List<OptionalFeatureFactory<T, *>>) : CompositeFeature<T>(listOf(
        OneHot(factories.map { it.clazz }).from { it::class },
        SumTypeFeatureFactory(factories)
))

class SumTypeFeatureFactory<T : Any>(val factories: List<OptionalFeatureFactory<T, *>>) : FeatureVectorFactory<T> {

    val map: Map<KClass<*>, FeatureVectorFactory<T>> = factories.associate { it.clazz to it }
    val totalCount = factories.sumBy { it.count() }
    val counts = factories.scan(0) { acc, fac -> acc + fac.factory.count() }
    val offsets = factories.mapIndexed { idx, fac -> fac.clazz to counts[idx] }.toMap()


    override fun setFrom(t: T, result: FloatArray, start: Int) {
        map[t::class]?.setFrom(t, result, start + (offsets[t::class] ?: error("Unrecognized class")))
    }

    override fun count() = totalCount
}


data class Weapon(override val position: Vec3, val type: String, val amount: Int, val weaponName: String, val attackDmg: Int) : NethackItem(position) {
    companion object {
        val factory = CompositeFeature<Weapon>(listOf(
                Vec3Feature.from { it.position },
                IntFeature.from { it.amount },
                IntFeature.from { it.attackDmg }
        ))
    }
}

data class Restorable(override val position: Vec3, val amount: Int, val restoreAmount: Int) : NethackItem(position) {
    companion object {
        val factory = CompositeFeature<Restorable>(listOf(
                Vec3Feature.from { it.position },
                IntFeature.from { it.amount },
                IntFeature.from { it.restoreAmount }
        ))
    }
}

data class ModelGold(override val position: Vec3, val amount: Int) : NethackItem(position) {
    companion object {
        val factory = CompositeFeature<ModelGold>(listOf(
                Vec3Feature.from { it.position },
                IntFeature.from { it.amount }
        ))
    }
}

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
        val wrapper = (state as MyAgentState).env().nethackUnderTest
        return when (action) {
            Observe -> TODO()
            StartNewGame -> TODO()
            RestartGame -> TODO()
            is UseItem -> wrapper.useItem(action.inventoryIndex)
            is Move -> wrapper.move(action.movement)
            is Action -> wrapper.action(action.interact, "")
        }
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